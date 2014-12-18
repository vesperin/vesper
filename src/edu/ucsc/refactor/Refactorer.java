package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Commit;

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
     * <p />
     *
     * Context cache is flushed out after applying a given change. THis mean that if this method is
     * not invoked, then we have a cached context for the given Source.
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

    @Override String toString();
}
