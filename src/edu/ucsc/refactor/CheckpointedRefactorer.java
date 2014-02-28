package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CheckpointedRefactorer extends Refactorer {
    /**
     * Re-does any {@link CheckpointedRefactorer#regress(Source) undo-ed} changes made by the
     * {@code CheckpointedRefactorer}.
     *
     * @param current The current Source.
     * @return The {@code Source}'s next version, after the advance. Or the
     *      same {@code current} if {@code current} is the only
     *      available version of {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Source advance(Source current);

    /**
     * Checkpoints a commit and then returns a Source, which is the updated
     * source code after the application of the commit.
     *
     * @param commit The commit to be checkpointed.
     * @return The updated Source.
     */
    Source checkpoint(Commit commit);

    /**
     * Returns the list of {@code Source}s  tracked by this {@code CheckpointedRefactorer}.
     *
     * @return The list of tracked {@code Source}s.
     */
    List<Source> getTrackedSources();

    /**
     * Gets the commit history of a given {@code Source}.
     *
     * @return the compiled changed history, or empty history if none is available.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    CommitHistory getCommitHistory(Source src);

    /**
     * Retrieves the {@code CommitPublisher}; a strategy for publishing
     * all the changes to a given {@code Source}.
     *
     * @param src The current {@code Source}
     * @return The {@code CommitPublisher}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    CommitPublisher getCommitPublisher(Source src);

    /**
     * Rewrite a Source's Commit History having {@code Source} as a value.
     *
     * <p>
     * <strong>Note</strong>: After rewriting history, there is no turning back. So, you must be
     * {@code 100%} sure about using it.
     * </p>
     *
     * @param source THe current Source. This Source can be the same as the indexed Source, or
     *    a Source product of the methods {@link CheckpointedRefactorer#regress(Source)} or
     *    {@link CheckpointedRefactorer#advance(Source)}.
     *
     * @return the indexed Source, after the rewrite.
     * @throws java.lang.NullPointerException if {@code Source} null.
     * @throws java.util.NoSuchElementException if unable to find {@code current} {@code Source}.
     */
    Source rewriteHistory(Source source);

    /**
     * Un-does the changes made by the {@code CheckpointedRefactorer}.
     *
     * @param current The current Source.
     * @return The {@code Source}'s previous version, after the regress. Or the
     *      same {@code current} if {@code current} is the only
     *      available version of {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Source regress(Source current);
}
