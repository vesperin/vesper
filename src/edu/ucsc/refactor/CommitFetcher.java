package edu.ucsc.refactor;

import edu.ucsc.refactor.util.CommitHistory;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CommitFetcher {
    /**
     * Fetches a commit history of a Source matching some identifier (i.e., id)
     *
     * @param id The identifier of some Source.
     * @return The {@code CommitHistory}.
     */
    CommitHistory fetch(String id);
}
