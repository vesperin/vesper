package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;
import edu.ucsc.refactor.util.CommitPublisher;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CheckpointedRefactorer extends Refactorer {
    /**
     * In addition to {@code Refactorer}'s implementation, this checkpointed refactorer
     * exhibits the following behavior during the application of a change:
     *
     * <p>
     * Once a change is applied to a {@code Source}, this {@link CheckpointedRefactorer} re-inspects the
     * modified {@link Source} to check if there are still code issues that could be addressed.
     * The returned list of issues can be either empty (no issues found) or non-empty (issues found).
     * </p>
     *
     * <p>
     * To obtain the most recent list of issues (after any application of a change), the user must
     * invoke the {@link CheckpointedRefactorer#getIssues(Source)} method.
     * </p>
     */
    @Override Commit apply(Change change);

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
     * Returns the list of issues found in the given {@code Source}.
     *
     * @param key The source key
     * @return The list of issues detected in that source; or empty if no issues were found.
     */
    List<Issue> getIssues(Source key);

    /**
     * Checks whether a given source code has issues.
     *
     * @param code The {@code Source}.
     * @return {@code true} if the {@code Source} has issues, {@code false} otherwise.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    boolean hasIssues(Source code);

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
