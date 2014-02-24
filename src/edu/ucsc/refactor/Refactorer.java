package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.spi.UnitLocator;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Refactorer {
    /**
     * Re-does any {@link Refactorer#regress(Source) undo-ed} changes made by the
     * {@code Refactorer}.
     *
     * @param current The current Source.
     * @return The {@code Source}'s next version, after the advance. Or the
     *      same {@code current} if {@code current} is the only
     *      available version of {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Source advance(Source current);

    /**
     * Applies a code change to a {@code Source} and then returns a {@link Commit}, which
     * represents a set of applied changes, or "deltas", from one version of the {@code Source} to
     * the next.
     *
     * <p>
     * Once a change is applied to a {@code Source}, this {@link Refactorer} re-inspects the
     * modified {@link Source} to check if there are still code issues that could be addressed.
     * The returned list of issues can be either empty (no issues found) or non-empty (issues found).
     * </p>
     *
     * <p>
     * To obtain the most recent list of issues (after any application of a change), the user must
     * invoke the {@link Refactorer#getIssues(Source)} method.
     * </p>
     *
     *
     * @param change The change to be applied
     * @return The applied commit, null if the {@link Commit} could not
     *      be applied.
     * @throws java.lang.NullPointerException if change is null.
     */
    Commit apply(Change change);

    /**
     * Creates a code change in response to a {@code ChangeRequest}. This request describes
     * what needs to be change in the {@code Source} and also why should be changed. This
     * information is used by the {@code Refactorer} to make the necessary changes to a
     * tracked {@code Source}.
     *
     * <p> The following code shows a typical use scenario:</p>
     * <pre>
     *     final Change change = refactorer.createChange(ChangeRequest.deleteMethod(...));
     * </pre>
     *
     * @param request The {@link ChangeRequest}.
     * @return a new {@link Change} to be applied by {@link Refactorer} by calling
     *      {@link Refactorer#apply(Change)}.
     * @throws java.lang.NullPointerException if request is null.
     */
    Change createChange(ChangeRequest request);

    /**
     * Scans a {@link Source} tracked by the {@code Refactorer}, looking for any {@link Issue}s
     * in it.
     *
     * @param code The {@link Source} to be scanned.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    void detectIssues(Source code);

    /**
     * Returns the list of {@code Source}s  tracked by this {@code Refactorer}.
     *
     * @return The list of tracked {@code Source}s.
     */
    List<Source> getTrackedSources();

    /**
     * Returns the list of issues found in the given {@code Source}.
     *
     * @param key The source key
     * @return The list of issues detected in that source; or empty if no issues were found.
     */
    List<Issue> getIssues(Source key);

    /**
     * Gets the commit history of a given {@code Source}.
     *
     * @return the compiled changed history, or empty history if none is available.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    CommitHistory getCommitHistory(Source src);

    /**
     * Checks whether a given source code has issues.
     *
     * @param code The {@code Source}.
     * @return {@code true} if the {@code Source} has issues, {@code false} otherwise.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    boolean hasIssues(Source code);

    /**
     * Returns a locator of any structural unit of a base {@code Source} (class, member, ...).
     * Units exclude the code in a text form. Units include nodes in an AST.
     *
     * @param src The current {@code Source}
     * @return The locator created for this {@code Source}
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    UnitLocator getLocator(Source src);

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

    /**
     * Recommends changes for a {@code Source} based on a list of found {@code issues}.
     * E.g., if this {@code Source} has 10 issues in it, then the
     * {@code Refactorer} will recommend 10 changes that will address this 10 issues.
     *
     * Each change is orthogonal to every other change of the {@code recommended} list. Consequently,
     * once a change is applied to a {@code Source}, the remaining changes become outdated since
     * they contain an outdated version of the {@code Source}. To alleviate this problem, each time
     * the user chooses a {@code change} from the recommended list and then applies it, the user
     * must re-run the {@code Refactorer#recommendChanges(Source)} method to
     * guarantee a fresh set of recommended changes. Otherwise, source code conflicts may occur.
     *
     * @param code The {@code Source}
     * @return The list of recommended changes
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    List<Change> recommendChanges(Source code);

    /**
     * Rewrite a Source's Commit History having {@code Source} as a value.
     *
     * <p>
     * <strong>Note</strong>: After rewriting history, there is no turning back. So, you must be
     * {@code 100%} sure about using it.
     * </p>
     *
     * @param source THe current Source. This Source can be the same as the indexed Source, or
     *    a Source product of the methods {@link Refactorer#regress(Source)} or
     *    {@link Refactorer#advance(Source)}.
     *
     * @return the indexed Source, after the rewrite.
     * @throws java.lang.NullPointerException if {@code Source} null.
     * @throws java.util.NoSuchElementException if unable to find {@code current} {@code Source}.
     */
    Source rewriteHistory(Source source);

    /**
     * Un-does the changes made by the {@code Refactorer}.
     *
     * @param current The current Source.
     * @return The {@code Source}'s previous version, after the regress. Or the
     *      same {@code current} if {@code current} is the only
     *      available version of {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Source regress(Source current);
}
