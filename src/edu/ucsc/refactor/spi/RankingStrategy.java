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
     * @param docs The set of documents (text)
     * @return the ranked clip space; now as a list.
     */
    List<Clip> rankSpace(List<Clip> space, Set<String> docs);
}
