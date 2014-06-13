package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.IssueDetector;
import org.eclipse.jdt.core.dom.CompilationUnit;

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
     * @param context The context (and its compilation units) to scan trough for issues.
     * @return The detected issues.
     */
    Set<Issue> detectIssues(Context context);
}
