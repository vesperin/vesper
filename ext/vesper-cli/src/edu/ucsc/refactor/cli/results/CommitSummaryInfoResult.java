package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultVisitor;
import edu.ucsc.refactor.spi.CommitSummary;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitSummaryInfoResult implements Result {
    private final String description;
    private final CommitSummary summary;

    /**
     * @param description a brief description of result.
     * @param summary the CommitSummary
     */
    public CommitSummaryInfoResult(String description, CommitSummary summary){
        this.description    = description;
        this.summary        = summary;
    }

    @Override public String getBriefDescription() {
        return description;
    }


    public CommitSummary getCommitSummary(){
        return summary;
    }

    @Override public void accepts(ResultVisitor visitor) {
        visitor.visit(this);
    }


    @Override public String toString() {
        return getCommitSummary().more();
    }
}
