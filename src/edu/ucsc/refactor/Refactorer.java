package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.Upstream;
import edu.ucsc.refactor.util.ChangeHistory;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Refactorer {
    /**
     * Applies a change to a {@code Source} and then return its committed {@link CommitRequest}.
     * Once a change is applied to a {@code Source}, the {@link Refactorer} will re-inspect the
     * {@link Source} to update its list of {@code Source}'s issues. This list of issues can
     * be either empty (no issues found) or have more than one issue.
     *
     * To obtain the refreshed list of issues after the application of a change, the user must
     * call the  {@link Refactorer#getIssues(Source)} method.
     *
     * @param change The change to be applied
     * @return The committed request, null if the {@link CommitRequest} could not
     *      be committed.
     */
    CommitRequest apply(Change change);

    /**
     * Creates a change or solution that will address (e.g., fix or amend) a
     * given cause wrapped in a {@code ChangeRequest}.
     *
     * @param request The {@link ChangeRequest}.
     * @return a new {@link Change} to be applied by {@link Refactorer} by calling
     *      {@link Refactorer#apply(Change)}.
     */
    Change createChange(ChangeRequest request);

    /**
     * Scans the {@link Source} looking for {@link Issue}s in it.
     *
     * @param code The {@link Source} to be scanned.
     */
    void detectIssues(Source code);

    /**
     * @return a map representing the analysis result
     *      the refactorer will use to create adequate changes
     *      that may fix issues in the source.
     */
    Map<Source, List<Issue>> getIssueRegistry();

    /**
     * Returns the list of {@code Source}s  this {@code Refactorer}
     * is aware of.
     *
     * @return The list of {@code Source}s.
     */
    List<Source> getVisibleSources();

    /**
     * Returns the list of issues for that file. This list can be used
     * as an indicator for what refactorings are enabled in the UI.
     *
     * @param key The source key
     * @return The list of issues detected in that source.
     */
    List<Issue> getIssues(Source key);

    /**
     * Gets the change history of a given {@code Source}.
     *
     * @return the compiled changed history, or empty history if none is available.
     */
    ChangeHistory getChangeHistory(Source src);

    /**
     * Checks whether a given source code has issues.
     * @param code The {@code Source}.
     * @return {@code true} if the {@code Source} has issues. {@code false} otherwise.
     */
    boolean hasIssues(Source code);

    /**
     * Commit local changes to a remote upstream.
     *
     * @param localCommit The request (locally committed)
     * @return The request with updated status (remotely committed). This status contains
     *      info related to the remote commit.
     * @throws java.lang.IllegalArgumentException if {@code Vesper} lacks of the right
     *      credentials (!= null) to publish a local commit to a remote repository.
     */
    CommitRequest publish(CommitRequest localCommit);

    /**
     * Commit local changes to a remote upstream and return the request with an updated status.
     *
     * @param request A valid commit request (locally committed and with no errors).
     * @param upstream The upstream.
     * @return The request (remotely committed)
     * @throws java.lang.IllegalStateException if the request has already been remotely committed.
     */
    CommitRequest publish(CommitRequest request, Upstream upstream);

    /**
     * Recommends changes for {@code Source} based on found {@code issues}.
     * In other words, if this {@code Source} has 10 issues in it, then the
     * {@code Refactorer} will recommend 10 changes that will address this 10 issues.
     *
     * Each change is orthogonal to every other change of the {@code recommended} list. Consequently,
     * once a change is applied to a {@code Source}, the remaining changes become outdated since
     * they contain an outdated version of the {@code Source}. To alleviate this problem, each time
     * the user chooses a change from the recommended list and then applies it, the user must run
     * the {@code Refactorer#recommendChanges(Source)} method again to
     * guarantee a fresh set of changes. Otherwise, source code conflicts may occur.
     *
     * @param code The {@code Source}
     * @return The list of recommended changes
     */
    List<Change> recommendChanges(Source code);

    /**
     * Rewriting a Source's Change History having {@code Source} as a value.
     *
     * <strong>Note</strong>: After rewriting history, there is no turning back. So, you must be
     * {@code 100%} sure about using it.
     *
     * @param current THe current Source. This Source can be the same as the indexed Source, or
     *    a Source product of the methods {@link Refactorer#rollback(Source)} or
     *    {@link Refactorer#rollforward(Source)}.
     *
     * @return the indexed Source, after the rewrite.
     * @throws java.util.NoSuchElementException if unable to find {@code current} {@code Source}.
     */
    Source rewrite(Source current);

    /**
     * It is nothing but re-doing the changes made by the {@code Refactorer}; i.e., move to a
     * newer and <strong>existing</strong> version of the {@code current} {@code Source}.
     * History is not rewritten yet, all we are doing is walking the time arrow.
     *
     * To rewrite history, the caller should used the {@code Source} return by this method
     * as an input to the {@link Refactorer#rewrite(Source)}.
     *
     * @param current The current Source.
     * @return The Source, after the rollforward
     */
    Source rollforward(Source current);

    /**
     * Un-does the changes made by the {@code Refactorer}. History is not rewritten yet,
     * all we are doing is walking the time arrow.
     *
     * To rewrite history, the caller should used the {@code Source} return by this method
     * as an input to the {@link Refactorer#rewrite(Source)}.
     *
     * @param current The current Source.
     * @return The Source, after the rollback
     */
    Source rollback(Source current);
}
