package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.util.Commit;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CommitRequest {
    /**
     * Commit changes locally.
     *
     * @return A committed {@code CommitRequest}.
     * @throws RuntimeException if unable to commit changes.
     */
    Commit commit() throws RuntimeException ;

    /**
     * @return {@code true} if the changes this commit
     * contains NO errors {@code false} otherwise.
     */
    boolean isValid();
}
