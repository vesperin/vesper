package edu.ucsc.refactor.util;

import java.util.logging.Logger;

/**
 * Enables simple performance monitoring.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class StopWatch {
    private static final Logger LOGGER = Logger.getLogger(StopWatch.class.getName());

    private long start = System.currentTimeMillis();

    /**
     * Resets and returns elapsed time in milliseconds.
     */
    public long reset() {
        long now = System.currentTimeMillis();
        try {
            return now - start;
        } finally {
            start = now;
        }
    }

    /**
     * Resets and logs elapsed time in milliseconds.
     */
    public void resetAndLog(String label) {
        LOGGER.fine(label + ": " + reset() + "ms");
    }
}
