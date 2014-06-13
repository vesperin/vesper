package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.CentralityMeasure;
import edu.ucsc.refactor.spi.IssueDetector;

import java.util.Map;
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
     * Calculates the centrality of {@code ASTNode}s in a {@code CallGraph} based
     * on a {@link CentralityMeasure}
     *
     * @param code The {@link Source} to be introspected.
     * @param measure The {@link CentralityMeasure} to be used.
     * @return A map of {@code ASTNode}s and their centrality values.
     */
    Map<Object, Double> calculateCentrality(CentralityMeasure measure, Source code);
}
