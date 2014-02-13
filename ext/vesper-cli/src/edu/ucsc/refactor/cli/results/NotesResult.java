package edu.ucsc.refactor.cli.results;

import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.ResultProcessorVisitor;
import edu.ucsc.refactor.util.Notes;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class NotesResult implements Result {
    private final String message;
    private final Notes notes;

    /**
     * Constructs a new NotesResult.
     *
     * @param message The brief description
     * @param notes   The available {@code Notes}
     */
    public NotesResult(String message, Notes notes){
        this.message = message;
        this.notes   = notes;
    }

    @Override public void accepts(ResultProcessorVisitor visitor) {
        visitor.visit(this);
    }

    @Override public String getBriefDescription() {
        return message;
    }

    /**
     * @return the available notes on a given source.
     */
    public Notes getNotes(){
        final Notes copy = new Notes();
        return copy.union(this.notes);
    }

    @Override public String toString() {
        return getBriefDescription();
    }
}
