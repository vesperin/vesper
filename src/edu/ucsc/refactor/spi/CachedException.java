package edu.ucsc.refactor.spi;

import java.util.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class CachedException extends RuntimeException {
    private final List<Throwable> errorMessages;

    protected CachedException(){
        this(new ArrayList<Throwable>());
    }

    protected CachedException(List<Throwable> errorMessages){
        super();
        // Sort the messages by source.
        this.errorMessages = new ArrayList<Throwable>(errorMessages);
        if(!errorMessages.isEmpty()){
            sortMessages();
        }
    }

    public void cache(Throwable throwable){
        errorMessages.add(throwable);
    }

    protected abstract String getTitle();

    public String getMessage() {
        return createErrorMessage(getTitle(), errorMessages);
    }

    /**
     * Gets the error messages which resulted in this exception.
     */
    public Collection<Throwable> getErrorMessages() {
        return Collections.unmodifiableCollection(errorMessages);
    }

    public boolean isEmpty(){
        return this.errorMessages.isEmpty();
    }

    protected void sortMessages(){
        Collections.sort(this.errorMessages, new MessageComparator());
    }

    public abstract void throwCachedException() throws CachedException;

    private static String createErrorMessage(String title, Collection<Throwable> errorMessages) {
        final java.util.Formatter messageFormatter = new java.util.Formatter();
        messageFormatter.format(title + ":%n%n");
        int index = 1;

        for (Throwable errorMessage : errorMessages) {
            String    message = errorMessage.getMessage();
            messageFormatter.format("%s) Error at %s:%n", index++, message)
                    .format(" %s%n%n", message
                    );
        }

        return messageFormatter.format("%s error[s]", errorMessages.size()).toString();
    }


    static class MessageComparator implements Comparator<Throwable> {
        @Override public int compare(Throwable a, Throwable b) {
            return a.getMessage().compareTo(b.getMessage());
        }
    }
}
