package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Source;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Delta {

    private Source      source;
    private String      before;
    private String      after;

    /**
     * Instantiate a new Delta for a certain {@link Source}.
     *
     * @param source The changed source file
     */
    public Delta(Source source) {
        this.source = source;
    }

    /**
     * Set the {@link Source}'s before change state.
     *
     * @param before The Source's before state
     */
    public void setBefore(String before) {
        this.before = before;
    }

    /**
     * Returns the {@link Source}'s before change state.
     *
     * @return the {@link Source}'s before change state
     */
    public String getBefore() {
        return before;
    }

    /**
     * Set the {@link Source}'s after change state.
     *
     * @param after The Source's after state.
     */
    public void setAfter(String after) {
        this.after  = after;
    }

    /**
     * Set the {@link Source}'s after update.
     *
     * @param source The Source's after update.
     */
    final void setSource(Source source){
        this.source = source;
    }

    /**
     * Returns the {@link Source}'s after change state.
     *
     * @return the {@link Source}'s after change state
     */
    public String getAfter() {
        return after;
    }

    /**
     * Get the file from this delta
     *
     * @return the file
     */
    public Source getSource() {
        return source;
    }


}
