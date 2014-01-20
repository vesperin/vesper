package edu.ucsc.refactor.spi;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.util.CommitInformation;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitStatus implements Comparable <CommitStatus> {
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
     * @return The aborted status.
     */
    public static CommitStatus abortedStatus(String reason){
        return new CommitStatus(
                Status.ABORTED,
                Preconditions.checkNotNull(reason)
        );
    }

    /**
     * Creates a succeeded commit status.
     * @param builder  The CommitInformation.
     * @return A succeeded commit status.
     */
    public static CommitStatus succeededStatus(CommitInformation builder){
        return new CommitStatus(Status.COMMITTED, builder.toString());
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
     * @return {@code true} if this is a succeeded status.
     */
    public boolean isCommitted(){
        return this.type.isSame(Status.COMMITTED);
    }


    /**
     * @return {@code true} if this is a nothing status.
     */
    public boolean isAborted(){
        return this.type.isSame(Status.ABORTED);
    }


    /**
     * @return {@code true} if this is an unknown status.
     */
    public boolean isUnknown(){
        return this.type.isSame(Status.UNKNOWN);
    }

    @Override public int compareTo(CommitStatus that) {
        Preconditions.checkNotNull(that);
        return this.type.compareTo(that.type);
    }

    /**
     * Displays the contents of the change; e.g., time stamp, # of trials, number of errors.
     *
     * @return A human readable representation of changes.
     */
    public String more(){
        return type + " " + message;
    }


    @Override public String toString() {
        return more();
    }

    /**
     * The status
     */
    private static enum Status {
        /** There is nothing to commit **/
        ABORTED("Aborted"),
        /** Succeeded commit **/
        COMMITTED("Committed"),
        /** Unknown status; i.e., no commit yet **/
        UNKNOWN("Unknown");

        private String key;

        Status(String key){
            this.key = key;
        }


        boolean isSame(Status that){
            return this == that;
        }

        @Override public String toString() {
            return "(" + key + ")";
        }
    }

}
