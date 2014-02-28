package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.UnitLocator;
import edu.ucsc.refactor.util.Commit;

import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Refactorer {
    /**
     * Applies a code change to a {@code Source} and then returns a {@link Commit}, which
     * represents a set of applied changes, or "deltas", from one version of the {@code Source} to
     * the next.
     *
     * <p />
     *
     * Each change is orthogonal to every other change of the {@code list} of recommended changes
     * or detected issues. Consequently, once a change or fix to an issue is applied to a {@code Source},
     * the remaining changes (the ones that have not been applied) become outdated since they contain
     * an outdated version of the {@code Source} and therefore an different {@code AST}. To alleviate
     * this problem, each time the user chooses a {@code change} from the list of recommended changes or
     * from the list of detected issues to fix and then applies it, the user must re-run the
     * {@code Refactorer#recommendChanges(Source)} method to guarantee a fresh set of recommended
     * changes. Otherwise, source code conflicts may occur; e.g., Document does not match the AST.
     *
     * @param change The change to be applied
     * @return The applied commit, null if the {@link Commit} could not
     *      be applied.
     * @throws java.lang.NullPointerException if change is null.
     * @throws java.lang.IllegalArgumentException if conflicts in AST.
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
     * <p />
     *
     * Contexts are cached when invoking this method. THis mean that if this method is not invoked,
     * then a new context will be created per change request.
     *
     *
     * @param code The {@link Source} to be scanned.
     * @return a set of issues found in {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Set<Issue> detectIssues(Source code);

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
     * Recommends changes for a {@code Source} based on a list of found {@code issues}.
     * E.g., if this {@code Source} has 10 issues in it, then the
     * {@code Refactorer} will recommend 10 changes that will address this 10 issues.
     *
     * <p />
     *
     * Context cache is flushed out after recommending changes for a set of issues. THis mean that
     * if this method is not invoked, then the cached contexts will remain.
     *
     * @param code The {@code Source}
     * @param issues The issues from where changes will be recommended.
     * @return The list of recommended changes
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    List<Change> recommendChanges(Source code, Set<Issue> issues);
}
