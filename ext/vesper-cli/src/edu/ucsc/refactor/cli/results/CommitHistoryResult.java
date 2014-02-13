package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultVisitor;
import edu.ucsc.refactor.util.CommitHistory;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitHistoryResult implements Result {
    private final String        message;
    private final CommitHistory history;

    /**
     * Constructs a new {@code CommitHistoryResult}
     *
     * @param message the result's brief description
     * @param history the {@code CommitHistoryResult}
     */
    public CommitHistoryResult(String message, CommitHistory history){
        this.message = message;
        this.history = history;
    }

    @Override public void accepts(ResultVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String getBriefDescription() {
        return message;
    }


    /**
     * @return {@code CommitHistory}
     */
    public CommitHistory getCommitHistory(){
        return history.slice(history.last()); // creates a copy
    }


    @Override public String toString() {
        return getBriefDescription();
    }
}
