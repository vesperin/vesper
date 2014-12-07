package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.IssueDetector;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.List;
import java.util.Set;

/**
 * It examines a {@code Source} object.
 *
 * new CodeIntrospector(host);
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Introspector {
    /**
     * Scans a previously given {@link Source} tracked by the {@code Refactorer}, looking for any {@link Issue}s
     * in it.
     *
     * <p />
     *
     * Contexts are cached when invoking this method. THis mean that if this method is not invoked,
     * then a new context will be created per change request.
     *
     * @return a set of issues found in {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Set<Issue> detectIssues();

    /**
     * Scans a {@link Source} tracked by the {@code Refactorer}, looking for any {@link Issue}s
     * in it.
     *
     * <p />
     *
     * Contexts are cached when invoking this method. THis mean that if this method is not invoked,
     * then a new context will be created per change request.
     *
     * @param code The {@link Source} to be introspected.
     * @return a set of issues found in {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Set<Issue> detectIssues(Source code);

    /**
     * Scans a {@link Source} tracked by the {@code Refactorer}, looking for any {@link Issue}s
     * in it.
     *
     * <p />
     *
     * Contexts are cached when invoking this method. THis mean that if this method is not invoked,
     * then a new context will be created per change request.
     *
     * @param detector The {@link IssueDetector issue detector}.
     * @param parsedCode The {@link Context} to be introspected.
     * @return a set of issues found in {@code Source}.
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    Set<Issue> detectIssues(IssueDetector detector, Context parsedCode);

    /**
     * Let all the {@link IssueDetector}s scan the {@link CompilationUnit}s and
     * find the {@link Issue}s.
     *
     * @param context The Java context to scan through for issues.
     * @param selection  The user's code selection.
     * @return The detected issues.
     */
    Set<Issue> detectIssues(Context context, SourceSelection selection);

    /**
     * Let all the {@link IssueDetector}s scan the {@link CompilationUnit}s and
     * find the {@link Issue}s.
     *
     * @param context The context (and its compilation units) to scan through for issues.
     * @return The detected issues.
     */
    Set<Issue> detectIssues(Context context);

    /**
     * Scans a {@link Source} tracked by the {@code Refactorer}, looking for any missing imports.
     *
     * @param code The code to be scanned.
     * @return a set of required imports.
     */
    Set<String> detectMissingImports(Source code);

    /**
     * Compares two source and return their differences; i.e., insertions, changes, or deletions.
     *
     * Due to multi stage code examples goes from less to more complexity, we assume that
     * `original`'s length is less than `revised`'s length. Therefore, the implementation
     * of this method is based on that assumption. If this assumption is violated, then we will
     * flip the inputs to make sure they comply with this method's contract.
     *
     * @param original The original {@link Source}.
     * @param revised The revised {@link Source}.
     * @return the differences between the two sources.
     */
    Diff differences(Source original, Source revised);

    /**
     * Generates the clips space; i.e., the space containing all the different stages
     * (functionality) covered in the code example.
     *
     * @param code The code example used to created the clip space.
     * @return The clip space.
     */
    List<Clip> generateClipSpace(Source code);


    /**
     * Verifies whether this {@code Source} is valid; purely a syntax checking.
     *
     * More specifically, this method checks for:
     *
     * <ol>
     *     <li>Field related problems; e.g., undefined field</li>
     *     <li>Method related problems; e.g., undefined methods</li>
     *     <li>Internal related problems; e.g., two classes in same file</li>
     *     <li>Constructor related problems; e.g., undefined constructor</li>
     * </ol>
     *
     *
     * @param code The {@code Source} to be verified.
     * @return a list of error messages or []
     */
    List<String> verifySource(Source code);
}
