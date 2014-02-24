package edu.ucsc.refactor.spi;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Atomics;
import edu.ucsc.refactor.util.CommitInformation;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitSummary implements Comparable <CommitSummary> {
    private final CommitStatus type;
    private final String       message;

    private final AtomicReference<String>       userName;
    private final AtomicReference<Date>         committedAt;
    private final AtomicReference<String>       url;
    private final AtomicReference<String>       id;


    /**
     * Construct a {@code CommitSummary}.
     *
     * @param type The type of CommitSummary
     * @param message The human readable statement.
     */
    CommitSummary(CommitStatus type, String message){
        this.type       = type;
        this.message    = message;

        this.userName       = Atomics.newReference("(pending)");
        this.url            = Atomics.newReference("(pending)");
        this.id             = Atomics.newReference("(local)");
        this.committedAt    = Atomics.newReference();
    }


    /**
     * @return The failed commit summary.
     */
    public static CommitSummary forFailedCommit(String reason){
        return new CommitSummary(
                CommitStatus.FAILURE,
                Preconditions.checkNotNull(reason)
        );
    }

    /**
     * Creates the successful commit summary.
     *
     * @param username  who committed this.
     * @param committedAt  when was this committed
     * @param message  what was committed
     *
     * @return the successful commit summary.
     */
    public static CommitSummary forSuccessfulCommit(
            String username,
            Date committedAt,
            String message
    ){

        return forSuccessfulCommit(
                "(local)",
                username,
                committedAt,
                "(pending)",
                message
        );
    }

    /**
     * Creates the successful commit summary.
     *
     * @param id            commit identifier.
     * @param username      who committed this.
     * @param committedAt   when was this committed
     * @param url           where one can locate the updated source.
     * @param message       what was committed
     *
     * @return the successful commit summary.
     */
    public static CommitSummary forSuccessfulCommit(
            String id,
            String username,
            Date committedAt,
            String url,
            String message
    ){

        final CommitSummary summary = new CommitSummary(CommitStatus.SUCCESS, message);
        summary.addId(id);
        summary.addUsername(username);
        summary.addCommittedAt(committedAt);
        summary.addUrl(url);
        return summary;
    }

    /**
     * @return the canceled commit summary.
     */
    public static CommitSummary forCanceledCommit(String message){
        return new CommitSummary(CommitStatus.CANCELED, message);
    }


    /**
     * @return the pending commit summary.
     */
    public static CommitSummary forPendingCommit(){
        return new CommitSummary(
                CommitStatus.PENDING,
                "commit is pending"
        );
    }


    void addUsername(String username){
        this.userName.compareAndSet(this.userName.get(), username);
    }

    void addCommittedAt(Date committedAt){
        this.committedAt.compareAndSet(this.committedAt.get(), committedAt);
    }


    void addUrl(String url){
        this.url.compareAndSet(this.url.get(), url);
    }

    void addId(String id){
        this.id.compareAndSet(this.id.get(), id);
    }


    public CommitSummary updateSummary(CommitSummary status){
        final CommitSummary summary = new CommitSummary(status.type, status.message);
        summary.addId(status.getCommitId());
        summary.addUsername(status.getUsername());
        summary.addCommittedAt(status.getCommittedAt());
        summary.addUrl(status.getUrl());
        return summary;
    }

    /**
     * @return the commit id, or `(local)` if pending.
     */
    public String getCommitId(){
        return this.id.get();
    }

    /**
     * @return the commit message.
     */
    public String getMessage(){
        return this.message;
    }


    /**
     * @return the url where the updated Source resides.
     */
    public String getUrl(){
        return this.url.get();
    }

    /**
     * @return the user of this library.
     */
    public String getUsername(){
        return this.userName.get();
    }

    /**
     * @return the date and time when this commit was made.
     */
    public Date getCommittedAt(){
        return this.committedAt.get();
    }


    /**
     * @return {@code true} if this represents the summary of a succeeded commit.
     */
    public boolean isSuccess(){
        return this.type.isSame(CommitStatus.SUCCESS);
    }


    /**
     * @return {@code true} if this represents the summary of a failed commit.
     */
    public boolean isFailure(){
        return this.type.isSame(CommitStatus.FAILURE);
    }

    /**
     * @return {@code true} if this represents the summary of a canceled commit.
     */
    public boolean isCanceled(){
        return this.type.isSame(CommitStatus.CANCELED);
    }


    /**
     * @return {@code true} if this is the pending status.
     */
    public boolean isPending(){
        return this.type.isSame(CommitStatus.PENDING);
    }

    @Override public int compareTo(CommitSummary that) {
        Preconditions.checkNotNull(that);
        return this.type.compareTo(that.type);
    }

    /**
     * Displays the contents of the change; e.g., time stamp, # of trials, number of errors.
     *
     * @return A human readable representation of changes.
     */
    public String more(){

        final CommitInformation info = (isPending() || isCanceled() ?
                new CommitInformation()
                    .comment(getMessage()) :
                new CommitInformation()
                        .commit(getCommitId())
                        .author(getUsername())
                        .url(getUrl())
                        .date(getCommittedAt())
                        .comment(getMessage())
        );

        return info.toString();
    }


    @Override public String toString() {
        return more();
    }

    /**
     * The commit status
     */
    private static enum CommitStatus {
        CANCELED("canceled"),
        FAILURE("failure"),
        SUCCESS("success"),
        PENDING("pending");

        private String key;

        CommitStatus(String key){
            this.key = key;
        }


        boolean isSame(CommitStatus that){
            return this == that;
        }

        @Override public String toString() {
            return "(" + key + ")";
        }
    }

}
