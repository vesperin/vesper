package edu.ucsc.refactor;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ResultPackage {
    private final ASTNode parsedNode;
    private final boolean isSnippet;

    /**
     * Construct
     * @param parsedNode  the parsed ASTNode
     * @param isSnippet It is partial program; an incomplete code; we are dealing with blocks.
     */
    ResultPackage(ASTNode parsedNode, boolean isSnippet){
        this.parsedNode = parsedNode;
        this.isSnippet  = isSnippet;
    }

    public static ResultPackage empty(){
        return ResultPackage.makePackage(null, false);
    }

    public static ResultPackage makePackage(ASTNode parsed, boolean isSnippet){
        return new ResultPackage(parsed, isSnippet);
    }


    /**
     * Returns the parsed ASTNode.
     */
    public ASTNode getParsedNode(){ return this.parsedNode; }

    /**
     *
     * Returns whether the parsed node corresponds to a partial program; i.e., a block of
     * statements wrapped inside a MISSING class.
     */
    public boolean isSnippet(){ return this.isSnippet; }

    @Override  public String toString() {
        final String tName = getParsedNode() == null ? "Unknown" : getParsedNode()
                .getClass().getSimpleName();
        return "ResultPackage(" + "class=" + tName + ", isSnippet=" + isSnippet() + ")";
    }
}
