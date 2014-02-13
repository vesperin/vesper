package edu.ucsc.refactor.cli;

import edu.ucsc.refactor.spi.CommitSummary;

import static edu.ucsc.refactor.spi.CommitSummary.forPendingCommit;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Result {
    private final ResultType    type;
    private final String        description;
    private final CommitSummary summary;


    /**
     * Construct a {@code Result}.
     * @param key The type of content
     * @param value The content's value
     */
    private Result(ResultType key, CommitSummary value, String description){
        this.type           = key;
        this.summary        = value;
        this.description    = description;
    }

    private static Result createResult(ResultType resultType, CommitSummary value, String description){
        return new Result(resultType, value, description);
    }


    /**
     * Creates a Failed Result, which will consist of only the error message and
     * no commit request.
     *
     * @param message The error message.
     * @return a new failed Result
     */
    public static Result failedPackage(String message, CommitSummary summary){
        return createResult(ResultType.ERROR, summary, message);
    }

    /**
     * Creates a Failed Result, which will consist of only the error message and
     * no commit request.
     *
     * @param message The error message.
     * @return a new failed Result
     */
    public static Result failedPackage(String message){
        return failedPackage(message, CommitSummary.forFailedCommit(message));
    }


    /**
     * Creates an Info Result, which will consist of only the submitted commit status
     * object and an info message.
     *
     * @param message The info message
     * @param status The commit status
     * @return a new info Result
     */
    public static Result infoPackage(String message, CommitSummary status){
        return createResult(ResultType.INFO, status, message);
    }

    /**
     * Creates an info package.
     *
     * @param message the message to be wrapped in the result.
     * @return a new Result package.
     */
    public static Result infoPackage(String message){
        return infoPackage(message, forPendingCommit());
    }


    /**
     * Creates an info package.
     *
     * @param status The CommitSummary
     * @return a new info Result package.
     */
    public static Result infoPackage(CommitSummary status){
        return createResult(ResultType.INFO, status, status.getMessage());
    }


    /**
     * Creates a warning package.
     *
     * @param message The warning message.
     * @return a new warning Result package.
     */
    public static Result warningPackage(String message){
        return createResult(ResultType.WARNING, forPendingCommit(), message);
    }


    /**
     * Creates the Unit Result.
     *
     * @return a new Unit Result.
     */
    public static Result unitPackage(){
        return infoPackage("()");
    }


    /**
     * @return The Result type
     */
    public ResultType getType(){
        return type;
    }

    /**
     * @return The Result description
     */
    public String getDescription(){
        return description;
    }

    /**
     * @return The commit summary
     */
    public CommitSummary getCommitSummary(){
        return summary;
    }

    /**
     * @return {@code true} if it is an info result, {@code false} otherwise.
     */
    public boolean isInfo(){
        return getType() == ResultType.INFO;
    }

    /**
     * @return {@code true} if it is an error result, {@code false} otherwise.
     */
    public boolean isError(){
        return getType() == ResultType.ERROR;
    }

    /**
     * @return {@code true} if it is a warning result, {@code false} otherwise.
     */
    public boolean isWarning(){
        return getType() == ResultType.WARNING;
    }


    @Override public String toString() {
        return getType() + ":" + getDescription();
    }

    /**
     * The type of content to be stored.
     */
    public enum ResultType {
        INFO,
        WARNING,
        ERROR
    }
}