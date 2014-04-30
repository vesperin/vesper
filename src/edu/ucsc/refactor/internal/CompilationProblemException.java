package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.spi.CachedException;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CompilationProblemException extends CachedException {
    public CompilationProblemException(){
        super();
    }

    @Override protected String getTitle() {
        return "Java compilation errors";
    }

    @Override public void throwCachedException() throws CompilationProblemException {
        sortMessages();
        if(!isEmpty()) {
            throw this;
        }
    }
}
