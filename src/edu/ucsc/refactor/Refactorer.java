package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.CommitRequest;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Refactorer {

    /**
     * Applies a change and then return its committed {@link CommitRequest}.
     *
     * <strong>Node</strong>After applying the change, the {@link Refactorer} will
     * re-inspect the {@link Source} to update the {@link Source}'s list of issues.
     * Consequently, the caller of this method must call the {@link Refactorer#getIssues(Source)}
     * to get an updated list of {@link Source}'s issues.
     *
     * @param change The change to be applied
     * @return The committed request, null if the {@link CommitRequest} could not
     *      be committed.
     */
    CommitRequest apply(Change change);

    /**
     * Applies a list of changes to a {@link Source}, and return those ones that failed.
     *
     * @param changes The list of changes to be applied.
     * @return the list of changes that could NOT be processed.
     */
    List<Change> apply(List<Change> changes);

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
     * Checks whether a given source code has issues.
     * @param code The {@code Source}.
     * @return {@code true} if the {@code Source} has issues. {@code false} otherwise.
     */
    boolean hasIssues(Source code);

    /**
     * Recommends changes for {@code Source} based on whether this {@code Source} has
     * issues. In other words, if this {@code Source} has 10 issues in it, then the
     * {@code Refactorer} will recommend 10 changes that will address this 10 issues.
     *
     * <strong>Note</strong>: If the user decides to apply any of the recommended
     * changes, then the user is discouraged to apply the remaining ones as they
     * may have become obsolete. If the user proceeds with applying obsolete changes, then
     * this API does not make any guarantees that the source being updated will be ok.  This
     * indicates that every time the user applies one change from the list of recommended changes,
     * the user must re-call the {@link #recommendChanges(Source)} sub-routine.
     *
     * @param code The {@code Source}
     * @return The list of recommended changes
     */
    List<Change> recommendChanges(Source code);
}
