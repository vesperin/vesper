package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.SelectedASTNodeVisitor;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.ToStringBuilder;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class JavaRefactorer implements Refactorer {
    private static final Logger LOGGER = Logger.getLogger(JavaRefactorer.class.getName());

    private final HostImpl host;
    private final Map<Source, List<Issue>>  findings;
    private final Map<String, List<Record>> history;
    private final Map<Source, Context>      cachedContexts;

    private final HealthInspector   inspector;
    private final ChangeMaker       changeMaker;



    /**
     * Instantiates a new {@link JavaRefactorer} object.
     * @param host Vesper's {@code Host}
     */
    public JavaRefactorer(Host host) {
        this.host           = (HostImpl) host;
        this.findings       = new HashMap<Source, List<Issue>>();
        this.history        = new HashMap<String, List<Record>>();
        this.cachedContexts = new HashMap<Source, Context>();

        this.inspector   = new HealthInspector(host.getIssueDetectors());
        this.changeMaker = new ChangeMaker(host.getSourceChangers());
    }

    @Override public CommitRequest apply(Change change) {
        if(change == null) {
            throw new IllegalArgumentException(
                    "apply() method has received a null change"
            );
        }

        final CommitRequest applied = change.perform();

        if(applied == null) { return null; }

        if(applied.isValid()){
            try {
                applied.commit(host.getUpstream());
                final Source updated = applied.getUpdatedSource();
                checkpoint(change, updated);
                detectIssues(updated);
                return applied;
            } catch (RuntimeException ex){
                LOGGER.throwing("Unable to commit change", "apply()", ex);
                return null; // nothing was committed
            }
        }

        return null;  // nothing was committed
    }


    private void checkpoint(Change change, Source after){
        final Record record = new Record();
        record.before = change.getSource();
        record.after  = after;
        record.change = change;

        // clear current issue registry for source
        getIssueRegistry().remove(record.before);
        getValidContexts().remove(record.before);

        final String name = after.getName();

        if(history.containsKey(name)) { history.get(name).add(record); } else {
            history.put(name, new ArrayList<Record>());
            history.get(name).add(record);
        }
    }

    @Override public Change createChange(ChangeRequest request) {
        final boolean                isIssue    = request.isIssue();
        final CauseOfChange          cause      = request.getCauseOfChange();
        final Map<String, Parameter> parameters = request.getParameters();

        LOGGER.fine((isIssue
                        ? "Creating a change for an issue."
                        : "creating a change for a single edit.") );

        return changeMaker.createChange(
                (isIssue ? cause : prep(cause, request)), parameters
        );
    }


    private SingleEdit prep(CauseOfChange cause, ChangeRequest request){
        final SingleEdit      edit    = (SingleEdit) cause;
        final SourceSelection select  = request.getSelection();
        final Source          code    = select.first().getSource();
        final Context         context = getValidContexts().containsKey(code)
                                            ? getValidContexts().get(code)
                                            : host.createContext(code);

        getValidContexts().put(code, context);

        final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(select.toLocation());
        context.accept(visitor);

        // it blows up with we want to reformat a CompilationUnit, since
        // StructuralPropertyDescriptor in ASTRewrite. Therefore, here is a
        // HACK that solves the issue: A compilation unit is the same as formatting all
        // constructors and method declarations..
        if((visitor.getMatchedNode() instanceof CompilationUnit)
                || visitor.getMatchedNode() == null){
            final MethodDeclarationVisitor methods = new MethodDeclarationVisitor();
            methods.includeConstructor(true);
            context.accept(methods);
            for(MethodDeclaration each : methods.getMethodDeclarations()){
                edit.addNode(each);
            }
        } else {
            edit.addNode(visitor.getMatchedNode());
        }

        return edit;
    }


    @Override public void detectIssues(Source code) {
        try {
            final Context context = validContext(code);
            if(context == null) { return; }

            final Set<Issue> issues = inspector.detectIssues(context);
            for(Issue each : issues){
                registerIssue(code, each);
            }
        } catch (Exception ex){
            host.addError(ex);
            LOGGER.throwing(this.getClass().getName(), "detectIssues()", ex);
        }
    }

    private Map<Source, Context> getValidContexts(){
        return cachedContexts;
    }

    private Context validContext(Source code){
        final Context context = host.createContext(code);
        if(!canScanContextForIssues(context)) {
            LOGGER.fine("Cannot scan this context. Check configuration.");
            return null;
        }

        // cached contexts, just in case we need to reuse them
        cachedContexts.put(code, context);
        return context;
    }

    private Issue registerIssue(Source source, Issue issue) {
        if(getIssueRegistry().containsKey(source)) { this.findings.get(source).add(issue); } else {
            this.findings.put(source, new ArrayList<Issue>());
            this.findings.get(source).add(issue);
        }

        return issue;
    }

    /**
     * Checks whether the conditions are right so the detection service
     * to start scanning a source file for issues.
     *
     * @return {@code true} if the detection service can start scanning.
     *      {@code false} otherwise.
     */
    boolean canScanContextForIssues(Context context) {
        return context != null
                && !host.getIssueDetectors().isEmpty()
                && context.getSource() != null;
    }

    @Override public Map<Source, List<Issue>> getIssueRegistry() {
        return findings;
    }

    @Override public List<Source> getVisibleSources() {
        return new ArrayList<Source>(getIssueRegistry().keySet());
    }

    @Override public List<Issue> getIssues(Source key) {
        if(getIssueRegistry().containsKey(key)){
            return getIssueRegistry().get(key);
        }

        return Collections.emptyList();
    }

    @Override public boolean hasIssues(Source code) {
        return !getIssues(code).isEmpty();
    }

    @Override public List<Change> recommendChanges(Source code) {
        final List<Change> recommendations = new ArrayList<Change>();

        if(hasIssues(code)){
            final List<Issue> issues = getIssues(code);

            for(Issue issue : issues){
                recommendations.add(createChange(ChangeRequest.forIssue(issue, code)));
            }
        }

        return recommendations;
    }


    @Override public String toString() {
        final ToStringBuilder builder = new ToStringBuilder("Refactorer");
        builder.add("known files", getVisibleSources());
        return builder.toString();
    }

    /**
     * A record of changes made to a {@link Source}.
     */
    static class Record {
        public Source before;
        public Source after;
        public Change change;
    }

    /**
     * Helper class that allow {@code Refactorer} to scan the given
     * {@code Source}s in search of issues.
     */
    static class HealthInspector {
        private final List<IssueDetector> detectors;

        HealthInspector(List<IssueDetector> detectors){
            this.detectors = detectors;
        }


        /**
         * Let all the {@link IssueDetector}s scan the {@link CompilationUnit}s and
         * find the {@link Issue}s.
         *
         * @param context The Java context to scan through for issues.
         * @param selection  The user's code selection.
         * @return The detected issues.
         */
        Set<Issue> detectIssues(Context context, SourceSelection selection){
            if(context == null || selection == null) {
                throw new IllegalArgumentException(
                        "detectIssues() received a null context or a null source selection"
                );
            }

            context.setScope(selection);
            LOGGER.info("Detecting issues...");

            Set<Issue> issues = new HashSet<Issue>();

            for (IssueDetector detector : getDetectors()) {
                issues.addAll(detector.detectIssues(context));
            }

            LOGGER.fine("Done detecting issues");
            return issues;
        }




        /**
         * Let all the {@link IssueDetector}s scan the {@link CompilationUnit}s and
         * find the {@link Issue}s.
         *
         * @param context The context (and its compilation units) to scan trough for issues.
         * @return The detected issues.
         */
        Set<Issue> detectIssues(Context context) {
            if(context == null) {
                throw new IllegalArgumentException(
                        "detectIssues() received a null context"
                );
            }
            final Source file = context.getSource();
            return detectIssues(
                    context,
                    new SourceSelection(file, 0, file.getLength())  // scan whole file
            );
        }


        /**
         * Returns the list of issue detectors.
         *
         * @return The list of issue detectors.
         */
        List<IssueDetector> getDetectors() {
            return detectors;
        }

    }

    /**
     * Helper class that allow {@code Refactorer} to change a given {@code Source}s
     * because of found {@code Issue}s or triggered {@code SingleEdit}s.
     */
    static class ChangeMaker {
        private final List<SourceChanger> solvers;

        ChangeMaker(List<SourceChanger> changers){
            this.solvers = changers;
        }


        /**
         * Create a solution for an issue, based on a set of parameters.
         *
         * @param issue      The issue to createChange.
         * @param parameters The parameters to use in the solving process.
         * @return The solution to the issue.
         */
        Change createChange(CauseOfChange issue, Map<String, Parameter> parameters) {
            LOGGER.fine("Creating change for " + issue);
            final SourceChanger changer = findSuitableChanger(issue);

            if (null == changer) {
                throw new IllegalStateException("No suitable changer available.");
            }


            return changer.createChange(issue, parameters);
        }

        /**
         * Find a suitable {@link SourceChanger} for an Issue.
         *
         * @param issue The issue to find an IssueSolver for.
         * @return The IssueSolver, or null in case no suitable solver could be found.
         */
        SourceChanger findSuitableChanger(CauseOfChange issue) {
            LOGGER.fine("Looking for suitable source changers");
            for (SourceChanger solver : getChangers()) {
                if (solver.canHandle(issue)) {
                    LOGGER.info(solver + " found!");
                    return solver;
                }
            }

            LOGGER.warning("Not suitable source changer could be found!");
            return null;
        }


        /**
         * Returns the list of issue changers.
         *
         * @return The list of issue changers.
         */
        List<SourceChanger> getChangers() {
            return solvers;
        }
    }
}
