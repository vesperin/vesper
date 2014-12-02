package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.ResultPackage;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface JavaSnippetParser extends JavaParser {
    /**
     * Offers a context to be parsed; this type will try to parse the context
     * in order: (1) as CompilationUnit, (2) as TypeDeclaration, and (3) as a Block of statements.
     *
     * @param context the context to be parsed.
     * @return a result package containing the parsed node and an indicator communicating whether
     *      this ASTNode corresponds to a partial program; i.e., a snippet.
     */
    ResultPackage offer(Context context);
}
