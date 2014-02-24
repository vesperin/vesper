package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.Commit;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CommitPublisher {
    /**
     * Publishes all local commits using a default repository; a repository setup in
     * advanced.
     *
     * @return The list of commits to be deleted.
     * @throws java.lang.NullPointerException if {@code repository} is null.
     * @throws java.lang.IllegalStateException if no remote access; e.g., {@code credentials}
     *      has been provided.
     */
    List<Commit> publish();

    /**
     * Publishes local changes, wrapped in a locally committed request, to a remote upstream
     * and the return tue request with an updated status.
     *
     * @return The list of commits to be deleted.
     * @throws java.lang.NullPointerException if {@code repository} is null.
     * @throws java.lang.IllegalStateException if no remote access; e.g., {@code credentials}
     *      has been provided.
     */
    List<Commit> publish(Repository to);

    /**
     * Publishes local changes, wrapped in a locally committed request, to a remote upstream
     * and the return tue request with an updated status.
     *
     * @param localCommit A valid commit (locally committed and with no errors).
     * @param upstream The upstream.
     * @return The commit with updated status.
     * @throws java.lang.NullPointerException if {@code localCommit} or {@code upstream} are null.
     * @throws java.lang.IllegalStateException if the commit has already been remotely committed.
     */
    Commit publish(Commit localCommit, Repository upstream);
}
