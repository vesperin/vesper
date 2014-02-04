package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceVisitor;
import edu.ucsc.refactor.internal.util.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.List;
import java.util.TreeSet;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class DuplicateCodeVisitor extends SourceVisitor {
    private static final int    MASS_THRESHOLD          = 10;
    private static final float  SIMILARITY_THRESHOLD    = 0.98f;


    final TypeBucket typeBucket;
    final Source     code;

    /**
     * Constructs a new {@code DuplicateCodeVisitor} with the actual
     * source code as a value.
     *
     * @param code The {@code Source}
     */
    public DuplicateCodeVisitor(Source code){
        super(true);
        this.code       = code;
        this.typeBucket = new TypeBucket();
    }

    /**
     * @return The detected clones in the base {@code Source}.
     */
    public DetectedClones getClones(){
        final DetectedClones result = new DetectedClones();

        final TreeSet<BucketElement> candidates = typeBucket.getDuplicates();

        for(BucketElement candidate : candidates){
            for (int i = 1; i < candidate.size(); i++) {
                ASTNode left  = candidate.get(i - 1).getParseTree();
                ASTNode right = candidate.get(i).getParseTree();

                final TreeSimilarity similarity = new TreeSimilarity(left, right);
                if(similarity.getSimilarity() > SIMILARITY_THRESHOLD){
                    result.addClonePair(candidate.get(i - 1), candidate.get(i));
                }

            }
        }


        return result;
    }

    @Override public boolean visit(MethodDeclaration node) {
        hashSubtrees(node);

        return false;
    }



    /**
     * Hash (using the node type) all the subtrees and add the value to the type bucket.
     *
     * @param tree Subtree to hash.
     */
    private void hashSubtrees(ASTNode tree) {
        int mass = mass(tree);

        final List<ASTNode> children = AstUtil.getChildren(tree);
        final int childCount = children.size();

        if (mass >= MASS_THRESHOLD) { // Ignores small subtrees.
            for (int i = childCount - 1; i >= 0; i--) {
                hashSubtrees(children.get(i));
            }

            int nodeType = tree.getNodeType();

            DetectedClone candidate = new DetectedClone(code, tree);
            typeBucket.put(nodeType, candidate, mass);
        }
    }


    /**
     * Get the tree mass. The tree mass is simply the number children the
     * {@code current} node has.
     *
     * @param tree The current ASTNode
     * @return The number of nodes (i.e., children) in the ASTNode tree.
     */
    private int mass(ASTNode tree) {
        final List<ASTNode> children = AstUtil.getChildren(tree);
        final int childCount = children.size();
        int initialChildrenMass = childCount;

        if (initialChildrenMass > 0) {
            for (int i = childCount - 1; i >= 0; i--) {
                initialChildrenMass += mass(children.get(i));
            }
        }

        return initialChildrenMass;
    }



    @Override public boolean visit(SingleVariableDeclaration node) {
        return super.visit(node);
    }


    static class TreeSimilarity {
        int sharedNodes;
        int leftTreeDifferentNodes;
        int rightTreeDifferentNodes;

        TreeSimilarity(ASTNode left, ASTNode right){
            this.sharedNodes = 0;
            this.leftTreeDifferentNodes = 0;
            this.rightTreeDifferentNodes = 0;

            compareTree(left, right);
        }


        public float getSimilarity() {
            /*
             * Similarity = 2 x S /  (2 x S + L + R)
             * where:         S = number of shared nodes
             *                L = number of different nodes in sub-tree 1
             *                R = number of different nodes in sub-tree 2
             *
             * Source:         Clone Detection Using Abstract Syntax Trees
             *
             *                         Ira D. Baxter, Andrew Yahin, Leonardo Moura,
             *                         Marcelo Sant’Anna, Lorraine Bier
             */

            float fS = (float)sharedNodes;
            float fL = (float)leftTreeDifferentNodes;
            float fR = (float)rightTreeDifferentNodes;

            return (2 * fS) / (2 * fS + fL + fR);
        }


        private void compareTree(ASTNode left, ASTNode right) {
            if (left == null && right == null) {
                return;
            }

            if (left == null) {
                leftTreeDifferentNodes += 1;
            } else if (right == null) {
                rightTreeDifferentNodes += 1;
            } else if (left.getClass().getName().equals(right.getClass().getName())) {
                sharedNodes += 1;
            } else {
                leftTreeDifferentNodes  += 1;
                rightTreeDifferentNodes += 1;
            }

            compareChildren(left, right);
        }


        private void compareChildren(ASTNode left, ASTNode right) {
            int leftCount = 0;
            int rightCount = 0;


            final List<ASTNode> leftChildren  = AstUtil.getChildren(left);
            final List<ASTNode> rightChildren = AstUtil.getChildren(right);


            if (left != null) {
                leftCount = leftChildren.size();
            }

            if (right != null) {
                rightCount = rightChildren.size();
            }

            for (int i = Math.max(leftCount, rightCount); i >= 0; i--) {
                ASTNode leftSubtree  = null;
                ASTNode rightSubtree = null;

                if (i < leftCount) {
                    leftSubtree = leftChildren.get(i);
                }

                if (i < rightCount) {
                    rightSubtree = rightChildren.get(i);
                }

                compareTree(leftSubtree, rightSubtree);
            }
        }
    }
}
