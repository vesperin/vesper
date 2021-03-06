package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.util.Edits;
import edu.ucsc.refactor.locators.SelectedUnit;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.Commit;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static edu.ucsc.refactor.Vesper.createUnitLocator;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class JavaRefactorer implements Refactorer {
    private static final Logger LOGGER = Logger.getLogger(JavaRefactorer.class.getName());

    private final HostImpl          host;
    private final SourceChanging    changer;


    /**
     * Instantiates a new {@link JavaRefactorer} object.
     * @param host Vesper's {@code Host}
     */
    public JavaRefactorer(Host host) {
        this.host       = (HostImpl) host;
        this.changer    = new SourceChanging(host.getSourceChangers());
    }

    @Override public Commit apply(Change change) {
        Preconditions.checkNotNull(change, "apply() method has received a null change" );

        final CommitRequest applied = change.perform();

        if(applied == null) { return null; }

        if(applied.isValid()){
            try {
                return applied.commit();
            } catch (RuntimeException ex){
                LOGGER.throwing("Unable to commit change", "apply()", ex);
                return null; // nothing was committed
            }
        }

        return null;  // nothing was committed
    }


    @Override public Change createChange(ChangeRequest request) {
        Preconditions.checkNotNull(request, "createChange() method has received a null request");
        final boolean          isIssue    = request.isIssue();
        final Cause cause      = request.getCause();
        final Map<String, Parameter> parameters = request.getParameters();

        LOGGER.fine((isIssue
                        ? "Creating a change for an issue."
                        : "creating a change for a single edit.") );

        return changer.createChange(
                (isIssue ? cause : prepSingleEdit(cause, request)), parameters
        );
    }


    private Edit prepSingleEdit(Cause cause, ChangeRequest request){
        final Edit edit    = (Edit) cause;
        final SourceSelection select  = request.getSelection();
        final Source          code    = select.getSource();

        select.setContext(validContext(code));

        final UnitLocator           inferredUnitLocator = createUnitLocator(select.getContext());
        final List<NamedLocation>   namedLocations      = inferredUnitLocator.locate(
                new SelectedUnit(select)
        );

        for(NamedLocation eachNamedLocation : namedLocations){
            edit.addNode(((ProgramUnitLocation)eachNamedLocation).getNode());
        }

        return Edits.resolve(edit);
    }


    HostImpl getRefactoringHost(){
        return this.host;
    }


    Context validContext(Source code){
        final Context context = getRefactoringHost().createContext(code);

        if(!canScanContextForIssues(context)) {
            LOGGER.fine("Cannot scan this context. Check configuration.");
            return null;
        }

        return context;
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
                && !getRefactoringHost().getIssueDetectors().isEmpty()
                && context.getSource() != null;
    }


    @Override public String toString() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        builder.add("files", "none");
        return builder.toString();
    }


    /**
     * Helper class that allows the {@code Refactorer} to change a given {@code Source}s
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
        Change createChange(Cause issue, Map<String, Parameter> parameters) {
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
        SourceChanger findSuitableChanger(Cause issue) {
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
