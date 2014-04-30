package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.CachedException;

import java.util.List;

/**
 * Thrown when errors occur while creating a {@link Refactorer}. Includes a list
 * of encountered errors. Typically, a client should catch this exception, log
 * it, and stop execution.
 */
public class CreationException extends CachedException {

    /**
     * Constructs a new Refactorer creation exception for the given error messages.
     * @param errorMessages the error messages
     */
    public CreationException(List<Throwable> errorMessages){
        super(errorMessages);
    }


    @Override protected String getTitle() {
        return "Vesper configuration errors";
    }

    @Override public void throwCachedException() throws CreationException {}
}
