package edu.ucsc.refactor.internal.util;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Source;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Stores information about a (candidate) clone.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class DetectedClone {
    final Source  source;
    final ASTNode parseTree;

    /**
     * Create a new stored for a detected clone.
     *
     * @param source Source code
     * @param parseTree parsed ASTNode
     */
    public DetectedClone(Source source, ASTNode parseTree){
        this.source     = Preconditions.checkNotNull(source);
        this.parseTree  = Preconditions.checkNotNull(parseTree);
    }

    /**
     * Get the set the Source.
     *
     * @return the Source.
     */
    public Source getSource() {
        return source;
    }

    /**
     * Get the parse tree that is a clone.
     *
     * @return Parse tree that is a clone.
     */
    public ASTNode getParseTree() {
        return parseTree;
    }
}
