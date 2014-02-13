package edu.ucsc.refactor.spi;

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
    CommitRequest commit() throws RuntimeException ;

    /**
     * @return The issued commit summary; after committing change.
     */
    CommitSummary getCommitSummary();

    /**
     * @return {@code true} if the changes this commit
     * contains NO errors {@code false} otherwise.
     */
    boolean isValid();

    /**
     * Delegate call to {@link CommitSummary#more()} to get
     * a human readable of what went on in this commit request.
     *
     * @return A human readable representation of changes.
     */
    String more();
}
