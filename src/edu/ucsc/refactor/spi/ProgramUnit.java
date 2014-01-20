package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface ProgramUnit {
    /**
     * @return The unit's name.
     */
    String getName();

    /**
     * @param context The Source's {@code Context}.
     * @return The list of locations where this unit occurs.
     */
    List<Location> getLocations(Context context);
}
