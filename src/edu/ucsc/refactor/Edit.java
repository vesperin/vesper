package edu.ucsc.refactor;

import com.google.common.base.Objects;
import edu.ucsc.refactor.spi.Refactoring;

/**
 * A user-triggered change
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Edit extends AbstractCauseOfChange {
    private final Refactoring       name;         // name of operation
    private final SourceSelection   selection;    // source range

    /**
     * Instantiate a new SingleEdit.
     * @param name The name or description of this edit.
     * @param selection The scope of this edit.
     */
    public Edit(Refactoring name, SourceSelection selection){
        super();
        this.name       = name;
        this.selection  = selection;
    }

    /**
     * Reformat the {@code Source} code.
     *
     * @param code The source code to be formatted.
     * @return The {@code SingleEdit}.
     */
    public static Edit reformatCode(Source code){
        return new Edit(
                Refactoring.REFORMAT_CODE,
                new SourceSelection(code, 0, code.getLength())
        );
    }

    /**
     * Clips a given source selection. Not only the selection is clipped, but also
     * all the source elements used by the ASTNode representation of the clipped selection.
     * For example,
     *
     * <pre>
     *     Let's assume we have a class named Foo. This class has three methods; methodA, methodB,
     *     and methodC. This class also has two fields; fieldA and fieldB. <strong>methodA</strong> uses methodC and
     *     access fieldA; and methodC does not have any callers. Let's define the CLIP set; which is the set
     *     that will contain all the ASTNodes that were clipped from a source code. So, if the user wants
     *     to clip methodA, then this refactoring will include methodA, fieldA, and methodC in the CLIP set. Any
     *     elements (ASTNodes) not in the CLIP set will be removed from the Source code.
     * </pre>
     *
     * @param selection The source selection to be clipped.
     * @return The {@code SingleEdit}.
     */
    public static Edit clipSelection(SourceSelection selection){
        return new Edit(Refactoring.CLIP_SELECTION, selection);
    }


    /**
     * Deletes a class, which location can be inferred from {@code SourceSelection}
     *
     * Note: this edit can be performed only if this class is not the main class covering
     * the whole code snippet. If it is, then it will fail.
     *
     * @param selection The selected field
     * @return The {@code SingleEdit}.
     */
    public static Edit deleteClass(SourceSelection selection){
        return new Edit(Refactoring.DELETE_TYPE, selection);
    }

    /**
     * Deletes a method, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected method
     * @return The {@code SingleEdit}.
     */
    public static Edit deleteMethod(SourceSelection selection){
        return new Edit(Refactoring.DELETE_METHOD, selection);
    }


    /**
     * Deletes a field, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected field
     * @return The {@code SingleEdit}.
     */
    public static Edit deleteField(SourceSelection selection){
        return new Edit(Refactoring.DELETE_FIELD, selection);
    }


    /**
     * Deletes a local variable, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected local variable
     * @return The {@code SingleEdit}.
     */
    public static Edit deleteLocalVariable(SourceSelection selection){
        return new Edit(Refactoring.DELETE_VARIABLE, selection);
    }


    /**
     * Deletes a method parameter, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected method parameter
     * @return The {@code SingleEdit}.
     */
    public static Edit deleteParameter(SourceSelection selection){
        return new Edit(Refactoring.DELETE_PARAMETER, selection);
    }


    /**
     * Deletes a code region, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected region
     * @return The {@code SingleEdit}.
     */
    public static Edit deleteRegion(SourceSelection selection){
        return new Edit(Refactoring.DELETE_REGION, selection);
    }

    /**
     * Renames a selected member of a class (e.g., class, method, parameter, or field), which its
     * location can be inferred from {@code SourceSelection} object.
     *
     * @param selection The selected member
     * @return The {@code SingleEdit}.
     */
    public static Edit renameSelectedMember(SourceSelection selection){
        return new Edit(Refactoring.RENAME_SELECTION, selection);
    }

    /**
     * Optimizes the import declarations found in a {@code Source} code.
     *
     * @param code The source code whose import declarations will be optimized.
     * @return The {@code SingleEdit}.
     */
    public static Edit optimizeImports(Source code) {
        return new Edit(
                Refactoring.DELETE_UNUSED_IMPORTS,
                new SourceSelection(code, 0, code.getLength())
        );
    }


    /**
     * Renames a class or interface, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected class
     * @return The {@code SingleEdit}.
     */
    public static Edit renameClassOrInterface(SourceSelection selection){
        return new Edit(Refactoring.RENAME_TYPE, selection);
    }

    /**
     * Renames a method, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected method
     * @return The {@code SingleEdit}.
     */
    public static Edit renameMethod(SourceSelection selection){
        return new Edit(Refactoring.RENAME_METHOD, selection);
    }

    /**
     * Renames a parameter in a given method, which location (of method) can be inferred from
     * {@code SourceSelection}
     *
     * @param selection The selected parameter in a given method.
     * @return The {@code SingleEdit}.
     */
    public static Edit renameParameter(SourceSelection selection){
        return new Edit(Refactoring.RENAME_PARAMETER, selection);
    }


    /**
     * Renames a field in a given class, which location (of class) can be inferred from
     * {@code SourceSelection}
     *
     * @param selection The selected field in a given class.
     * @return The {@code SingleEdit}.
     */
    public static Edit renameField(SourceSelection selection){
        return new Edit(Refactoring.RENAME_FIELD, selection);
    }


    /**
     * Renames a local variable in a given scope, which location can be inferred from
     * {@code SourceSelection}
     *
     * @param selection The selected local variable in a given class | method.
     * @return The {@code SingleEdit}.
     */
    public static Edit renameLocalVariable(SourceSelection selection){
        return new Edit(Refactoring.RENAME_VARIABLE, selection);
    }

    @Override public Refactoring getName() {
        return name;
    }

    /**
     * Returns the scope of this edit.
     *
     * @return The {@code SourceSelection}.
     */
    public SourceSelection getSourceSelection(){
        return selection;
    }

    @Override public String more() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        final boolean isNodesEmpty = getAffectedNodes().isEmpty();

        if(isNodesEmpty){ builder.add("scope", getSourceSelection()); } else {
            builder.add("affected nodes", getAffectedNodes());
        }

        return builder.toString();
    }
}
