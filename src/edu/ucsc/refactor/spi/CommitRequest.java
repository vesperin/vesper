package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Source;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CommitRequest {
    /**
     * Abort a request.
     *
     * @param reason THe reason why this request was aborted.
     * @return An aborted {@code CommitRequest}
     */
    CommitRequest abort(String reason);

    /**
     * Commit changes locally.
     *
     * @return A committed {@code CommitRequest}.
     * @throws RuntimeException if unable to commit changes.
     */
    CommitRequest commit() throws RuntimeException ;

    /**
     * @return the time of commit in milliseconds,
     *      Long.MIN_VALUE if it has not been committed.
     */
    long commitTimestamp();

    /**
     * @return {@code true} if the changes this commit
     * contains NO errors {@code false} otherwise.
     */
    boolean isValid();

    /**
     * Gets the updated {@link Source}, after applying corresponding {@link Change}.
     *
     * @return The updated source code, or <tt>null</tt> if we could not commit anything.
     */
    Source getSource();

    /**
     * @return The issued status; after committing change.
     */
    CommitStatus getStatus();

    /**
     * Displays the contents of the change; e.g., time stamp, # of trials, number of errors.
     *
     * @return A human readable representation of changes.
     */
    String more();
}
