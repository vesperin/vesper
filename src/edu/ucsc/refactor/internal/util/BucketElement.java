package edu.ucsc.refactor.internal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class BucketElement implements Comparable<BucketElement> {
    private List<DetectedClone> elements;
    private int mass;

    public BucketElement() {
        this.elements = new ArrayList<DetectedClone>();
        this.mass     = 0;
    }

    /**
     * Add a detected clone.
     *
     * @param candidate The clone candidate to add.
     * @param mass Mass of the ASTNode.
     */
    public void put(DetectedClone candidate, int mass) {
        elements.add(candidate);

        if (mass > this.mass) {
            this.mass = mass;
        }
    }

    /**
     * Get the clone candidate at index.
     *
     * @param i Index.
     * @return The clone candidate at the index.
     */
    public DetectedClone get(int i) {
        return elements.get(i);
    }

    /**
     * Compare BucketElement objects.
     *
     * @param that other element
     * @return < 0 if this is smaller, 0 if equal and > 0 if this is lager.
     */
    @Override public int compareTo(BucketElement that) {
        return this.mass - that.mass;
    }

    /**
     * Gets the number of {@code ASTNode}s in this element.
     *
     * @return the number of {@code ASTNode}s in this element.
     */
    public int size() {
        return elements.size();
    }
}