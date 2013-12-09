package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.Context;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface SourceScanner {
    /**
     * Scans a Java context. Meant to be implemented
     * by its subclasses.
     *
     * @param context The context of interest.
     */
    void scanJava(Context context);
}
