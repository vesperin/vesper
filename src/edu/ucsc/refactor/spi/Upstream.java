package edu.ucsc.refactor.spi;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Upstream {
    CommitStatus publish(CommitRequest request);
}
