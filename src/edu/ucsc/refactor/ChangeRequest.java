package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Parameters;

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
     * Delete a class change request with selection.
     *
     * @see {@link SingleEdit#deleteClass(SourceSelection)}
     */
    public static ChangeRequest deleteClass(SourceSelection selection){
        return ChangeRequest.forEdit(
                SingleEdit.deleteClass(selection)
        );
    }


    /**
     * Delete a method change request with selection.
     *
     * @see {@link SingleEdit#deleteMethod(SourceSelection)}
     */
    public static ChangeRequest deleteMethod(SourceSelection selection){
        return ChangeRequest.forEdit(
                SingleEdit.deleteMethod(selection)
        );
    }


    /**
     * Delete a field change request with selection.
     *
     * @see {@link SingleEdit#deleteField(SourceSelection)}
     */
    public static ChangeRequest deleteField(SourceSelection selection){
        return ChangeRequest.forEdit(
                SingleEdit.deleteField(selection)
        );
    }


    /**
     * Delete a parameter change request with selection.
     *
     * @see {@link SingleEdit#deleteParameter(SourceSelection)}
     */
    public static ChangeRequest deleteParameter(SourceSelection selection){
        return ChangeRequest.forEdit(
                SingleEdit.deleteParameter(selection)
        );
    }


    /**
     * Rename a class/interface change request with selection and newName as values.
     *
     * @see {@link SingleEdit#renameMethod(SourceSelection)}
     */
    public static ChangeRequest renameClassOrInterface(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                SingleEdit.renameClassOrInterface(selection),
                Parameters.newClassOrInterfaceName(newName)
        );
    }

    /**
     * Rename a method change request with selection and newName as values.
     *
     * @see {@link SingleEdit#renameMethod(SourceSelection)}
     */
    public static ChangeRequest renameMethod(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                SingleEdit.renameMethod(selection),
                Parameters.newMethodName(newName)
        );
    }


    /**
     * Rename a parameter change request with selection and newName as values.
     *
     * @see {@link SingleEdit#renameParameter(SourceSelection)}
     */
    public static ChangeRequest renameParameter(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                SingleEdit.renameParameter(selection),
                Parameters.newParameterName(newName)
        );
    }



    /**
     * Rename a field change request with selection and newName as values.
     *
     * @see {@link SingleEdit#renameField(SourceSelection)}
     */
    public static ChangeRequest renameField(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                SingleEdit.renameField(selection),
                Parameters.newFieldName(newName)
        );
    }


    /**
     * Reformat source code change request with source as value.
     *
     * @see {@link SingleEdit#reformatCode(Source)}
     */
    public static ChangeRequest reformatSource(Source source){
        return ChangeRequest.forEdit(
                SingleEdit.reformatCode(source)
        );
    }

    /**
     * Optimizes the import declarations of a {@code Source}, cleaning out any
     * un-used imports.
     *
     * @see {@link SingleEdit#optimizeImports(Source)}
     */
    public static ChangeRequest optimizeImports(Source code){
        return ChangeRequest.forEdit(
                SingleEdit.optimizeImports(code)
        );
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
