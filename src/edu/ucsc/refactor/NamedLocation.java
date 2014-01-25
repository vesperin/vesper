package edu.ucsc.refactor;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface NamedLocation extends Location {
    /**
     * @return The ProgramUnit's name or identifier.
     */
    String getName();
}
