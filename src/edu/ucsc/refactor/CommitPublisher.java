package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.Commit;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CommitPublisher {
    /**
     * Publishes local changes, wrapped in a local commit, to a remote upstream.
     *
     * @param localCommit The local commit
     * @return The commit with updated status. This status contains
     *      info related to the publishing of this commit to a remote repository.
     * @throws java.lang.NullPointerException if {@code Commit} null.
     * @throws java.lang.IllegalArgumentException if {@code Vesper} lacks of the right
     *      credentials (!= null) to publish a local commit to a remote repository.
     */
    Commit publish(Commit localCommit);

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
