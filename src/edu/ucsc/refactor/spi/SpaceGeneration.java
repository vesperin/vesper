package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Clip;
import edu.ucsc.refactor.Source;

import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface SpaceGeneration {
    /**
     * Generates the clip space of a {@link Source code}.
     *
     * @param ofCode The {@link Source} of interest.
     * @return a reference to the updated clip space, useful if you wish to
     *      hold a reference to the clip space for checking post-conditions or
     *      other purposes (e.g., logging)
     */
    Set<Clip> generateSpace(Source ofCode);
}
