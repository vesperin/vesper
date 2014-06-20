package edu.ucsc.refactor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import edu.ucsc.refactor.spi.IssueDetector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.ucsc.refactor.Context.throwCompilationErrorIfExist;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {

    private final Host      host;
    private final Context   seedContext;

    /**
     * Construct a code introspector.
     *
     * @param host {@code Vesper}'s main {@link Host}.
     * @param seedContext cached {@code Context}
     */
    public CodeIntrospector(Host host, Context seedContext){
        this.host           = Preconditions.checkNotNull(host);
        this.seedContext = seedContext;
    }

    /**
     * Construct a code introspector.
     *
     * @param host {@code Vesper}'s main {@link Host}.
     */
    public CodeIntrospector(Host host){
        this(host, null);
    }

    @Override public Set<Issue> detectIssues() {
        return detectIssues(seedContext);
    }

    @Override public Set<Issue> detectIssues(Source code) {
        final Context context = this.host.createContext(code);
        return detectIssues(context);
    }

    @Override public Set<Issue> detectIssues(IssueDetector detector, Context parsedCode) {
        final IssueDetector nonNullDetector = Preconditions.checkNotNull(detector);
        final Context       nonNullContext  = Preconditions.checkNotNull(parsedCode);
        return nonNullDetector.detectIssues(nonNullContext);
    }

    @Override public Set<Issue> detectIssues(Context context, SourceSelection selection) {
        if(context == null || selection == null) {
            throw new IllegalArgumentException(
                    "detectIssues() received a null context or a null source selection"
            );
        }

        // syntax related problem are different than code issues; therefore \
        // we should fail fast when encountering them
        throwCompilationErrorIfExist(context);

        context.setScope(selection);

        Set<Issue> issues = new HashSet<Issue>();

        for (IssueDetector detector : this.host.getIssueDetectors()) {
            issues.addAll(detectIssues(detector, context));
        }

        return issues;
    }

    @Override public Set<Issue> detectIssues(Context context) {
        return detectIssues(
                context,
                new SourceSelection(
                        context.getSource(),
                        0,
                        context.getSource().getLength()
                ) // scan whole source code
        );
    }


    @Override public List<String> verifySource(Source code) {
        return ImmutableList.copyOf(
                this.host.createContext(code).getSyntaxRelatedProblems()
        );
    }
}
