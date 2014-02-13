package edu.ucsc.refactor.cli.results;

import com.google.common.collect.ImmutableList;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultProcessorVisitor;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class IssuesResult implements Result {
    private final String      message;
    private final List<Issue> issues;

    /**
     * Constructs a new {@code IssuesResult}
     *
     * @param message the result's brief description
     * @param issues  the list of detected issues in some source.
     */
    public IssuesResult(String message, List<Issue> issues){
        this.message = message;
        this.issues  = issues;
    }

    @Override public void accepts(ResultProcessorVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String getBriefDescription() {
        return message;
    }

    /**
     * @return the list of detected issues in some source.
     */
    public List<Issue> getIssues(){
        return ImmutableList.copyOf(issues);
    }

    @Override public String toString() {
        return getBriefDescription();
    }
}
