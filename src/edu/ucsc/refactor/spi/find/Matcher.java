package edu.ucsc.refactor.spi.find;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
interface Matcher<T> {
  /**
   * matches an object of type {@code T}.
   * @param that The object to be matched.
   * @return true if matched; false otherwise.
   */
  boolean matches(T that);
}
