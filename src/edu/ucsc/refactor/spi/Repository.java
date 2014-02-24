package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.util.Commit;

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
}
