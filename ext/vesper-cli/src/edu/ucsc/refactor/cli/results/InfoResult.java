package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultVisitor;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class InfoResult implements Result {
    private final String message;

    /**
     * Construct a new {@code InfoResult}.
     *
     * @param message The info message.
     */
    public InfoResult(String message){
        this.message = message;
    }

    @Override public void accepts(ResultVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String getBriefDescription() {
        return message;
    }

    @Override public String toString() {
        return getBriefDescription();
    }
}
