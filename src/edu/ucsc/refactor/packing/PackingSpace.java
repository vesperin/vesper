package edu.ucsc.refactor.packing;

import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface PackingSpace {
  /**
   * @return true if the TypeSpace is empty; false otherwise.
   */
  boolean isEmpty();

  /**
   * @param pkg the package to be introspected.
   * @return the set of classes (e.g., types) inside the given package.
   */
  Set<String> classSet(String pkg);

  /**
   * @return the set of packages tracked in the TypeSpace.
   */
  Set<String> packageSet();

  /**
   * Puts a package and its fellow members inside the TypeSpace.
   *
   * @param pkg the package to be tracked.
   * @param classMembers the classes inside the package to be tracked.
   * @return
   *      a reference to the TypeSpace object, useful if you wish to hold a reference to
   *      the space for checking post-conditions or other purposes (e.g., logging).
   */
  PackingSpace put(String pkg, Set<String> classMembers);

  /**
   * @return the size of the TypeSpace
   */
  int size();
}
