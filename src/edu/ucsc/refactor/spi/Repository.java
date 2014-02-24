package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.SourceHistory;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Repository {
    /**
     * Pushes the changes contained in a given commit object into a
     * {@code Repository}.
     *
     * @param commit The commit before application.
     * @return The pushed commit.
     * @throws java.lang.RuntimeException if unable to publish commit.
     */
    Commit push(Commit commit);

    /**
     * Pull the CommitHistory of some {@code Source}.
     *
     * @param thatHistory The source Id
     * @return The CommitHistory of the Source matching the source id.
     */
    SourceHistory pull(String thatHistory);
}
