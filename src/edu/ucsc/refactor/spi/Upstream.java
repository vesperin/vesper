package edu.ucsc.refactor.spi;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Upstream {
    /**
     * Publishes a commit request, locally applied, to a remote repository.
     *
     * @param request The commit request
     * @return The commit request (remotely published)
     */
    CommitRequest publish(CommitRequest request);
}
