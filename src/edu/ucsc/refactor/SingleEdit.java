package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.util.ToStringBuilder;

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
     * Reformats the {@code Source} code.
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
     * Deletes a method, which location can be inferred from {@code SourceSelection}
     *
     * @param selection The selected method
     * @return The {@code SingleEdit}.
     */
    public static SingleEdit deleteMethod(SourceSelection selection){
        return new SingleEdit(Refactoring.DELETE_METHOD, selection);
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
        final ToStringBuilder builder = new ToStringBuilder("SingleEdit");
        final boolean isNodesEmpty = getAffectedNodes().isEmpty();

        if(isNodesEmpty){ builder.add("scope", getSourceSelection()); } else {
            builder.add("affected nodes", getAffectedNodes());
        }

        return builder.toString();
    }
}
