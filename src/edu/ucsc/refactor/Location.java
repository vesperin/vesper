package edu.ucsc.refactor;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Location extends Comparable<Location> {
    /**
     * Returns the {@link Source} containing the warning. Note that
     * the file *itself* may not yet contain the error. When editing a code snippet in
     * the IDE for example, the tool could generate warnings in the background even before
     * the document is saved.
     *
     * @return the code snippet handle for the location
     */
    Source getSource();

    /**
     * The start position of the range
     *
     * @return the start position of the range, or null
     */
    Position getStart();

    /**
     * The end position of the range
     *
     * @return the end position of the range, may be null for an empty range
     */
    Position getEnd();
}
