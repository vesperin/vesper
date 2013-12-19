package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.util.MessageBuilder;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitStatus {
    private final Status type;
    private final String message;


    /**
     * Construct a {@code CommitStatus}.
     *
     * @param type The type of CommitStatus
     */
    CommitStatus(Status type, String message){
        this.type    = type;
        this.message = message;
    }

    /**
     * Creates a failed commit status.
     *
     * @param builder The MessageBuilder.
     * @return A failed commit status.
     */
    public static CommitStatus failedStatus(MessageBuilder builder){
        return new CommitStatus(Status.FAILED, builder.toString());
    }

    /**
     * Creates a succeeded commit status.
     * @param builder  The MessageBuilder.
     * @return A succeeded commit status.
     */
    public static CommitStatus succeededStatus(MessageBuilder builder){
        return new CommitStatus(Status.SUCCEEDED, builder.toString());
    }

    /**
     * Creates a canceled commit status.
     * @param builder The MessageBuilder.
     * @return a canceled commit status.
     */
    public static CommitStatus canceledStatus(MessageBuilder builder){
        return new CommitStatus(Status.CANCELED, builder.toString());
    }

    /**
     * @return The unknown status.
     */
    public static CommitStatus unknownStatus(){
        return new CommitStatus(
                Status.UNKNOWN,
                "unknown status"
        );
    }


    public CommitStatus update(CommitStatus status){
        return new CommitStatus(status.type, status.message);
    }


    /**
     * Displays the contents of the change; e.g., time stamp, # of trials, number of errors.
     *
     * @return A human readable representation of changes.
     */
    public String more(){
        return type + " " + message;
    }


    /**
     * The status
     */
    private static enum Status {
        /** Failed commit **/
        FAILED("Failed"),
        /** Succeeded commit **/
        SUCCEEDED("Succeeded"),
        /** Canceled commit **/
        CANCELED("Canceled"),
        /** Unknown status; i.e., no commit yet **/
        UNKNOWN("Unknown");

        private String key;

        Status(String key){
            this.key = key;
        }


        @Override public String toString() {
            return "(" + key + ")";
        }
    }

}
