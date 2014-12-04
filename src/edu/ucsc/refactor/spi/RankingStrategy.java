package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Clip;

import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface RankingStrategy {
    /**
     * Ranks the clip space with respect to a query.
     *
     * @param space The generated clip space.
     * @param query The query or text to be used to rank the space.
     * @return the ranked clip space; now as a list.
     */
    List<Clip> rankSpace(Set<Clip> space, String query);
}
