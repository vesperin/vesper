package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultVisitor;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceResult implements Result {
    private final String message;
    private final Source source;

    /**
     * Construct a new {@code SourceResult}
     *
     * @param message A brief description of result
     * @param source  The Source object.
     */
    public SourceResult(String message, Source source){
        this.message = message;
        this.source  = source;
    }

    @Override public void accepts(ResultVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String getBriefDescription() {
        return message;
    }

    /**
     * @return the {@code Source}
     */
    public Source getSource(){
        return source;
    }

    @Override public String toString() {
        return getBriefDescription();
    }
}
