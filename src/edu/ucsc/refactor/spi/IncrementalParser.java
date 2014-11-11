package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Context;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface IncrementalParser extends JavaParser {
    /**
     * Offers a context to be parsed; this type will try to parse the context
     * in order: (1) as CompilationUnit, (2) as Class Body, and (3) as a set of Statements.
     *
     * @param context the context to be parsed.
     * @return parse result map, where the key is the type of parsed unit, and the value is
     *      the produced ASTNode object.
     */
    Map<Class<? extends ASTNode>, ASTNode> offer(Context context);
}
