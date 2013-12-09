package edu.ucsc.refactor.spi;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Name {
    /**
     * @return The actual name which identifies an object.
     */
    String getKey();

    /**
     * @return A short but useful description of this name.
     */
    String getSummary();

    /**
     * Checks if this name is the same as the other name.
     * @param other The other {@code Name}
     * @return {@code true} if they are the same
     */
    boolean isSame(Name other);
}
