package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.SelectedASTNodeVisitor;
import edu.ucsc.refactor.spi.*;
import edu.ucsc.refactor.util.CommitHistory;
import edu.ucsc.refactor.util.Checkpoint;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class JavaRefactorer implements Refactorer {
    private static final Logger LOGGER = Logger.getLogger(JavaRefactorer.class.getName());

    private final HostImpl                  host;
    private final Map<Source, List<Issue>>  findings;
    private final Map<Source, Context>      cachedContexts;

    private final SourceChecking inspector;
    private final SourceChanging changer;

    private final Map<String, CommitHistory> timeline;



    /**
     * Instantiates a new {@link JavaRefactorer} object.
     * @param host Vesper's {@code Host}
     */
    public JavaRefactorer(Host host) {
        this.host           = (HostImpl) host;
        this.findings       = Maps.newHashMap();
        this.cachedContexts = Maps.newHashMap();

        this.inspector  = new SourceChecking(host.getIssueDetectors());
        this.changer    = new SourceChanging(host.getSourceChangers());
        this.timeline   = Maps.newHashMap();
    }

    @Override public CommitRequest apply(Change change) {
        Preconditions.checkNotNull(change, "apply() method has received a null change" );

        final CommitRequest applied = change.perform();

        if(applied == null) { return null; }

        if(applied.isValid()){
            try {
                final Source before = change.getSource();
                beforeCommit(before);
                applied.commit();
                final Source after = applied.getSource();
                afterCommit(change.getCause().getName(), before, applied);
                detectIssues(after);
                return applied;
            } catch (RuntimeException ex){
                LOGGER.throwing("Unable to commit change", "apply()", ex);
                return null; // nothing was committed
            }
        }

        return null;  // nothing was committed
    }


    private void beforeCommit(Source before){
        if(!timeline.containsKey(before.getUniqueSignature())){  // the signature should have been generated during compilation.
            this.timeline.put(before.getUniqueSignature(), new CommitHistory());
        }
    }


    private void afterCommit(Name name, Source before, CommitRequest applied){
        final Checkpoint checkpoint = Checkpoint.createCheckpoint(name, before, applied);

        // clear current issue registry for source
        getIssueRegistry().remove(checkpoint.getSourceBeforeChange());
        getValidContexts().remove(checkpoint.getSourceBeforeChange());

        final String key = checkpoint.getUniqueSignature();

        Preconditions.checkState(
                timeline.containsKey(key),
                "At this point the Source unique signature should have been recorded."
        );

        timeline.get(key).add(checkpoint);
    }

    @Override public Change createChange(ChangeRequest request) {
        Preconditions.checkNotNull(request, "createChange() method has received a null request");
        final boolean                isIssue    = request.isIssue();
        final CauseOfChange          cause      = request.getCauseOfChange();
        final Map<String, Parameter> parameters = request.getParameters();

        LOGGER.fine((isIssue
                        ? "Creating a change for an issue."
                        : "creating a change for a single edit.") );

        return changer.createChange(
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

            if(cause.getName().isSame(Refactoring.DELETE_UNUSED_IMPORTS)){
                edit.addNode(context.getCompilationUnit());
            } else {
                final MethodDeclarationVisitor methods = new MethodDeclarationVisitor();
                methods.includeConstructor(true);
                context.accept(methods);
                for(MethodDeclaration each : methods.getMethodDeclarations()){
                    edit.addNode(each);
                }
            }

        } else {
            edit.addNode(visitor.getMatchedNode());
        }

        return edit;
    }


    @Override public void detectIssues(Source code) {
        Preconditions.checkNotNull(code);
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

    // forward
    @Override public Source rewriteHistory(Source source) {

        final Source        from   = Preconditions.checkNotNull(source);
        final CommitHistory entire = getCommitHistory(from);

        if(entire.isEmpty()) { return source; }

        final Checkpoint    first  = entire.first();
        final Checkpoint    last   = entire.last();


        if(first.getSourceBeforeChange().equals(from)) { // base case
            return rewritingHistory(from, entire.slice());
        }

        if(last.getSourceAfterChange().equals(from)) {  // base case
            return from;
        }


        final CommitHistory sandwiched = entire.slice(first, false, last, false);

        for(Checkpoint each : sandwiched){

            if(each.getSourceAfterChange().equals(from)){
                final CommitHistory sliced = entire.slice(each);

                Preconditions.checkArgument(
                        from.getUniqueSignature().equals(sliced.last().getUniqueSignature()),
                        "rewriteHistory() is dealing with sources that are not part "
                        + "of the same change history"
                );

                return rewritingHistory(from, sliced);

            }
        }

        throw new NoSuchElementException("rewriteHistory() was unable to find " + from);
    }


    private Source rewritingHistory(Source from, CommitHistory sliced){
        final String signature = from.getUniqueSignature();

        if(timeline.containsKey(signature)){
            getIssueRegistry().remove(from);

            timeline.remove(signature);
            timeline.put(signature, sliced);

            detectIssues(from);

        }

        return from;
    }

    @Override public Source forward(Source current) {
        final CommitHistory history = getCommitHistory(current);

        for(Checkpoint each : history){
            if(each.getSourceBeforeChange().equals(current)){
                return each.getSourceAfterChange();
            }
        }

        // otherwise, there is nothing to forward (i.e., current is the latest version)
        return current;
    }

    @Override public Source rewind(Source current) {
        final CommitHistory history = getCommitHistory(current);

        for(Checkpoint each : history){
            if(each.getSourceAfterChange().equals(current)){
                return each.getSourceBeforeChange();
            }
        }

        return current; // nothing to roll back

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

    public Map<Source, List<Issue>> getIssueRegistry() {
        return findings;
    }

    @Override public List<Source> getTrackedSources() {
        return new ArrayList<Source>(getValidContexts().keySet());
    }

    @Override public List<Issue> getIssues(Source key) {
        if(getIssueRegistry().containsKey(key)){
            return getIssueRegistry().get(key);
        }

        return Collections.emptyList();
    }

    @Override public CommitHistory getCommitHistory(Source src) {
        final CommitHistory result = timeline.get(Preconditions.checkNotNull(src).getUniqueSignature());
        if(result == null){
            return new CommitHistory();
        }

        return result;
    }

    @Override public boolean hasIssues(Source code) {
        return !getIssues(Preconditions.checkNotNull(code)).isEmpty();
    }

    @Override public UnitLocator getLocator(Source readSource) {
        Preconditions.checkNotNull(readSource);
        Preconditions.checkState(!getValidContexts().isEmpty(), "unknown Source");
        return new ProgramUnitLocator(getValidContexts().get(readSource));
    }

    @Override public CommitRequest publish(CommitRequest localCommit){
        final Upstream upstream = (this.host.isRemoteUpstreamEnabled()
                ? new RemoteRepository(this.host.getStorageKey())
                : new LocalRepository(this.host.getStorageKey())
        );

        return publish(
                Preconditions.checkNotNull(localCommit),
                upstream
        );
    }

    @Override public CommitRequest publish(CommitRequest request, Upstream upstream) {
        return Preconditions.checkNotNull(upstream).publish(
                Preconditions.checkNotNull(request)
        );
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
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        builder.add("known files", getTrackedSources());
        return builder.toString();
    }


    /**
     * Helper class that allow {@code Refactorer} to scan the given
     * {@code Source}s in search of issues.
     */
    static class SourceChecking {
        private final List<IssueDetector> detectors;

        SourceChecking(List<IssueDetector> detectors){
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
            LOGGER.fine("Detecting issues...");

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
    static class SourceChanging {
        private final List<SourceChanger> changers;

        SourceChanging(List<SourceChanger> changers){
            this.changers = changers;
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
                    LOGGER.fine(solver + " found!");
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
            return changers;
        }
    }
}
