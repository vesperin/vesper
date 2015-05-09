package edu.ucsc.refactor.packing;

import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface PackingSpaceGeneration {
  /**
   * Generates the type space.
   *
   * @param allowedPackages the JDK packages of interest.
   * @return the generated {@link PackingSpace} object.
   */
  PackingSpace generateSpace(Set<String> allowedPackages);
}
