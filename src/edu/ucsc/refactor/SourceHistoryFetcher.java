package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.SourceHistory;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface SourceHistoryFetcher {
    /**
     * Fetches a source history of some Source matching some identifier (i.e., id)
     * previously given.
     *
     * @return The {@code SourceHistory}.
     * @throws java.lang.NullPointerException if the sourceId is null.
     */
    SourceHistory fetchSourceHistory();

    /**
     * Fetches a source history of a Source matching some identifier (i.e., id)
     *
     * @param fromRepository The location from where the commit history of a
     *                       source will be retrieved.
     * @param sourceId The id of a source.
     * @return The {@code SourceHistory}.
     * @throws java.lang.NullPointerException if either the sourceId or the repository are null.
     */
    SourceHistory fetchSourceHistory(Repository fromRepository, String sourceId);
}
