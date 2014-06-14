package edu.ucsc.refactor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.internal.CompilationProblemException;
import edu.ucsc.refactor.spi.IssueDetector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {

    private static final Logger LOGGER = Logger.getLogger(CodeIntrospector.class.getName());

    private final Host      host;
    private final Context   cachedContext;

    /**
     * Construct a code introspector.
     *
     * @param host {@code Vesper}'s main {@link Host}.
     * @param cachedContext cached {@code Context}
     */
    public CodeIntrospector(Host host, Context cachedContext){
        this.host           = Preconditions.checkNotNull(host);
        this.cachedContext  = cachedContext;
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
        return detectIssues(cachedContext);
    }

    @Override public Set<Issue> detectIssues(Source code) {
        try {
            final Context context = this.host.createContext(code);

            if(context == null) { return ImmutableSet.of(); }

            return detectIssues(context);
        } catch (Exception ex){
            LOGGER.severe(ex.getMessage());
            return ImmutableSet.of();
        }
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
                new SourceSelection(context.getSource(), 0, context.getSource().getLength()) // scan whole source code
        );
    }

    @Override public List<String> verifySource(Source code) {
        try {
            this.host.createContext(code);
            return ImmutableList.of();
        } catch (CompilationProblemException cpe){
            final List<String> problems = Lists.newArrayList();

            for(Throwable each : cpe.getErrorMessages()){
                problems.add(each.getMessage());
            }

            return ImmutableList.copyOf(problems);
        }
    }
}
