package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.util.SourceHistory;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface PullableRepository extends Repository {

    /**
     * Pull the CommitHistory of some {@code Source}.
     *
     * @param historyForId The source Id
     * @return The CommitHistory of the Source matching the source id.
     */
    SourceHistory pull(String historyForId);
}
