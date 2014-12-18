package edu.ucsc.refactor;

import java.util.List;

/**
 * This interface represents a locator on a base program structural unit.
 *
 * <p>
 * A program unit represents any structural part of the program (i.e. any part
 * excepting code) such as a class, a method, a method parameter, a field...
 * <p>
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface UnitLocator {
    /**
     * The list of locations where the given {@code key} and the its {@code hint}
     * points to.
     *
     * @param unit The program unit to be located, e.g., class, method, ....
     * @return The list of locations where the
     *      {@code key} is found, or empty if nothing was found.
     */
    List<NamedLocation> locate(ProgramUnit unit);
}
