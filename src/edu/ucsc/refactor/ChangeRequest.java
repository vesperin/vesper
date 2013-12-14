package edu.ucsc.refactor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ChangeRequest {

    /**
     * Default parameters used during refactoring actions.
     * // todo fill it out with default names or settings, developers should be able
     * // to rename them once they have understood the changes.
     */
    static final Map<String, Parameter> DEFAULT_PARAMETERS = new HashMap<String, Parameter>();

    private final CauseOfChange             cause;
    private final SourceSelection           selection;
    private final Map<String, Parameter>    parameters;


    /**
     * Creates a new {@code ChangeRequest}.
     *
     * @param cause The reason why this request was made.
     * @param selection The code area selected by the user.
     * @param parameters The change-supporting data, e.g., new name (in rename refactoring)
     */
    public ChangeRequest(CauseOfChange cause, SourceSelection selection,
                         Map<String, Parameter> parameters){
        this.cause      = cause;
        this.selection  = selection;
        this.parameters = parameters;
    }


    /**
     * Creates a new {@code ChangeRequest} with {@code Issue} and {@code Source}
     * as values.
     *
     * @param issue The main {@code Issue}
     * @return A new {@link ChangeRequest}.
     */
    public static ChangeRequest forIssue(Issue issue, Map<String, Parameter> parameters){
        return forIssue(issue, null, parameters);
    }


    /**
     * Creates a new {@code ChangeRequest} with {@code Issue} and {@code Source}
     * as values.
     *
     * @param issue The main {@code Issue}
     * @param code The {@code Source}; The code can be null.
     * @return A new {@link ChangeRequest}.
     */
    public static ChangeRequest forIssue(Issue issue, Source code, Map<String, Parameter> parameters){
        final Source src = (code != null
                ?  code
                : Source.from(issue.getAffectedNodes().get(0)));

        final SourceSelection selection = new SourceSelection(src, 0, src.getLength());
        return new ChangeRequest(issue, selection, parameters);
    }



    /**
     * Creates a new {@code ChangeRequest} with {@code Issue} and {@code Source}
     * as values.
     *
     * @param issue The main {@code Issue}
     * @param code The {@code Source}; The code can be null.
     * @return A new {@link ChangeRequest}.
     */
    public static ChangeRequest forIssue(Issue issue, Source code){
        return forIssue(issue, code, DEFAULT_PARAMETERS);
    }

    /**
     * Creates a new {@code ChangeRequest} for a given {@code Issue}.
     * @param issue The {@code Issue}
     * @return A new {@link ChangeRequest}
     */
    public static ChangeRequest forIssue(Issue issue){
        return forIssue(issue, null, DEFAULT_PARAMETERS);
    }

    /**
     * Creates a new {@code ChangeRequest} for a given {@code SingleEdit}.
     *
     * @param edit The {@code SingleEdit}
     * @return A new {@link ChangeRequest}
     */
    public static ChangeRequest forEdit(SingleEdit edit){
        return forEdit(edit, DEFAULT_PARAMETERS);
    }

    /**
     * Creates a new {@code ChangeRequest} with {@code SingleEdit} and
     * {@code parameters} as values.
     *
     * @param edit The {@code SingleEdit}
     * @return A new {@link ChangeRequest}
     */
    public static ChangeRequest forEdit(SingleEdit edit, Map<String, Parameter> parameters){
        return new ChangeRequest(edit, edit.getSourceSelection(), parameters);
    }

    /**
     * Checks whether the cause of a source code change is an {@code Issue} or not.
     *
     * @return {@code true} if it is an issue. {@code false} otherwise.
     */
    public boolean isIssue(){
        return getCauseOfChange().getContextScanner() != null;
    }

    /**
     * Returns the reason why a {@code Source} must be changed.
     *
     * @return The {@link CauseOfChange}.
     */
    public CauseOfChange getCauseOfChange(){
        return cause;
    }

    /**
     * Returns an area, in the source code, selected by a user.
     *
     * @return The {@link SourceSelection}
     */
    public SourceSelection getSelection(){
        return selection;
    }

    /**
     * Returns the change-supporting data, usually provided by the user.
     *
     * @return The supporting data or change parameters.
     */
    public Map<String, Parameter> getParameters(){
        return parameters;
    }


    @Override public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("ChangeRequest (").append(getSelection()).append(")").append(" for ");
        s.append(getCauseOfChange().getName()).append(isIssue() ? " issue." : " edit.");
        return s.toString();
    }
}