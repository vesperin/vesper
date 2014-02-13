package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultVisitor;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ErrorResult implements Result {
    private final String description;

    /**
     * Construct a new {@code ErrorResult}.
     *
     * @param description the error message
     */
    public ErrorResult(String description){
        this.description = description;
    }

    @Override public String getBriefDescription() {
        return description;
    }

    @Override public void accepts(ResultVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String toString() {
        return getBriefDescription();
    }
}
