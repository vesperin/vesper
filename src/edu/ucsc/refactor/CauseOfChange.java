package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.SourceScanner;
import edu.ucsc.refactor.spi.Name;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface CauseOfChange {
    /**
     * Returns the name of this object.
     *
     * @return The name of this object.
     */
    Name getName();

    /**
     * Returns the object that indicated why a {@code Source} must be changed.
     *
     * @return The code scanner, or <tt>null</tt> if no
     *      scanner is available.
     */
    SourceScanner getContextScanner();

    /**
     * Returns a shallow and unmodifiable copy of the {@link ASTNode}s
     * that are involved in this request.
     *
     * @return The {@link ASTNode}s that are involved in this issue.
     */
    List<ASTNode> getAffectedNodes();

    /**
     * Human readable description of this cause.
     *
     * @return A human readable representation of cause.
     */
    String more();

    /** Communicates this method should be overridden in subclasses **/
    @Override String toString();
}
