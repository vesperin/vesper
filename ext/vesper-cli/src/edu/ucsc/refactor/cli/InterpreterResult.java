package edu.ucsc.refactor.cli;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface InterpreterResult {
    /**
     * Accepts a non-null {@code ResultVisitor}.
     *
     * @param visitor The {@link ResultVisitor}
     */
    void accepts(ResultVisitor visitor);

    /**
     * @return a brief description of result.
     */
    String getBriefDescription();

    // Put on purpose as a reminder that it needs to be implemented.
    @Override String toString();
}
