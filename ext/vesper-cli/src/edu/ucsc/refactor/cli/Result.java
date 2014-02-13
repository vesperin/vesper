package edu.ucsc.refactor.cli;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Result {
    /**
     * Accepts a non-null {@code ResultProcessorVisitor}.
     *
     * @param visitor The {@link ResultProcessorVisitor}
     */
    void accepts(ResultProcessorVisitor visitor);

    /**
     * @return a brief description of result.
     */
    String getBriefDescription();

    // Put on purpose as a reminder that it needs to be implemented.
    @Override String toString();
}