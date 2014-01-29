package edu.ucsc.refactor.internal.util;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Type bucket contains node types and AST nodes that match a node type.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class TypeBucket {
    private HashMap<Integer, BucketElement> bucket;

    public TypeBucket() {
        bucket = new HashMap<Integer, BucketElement>();
    }

    /**
     * Add an {@code ASTNode} or parsed tree to the bucket.
     *
     * @param nodeType The node type of the parse tree.
     * @param candidate The candidate clone.
     * @param mass The mass (number of nodes or children) of the tree.
     */
    public void put(int nodeType, DetectedClone candidate, int mass) {
        BucketElement element;

        if (!bucket.containsKey(nodeType)) {
            element = new BucketElement();

            bucket.put(nodeType, element);
        } else {
            element = bucket.get(nodeType);
        }

        element.put(candidate, mass);
    }

    /**
     * Get all the {@link BucketElement} items with more than one {@code ASTNode}s.
     *
     * @return The list of {@link BucketElement} with more than one Parse Tree.
     */
    public TreeSet<BucketElement> getDuplicates() {
        final TreeSet<BucketElement> result = new TreeSet<BucketElement>();

        for (Integer key : bucket.keySet()) {
            if (bucket.get(key).size() > 1) {
                result.add(bucket.get(key));
            }
        }

        return result;
    }
}
