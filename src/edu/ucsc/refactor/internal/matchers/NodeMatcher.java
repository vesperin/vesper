package edu.ucsc.refactor.internal.matchers;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface NodeMatcher {
    /**
     * Matches an ASTNode.
     *
     * @param root The root ASTNode
     * @param element The child ASTNode
     * @return true if matched given input, false otherwise.
     */
    boolean match(ASTNode root, ASTNode element);
}
