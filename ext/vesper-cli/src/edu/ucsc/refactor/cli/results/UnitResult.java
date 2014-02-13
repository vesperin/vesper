package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultVisitor;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UnitResult implements Result {
    private final String symbol;

    /**
     * Constructs a UnitResult.
     */
    public UnitResult(){
        symbol = "()";
    }

    @Override public void accepts(ResultVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String getBriefDescription() {
        return symbol;
    }

    @Override public String toString() {
        return getBriefDescription();
    }
}
