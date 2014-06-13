package edu.ucsc.refactor;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.spi.CentralityMeasure;
import edu.ucsc.refactor.spi.IssueDetector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {
    private final Host host;

    /**
     * Construct a code introspector.
     *
     * @param host {@code Vesper}'s main {@link Host}.
     */
    public CodeIntrospector(Host host){
        this.host = Preconditions.checkNotNull(host);
    }

    @Override public Set<Issue> detectIssues(Source code) {
        final Set<Issue> issues = new HashSet<Issue>();

        final Context context = this.host.createContext(code);

        for (IssueDetector detector : this.host.getIssueDetectors()) {
            issues.addAll(detectIssues(detector, context));
        }

        return issues;
    }

    @Override public Set<Issue> detectIssues(IssueDetector detector, Context parsedCode) {
        final IssueDetector nonNullDetector = Preconditions.checkNotNull(detector);
        final Context       nonNullContext  = Preconditions.checkNotNull(parsedCode);
        return nonNullDetector.detectIssues(nonNullContext);
    }

    @Override public Map<Object, Double> calculateCentrality(CentralityMeasure measure, Source code) {
        return null;
    }
}
