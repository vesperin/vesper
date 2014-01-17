package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Location;

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
     * @param key The name of the program structural unit.
     * @param hint A hint that could help locate the unit, e.g., class, method, ....
     * @return The list of locations where the
     *      {@code key} is found, or empty if nothing was found.
     */
    List<Location> locate(String key, SearchHint hint);
}
