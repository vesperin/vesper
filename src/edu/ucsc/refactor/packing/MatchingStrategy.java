package edu.ucsc.refactor.packing;

import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface MatchingStrategy {
  /**
   * Matches the list of types found in the code example to
   * its corresponding packages. Possible implementations: Many-to-one Bipartite Matching
   *
   * @param types the set of available types
   * @param typeSpace the generated type space
   * @return the set of required packages for the available types.
   */
  List<String> matches(Set<String> types, PackingSpace typeSpace);
}
