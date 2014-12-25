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

    private final Cause                     cause;
    private final SourceSelection           selection;
    private final Map<String, Parameter>    parameters;


    /**
     * Creates a new {@code ChangeRequest}.
     *
     * @param cause The reason why this request was made.
     * @param selection The code area selected by the user.
     * @param parameters The change-supporting data, e.g., new name (in rename refactoring)
     */
    public ChangeRequest(Cause cause, SourceSelection selection,
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
    public static ChangeRequest forEdit(Edit edit){
        return forEdit(edit, DEFAULT_PARAMETERS);
    }

    /**
     * Clips a source selection. Check {@link Edit#clipSelection(SourceSelection)}
     * for details of how this works.
     *
     * @param selection The source selection to be clipped.
     * @return A new {@link ChangeRequest}
     */
    public static ChangeRequest clipSelection(SourceSelection selection){
        return ChangeRequest.forEdit(
                Edit.clipSelection(selection)
        );
    }


    /**
     * Delete a class change request given selection.
     *
     * @see {@link Edit#deleteClass(SourceSelection)}
     */
    public static ChangeRequest deleteClass(SourceSelection selection){
        return ChangeRequest.forEdit(
                Edit.deleteClass(selection)
        );
    }


    /**
     * Delete a method change request given selection.
     *
     * @see {@link Edit#deleteMethod(SourceSelection)}
     */
    public static ChangeRequest deleteMethod(SourceSelection selection){
        return ChangeRequest.forEdit(
                Edit.deleteMethod(selection)
        );
    }


    /**
     * Delete a field change request given selection.
     *
     * @see {@link Edit#deleteField(SourceSelection)}
     */
    public static ChangeRequest deleteField(SourceSelection selection){
        return ChangeRequest.forEdit(
                Edit.deleteField(selection)
        );
    }


    /**
     * Delete a local variable ChangeRequest given selection.
     *
     * @see {@link Edit#deleteField(SourceSelection)}
     */
    public static ChangeRequest deleteLocalVariable(SourceSelection selection){
        return ChangeRequest.forEdit(
                Edit.deleteLocalVariable(selection)
        );
    }


    /**
     * Delete a parameter change request given selection.
     *
     * @see {@link Edit#deleteParameter(SourceSelection)}
     */
    public static ChangeRequest deleteParameter(SourceSelection selection){
        return ChangeRequest.forEdit(
                Edit.deleteParameter(selection)
        );
    }


    /**
     * Delete a code region change request given selection.
     *
     * @see {@link Edit#deleteRegion(SourceSelection)}
     */
    public static ChangeRequest deleteRegion(SourceSelection selection){
        return ChangeRequest.forEdit(
                Edit.deleteRegion(selection)
        );
    }

    /**
     * Rename a 'selected member change request' with selection and newName as values.
     *
     * @see {@link Edit#renameSelectedMember(SourceSelection)}
     */
    public static ChangeRequest renameSelectedMember(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                Edit.renameSelectedMember(selection),
                Parameters.newMemberName(newName)
        );
    }


    /**
     * Rename a class/interface change request with selection and newName as values.
     *
     * @see {@link Edit#renameMethod(SourceSelection)}
     */
    public static ChangeRequest renameClassOrInterface(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                Edit.renameClassOrInterface(selection),
                Parameters.newMemberName(newName)
        );
    }

    /**
     * Rename a method change request with selection and newName as values.
     *
     * @see {@link Edit#renameMethod(SourceSelection)}
     */
    public static ChangeRequest renameMethod(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                Edit.renameMethod(selection),
                Parameters.newMemberName(newName)
        );
    }


    /**
     * Rename a parameter change request with selection and newName as values.
     *
     * @see {@link Edit#renameParameter(SourceSelection)}
     */
    public static ChangeRequest renameParameter(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                Edit.renameParameter(selection),
                Parameters.newMemberName(newName)
        );
    }



    /**
     * Rename a field change request with selection and newName as values.
     *
     * @see {@link Edit#renameField(SourceSelection)}
     */
    public static ChangeRequest renameField(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                Edit.renameField(selection),
                Parameters.newMemberName(newName)
        );
    }


    /**
     * Rename local variable ChangeRequest with selection and newName as values.
     *
     * @see {@link Edit#renameLocalVariable(SourceSelection)}
     */
    public static ChangeRequest renameLocalVariable(SourceSelection selection, String newName){
        return ChangeRequest.forEdit(
                Edit.renameLocalVariable(selection),
                Parameters.newMemberName(newName)
        );
    }


    /**
     * Reformat source code change request with source as value.
     *
     * @see {@link Edit#reformatCode(Source)}
     */
    public static ChangeRequest reformatSource(Source source){
        return ChangeRequest.forEdit(
                Edit.reformatCode(source)
        );
    }

    /**
     * Optimizes the import declarations of a {@code Source}, cleaning out any
     * un-used imports.
     *
     * @see {@link Edit#optimizeImports(Source)}
     */
    public static ChangeRequest optimizeImports(Source code){
        return ChangeRequest.forEdit(
                Edit.optimizeImports(code)
        );
    }

    /**
     * Creates a new {@code ChangeRequest} with {@code SingleEdit} and
     * {@code parameters} as values.
     *
     * @param edit The {@code SingleEdit}
     * @return A new {@link ChangeRequest}
     */
    public static ChangeRequest forEdit(Edit edit, Map<String, Parameter> parameters){
        return new ChangeRequest(edit, edit.getSourceSelection(), parameters);
    }

    /**
     * Checks whether the cause of a source code change is an {@code Issue} or not.
     *
     * @return {@code true} if it is an issue. {@code false} otherwise.
     */
    public boolean isIssue(){
        return Issue.class.isInstance(getCause());
    }

    /**
     * Returns the reason why a {@code Source} must be changed.
     *
     * @return The {@link Cause}.
     */
    public Cause getCause(){
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
        return "ChangeRequest (" + getSelection() + ")"
                + " for " + getCause().getName()
                + (isIssue() ? " issue." : " edit.");
    }
}
