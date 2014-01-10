package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.*;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Changer {
    /**
     * Indicates whether this {@link Changer} can handle a given
     * {@link CauseOfChange cause}.
     *
     * @param cause The cause to be handled.
     * @return {@code true} if this changer can handle this cause.
     */
    boolean canHandle(CauseOfChange cause);

    /**
     * Creates a new {@link Change} for a given cause.
     *
     * @param cause The issue to be solved.
     * @param parameters Supporting data for the solver.
     * @return a new {@link Change}.
     */
    Change createChange(CauseOfChange cause, Map<String, Parameter> parameters);

    /**
     * Commits a change by creating a commit request.
     *
     * @param change The {@code Change} to be committed.
     * @return The Commit Request.
     */
    CommitRequest commitChange(Change change, boolean offline);

    /**
     * getContext().locate(ASTNode): Location
     *
     * @param cause The cause to be located.
     * @return The list of locations corresponding to
     *      this cause.
     */
    List<Location> locate(CauseOfChange cause);
}
