package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.ToStringBuilder;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Context {
    private final Source                    file;

    private Location        scope;
    private CompilationUnit compilationUnit;

    /**
     * Construct a new {@link Context} object.
     *
     * @param file {@link Source} object.
     */
    public Context(Source file){
        this.file   = file;
    }


    /**
     * Accepts a visitor
     *
     * @param visitor The ASTVisitor
     */
    public void accept(ASTVisitor visitor){
        getCompilationUnit().accept(visitor);
    }


    /**
     * Get the content of the source file.
     *
     * @return The source file's content.
     */
    public String getContents(){
        return getSource().getContents();
    }

    /**
     * Get the current scope (i.e., Location)
     * @return The {@code Location}
     */
    public Location getScope(){
        final Location currentScope = this.scope;
        if(currentScope == null){
            throw new IllegalStateException("Context's scope is null");
        }

        return currentScope;
    }


    /**
     * Set the CompilationUnit that belongs to the content of the Source's file.
     *
     * @param compilationUnit The compilation unit.
     */
    public void setCompilationUnit(CompilationUnit compilationUnit) {
        if(compilationUnit == null){
            throw new IllegalArgumentException(
                    "setCompilationUnit() was given a null compilation unit."
            );
        }

        this.compilationUnit = compilationUnit;
        this.compilationUnit.setProperty(
                Source.SOURCE_FILE_PROPERTY,
                this.getSource()
        );
    }

    /**
     * Get the compilation unit that belongs to the content of the source file's file.
     *
     * @return The source file's compilation unit
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    /**
     * Locates a ASTNode in the {@code Source}.
     *
     * @param node The ASTNode to be located.
     * @return A {@code Location} in the {@code Source} where a ASTNode is found.
     */
    public Location locate(ASTNode node) {
        return Locations.locate(node);
    }

    public Source getSource() {
        return file;
    }

    /**
     * Sets the context scope (if any).
     *
     * @param selection The {@link SourceSelection}
     */
    public void setScope(SourceSelection selection) {
        this.scope = selection.toLocation();
    }

    @Override public String toString() {
        final ToStringBuilder builder = new ToStringBuilder("Context");
        return builder.toString();
    }
}
