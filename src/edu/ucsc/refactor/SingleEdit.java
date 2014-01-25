package edu.ucsc.refactor;

import com.google.common.base.Objects;
import edu.ucsc.refactor.spi.Refactoring;

/**
 * A user-triggered change
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SingleEdit extends AbstractCauseOfChange {
    private final Refactoring       name;         // name of operation
    private final SourceSelection   selection;    // source range

    /**
     * Instantiate a new SingleEdit.
     * @param name The name or description of this edit.
     * @param selection The scope of this edit.
     */
    public SingleEdit(Refactoring name, SourceSelection selection){
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
    public static SingleEdit reformatCode(Source code){
        return new SingleEdit(
                Refactoring.REFORMAT_CODE,
                new SourceSelection(code, 0, code.getLength())
        );
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
    public static SingleEdit deleteClass(SourceSelection selection){
        return new SingleEdit(Refactoring.DELETE_TYPE, selection);
    }

    /**
     * Deletes a method, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected method
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit deleteMethod(SourceSelection selection){
        return new SingleEdit(Refactoring.DELETE_METHOD, selection);
    }


    /**
     * Deletes a field, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected field
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit deleteField(SourceSelection selection){
        return new SingleEdit(Refactoring.DELETE_FIELD, selection);
    }


    /**
     * Deletes a method parameter, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected method parameter
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit deleteParameter(SourceSelection selection){
        return new SingleEdit(Refactoring.DELETE_PARAMETER, selection);
    }

    /**
     * Optimizes the import declarations found in a {@code Source} code.
     *
     * @param code The source code whose import declarations will be optimized.
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit optimizeImports(Source code) {
        return new SingleEdit(
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
    public static SingleEdit renameClassOrInterface(SourceSelection selection){
        return new SingleEdit(Refactoring.RENAME_TYPE, selection);
    }

    /**
     * Renames a method, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected method
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit renameMethod(SourceSelection selection){
        return new SingleEdit(Refactoring.RENAME_METHOD, selection);
    }

    /**
     * Renames a parameter in a given method, which location (of method) can be inferred from
     * {@code SourceSelection}
     *
     * @param selection The selected parameter in a given method.
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit renameParameter(SourceSelection selection){
        return new SingleEdit(Refactoring.RENAME_PARAMETER, selection);
    }


    /**
     * Renames a field in a given class, which location (of class) can be inferred from
     * {@code SourceSelection}
     *
     * @param selection The selected field in a given class.
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit renameField(SourceSelection selection){
        return new SingleEdit(Refactoring.RENAME_FIELD, selection);
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
