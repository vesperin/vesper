package edu.ucsc.refactor;

import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CodePacker {
  /**
   * Returns the existing header of the packed code.
   *
   * @param code Packed Code example.
   * @return the existing header
   */
  String existingHeader(Source code);

  /**
   * Returns the missing header of the unpacked code.
   *
   * @param code Unpacked Code example.
   * @return the missing header
   */
  String missingHeader(Source code, String name);

  /**
   * Packs missing imports and then returns them to caller.
   *
   * @param code the code to be introspected.
   * @param prepend value to be prepended
   * @return the set of packed imports
   */
  Set<String> missingImports(Source code, String prepend);

  /**
   * Packs missing imports and then returns them to caller.
   *
   * @param code the code to be introspected.
   * @return the set of packed imports
   */
  Set<String> missingImports(Source code);

  /**
   * Packs a class declaration inside the partial code example.
   * @param code the partial code example to be packed
   * @param name the name of the packed code example;
   * @return the packed code example.
   */
  Source packs(Source code, String name);

  /**
   * Packs a class declaration inside the partial code example.
   * @param code the partial code example to be packed
   * @return the packed code example.
   */
  Source packs(Source code);

  /**
   * Unpacks a packed code example.
   *
   * @param packed packed code example to be unpacked.
   * @return unpacked code example.
   */
  Source unpacks(Source packed);
}
