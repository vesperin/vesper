package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Context;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface JavaParser {
    /**
     * Parses the {@link edu.ucsc.refactor.Source} pointed to by the given context.
     *
     * @param context the context pointing to the file to be parsed, typically
     *        via {@link Context#getContents()} but the file handle (
     *        {@link Context#getSource()} can also be used to map to an existing
     *        editor buffer in the surrounding tool, etc)
     * @return the compilation unit node for the file; useful for post processing.
     */
    CompilationUnit parseJava(Context context);
}
