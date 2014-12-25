package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.Name;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Cause {
    /**
     * Returns the name of this cause.
     *
     * @return The name of this cause.
     */
    Name getName();

    /**
     * Checks if the name of this cause is the same as the other name.
     *
     * @param otherName the other name to be checked.
     * @return true if they are same; false otherwise.
     */
    boolean isSame(Name otherName);

    /**
     * Returns a shallow and unmodifiable copy of the {@link ASTNode}s
     * that are involved in this cause.
     *
     * @return The payload of this cause
     */
    List<ASTNode> getAffectedNodes();

    /**
     * Human readable description of this stimulus.
     *
     * @return A human readable representation of stimulus.
     */
    String more();

    /** Communicates this method should be overridden in subclasses **/
    @Override String toString();
}
