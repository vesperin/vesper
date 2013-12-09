package edu.ucsc.refactor.internal;

import difflib.DiffUtils;
import difflib.Patch;
import edu.ucsc.refactor.Source;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        this.after = after;
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
    public Source getSourceFile() {
        return source;
    }

    /**
     * Gets the difference between the before and after source.
     *
     * @return The list of differences between the original {@code Source} and
     *      the updated {@code Source}.
     */
    public List<difflib.Delta> getDifferences() {
        Patch patch = DiffUtils.diff(contentToLines(before), contentToLines(after));
        return patch.getDeltas();
    }

    /**
     * Splits the content of a file into separate lines.
     *
     * @param content The content to split.
     * @return a List of all lines in the content string.
     */
    private List<String> contentToLines(String content) {
        String[] lines = content.split(System.getProperty("line.separator"));
        List<String> linesAsList = new LinkedList<String>();

        linesAsList.addAll(Arrays.asList(lines));

        return linesAsList;
    }
}
