package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Source;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CommitRequest {
    /**
     * commit changes to some external service, such as
     * Gist or Pastie.
     *
     * @return A {@code CommitStatus}.
     * @throws RuntimeException if unable to commit changes.
     */
    CommitStatus commit() throws RuntimeException ;

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
    Source getUpdatedSource();

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
