package edu.ucsc.refactor;

import java.util.*;

/**
 * Thrown when errors occur while creating a {@link Refactorer}. Includes a list
 * of encountered errors. Typically, a client should catch this exception, log
 * it, and stop execution.
 */
public class CreationException extends RuntimeException {

    private final List<Throwable> errorMessages;

    /**
     * Constructs a new exception for the given errors.
     */
    public CreationException(Collection<Throwable> errorMessages) {
        super();

        // Sort the messages by source.
        this.errorMessages = new ArrayList<Throwable>(errorMessages);
        Collections.sort(this.errorMessages, new MessageComparator());
    }

    public String getMessage() {
        return createErrorMessage(errorMessages);
    }

    private static String createErrorMessage(Collection<Throwable> errorMessages) {
        final Formatter messageFormatter = new Formatter();
        messageFormatter.format("Vesper configuration errors:%n%n");
        int index = 1;

        for (Throwable errorMessage : errorMessages) {
            String    message = errorMessage.getMessage();
            messageFormatter.format("%s) Error at %s:%n", index++, message)
                            .format(" %s%n%n", message
                            );
        }

        return messageFormatter.format("%s error[s]", errorMessages.size()).toString();
    }

    /**
     * Gets the error messages which resulted in this exception.
     */
    public Collection<Throwable> getErrorMessages() {
        return Collections.unmodifiableCollection(errorMessages);
    }

    static class MessageComparator implements Comparator<Throwable> {
        @Override public int compare(Throwable a, Throwable b) {
            return a.getMessage().compareTo(b.getMessage());
        }
    }
}
