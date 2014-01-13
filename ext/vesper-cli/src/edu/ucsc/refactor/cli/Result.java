package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitRequest;

import java.util.Collections;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Result {
    private final Content   type;
    private final Object    data;

    /**
     * Private constructor to prevent instantiation.
     */
    private Result(Content type, Object data){
        this.type       = Preconditions.checkNotNull(type);
        this.data       = Preconditions.checkNotNull(data);
    }

    /**
     * Creates a Failed Result, which will consist of only the error message and
     * no commit request.
     *
     * @param message The error message.
     * @return a new failed Result
     */
    public static Result failedPackage(String message){
        return new Result(Content.ERROR, message);
    }

    /**
     * Creates a Success Result, which will consist of only the submitted commit request
     * object and no error message.
     *
     * @param request The commit request
     * @return a new failed Result
     */
    public static Result committedPackage(CommitRequest request){
        return new Result(Content.COMMIT, request);
    }


    /**
     * Creates a Source Result, which will consist of only the submitted source code
     * object and no error message.
     *
     * @param request The Source
     * @return a new source Result
     */
    public static Result sourcePackage(Source request){
        return new Result(Content.SOURCE, request);
    }

    /**
     * Creates a nothing-to-report Result.
     *
     * @return a new nothing-to-report Result
     */
    public static Result nothing(){
        return infoPackage("");
    }

    /**
     * Creates an Info Result, which will consist of only the submitted info
     * object and no error message.
     *
     * @param request The Info message
     * @return a new info Result
     */
    public static Result infoPackage(String request){
        return new Result(Content.INFO, request);
    }

    /**
     * Creates a Issues List Result.
     *
     * @param issues THe list of detected issues.
     * @return a new issues list Result
     */
    public static Result issuesListPackage(List<Issue> issues){
        return new Result(Content.ISSUES, issues);
    }

    /**
     * @return {@code true} if this is failed result package, false otherwise.
     */
    public boolean isError(){
        return type.isSame(Content.ERROR);
    }

    /**
     * @return {@code true} if this is an info result package, false otherwise.
     */
    public boolean isInfo(){
        return type.isSame(Content.INFO);
    }

    /**
     * @return {@code true} if this is a 'list of issues' result package, false otherwise.
     */
    public boolean isIssuesList(){
        return type.isSame(Content.ISSUES);
    }


    /**
     * @return {@code true} if this is a commit request result package, false otherwise.
     */
    public boolean isCommitRequest(){
        return type.isSame(Content.COMMIT);
    }


    /**
     * @return {@code true} if this is a Source result package, false otherwise.
     */
    public boolean isSource(){
        return type.isSame(Content.SOURCE);
    }

    /**
     * @return The error message of a failed Result, null if it is not a failed
     *      Result.
     */
    public String getErrorMessage(){
        return isError() ? String.class.cast(data) : null;
    }

    /**
     * @return The CommitRequest of a success Result, null if it is not a success
     *      Result.
     */
    public CommitRequest getCommitRequest(){
        return isCommitRequest() ? CommitRequest.class.cast(data) : null;
    }

    /**
     * @return The Source file stored in Result, null if it is not a source
     *      Result.
     */
    public Source getSource(){
        return isSource() ? Source.class.cast(data) : null;
    }

    /**
     * @return The list of detected issues in the Source file.
     */
    public List<Issue> getIssuesList(){
        //noinspection unchecked
        return isIssuesList()
                ? Collections.unmodifiableList(((List<Issue>)data)) // unchecked warning
                : null;
    }

    /**
     * @return The message stored in Result, null if it is not a message
     *      Result.
     */
    public String getInfo(){
        return (isInfo()) ? String.class.cast(data) : null;
    }

    @Override public String toString() {
        return type + " " + (isError()
                ? getErrorMessage() :
                (isInfo() ?
                        getInfo()
                        : (isCommitRequest()
                             ? getCommitRequest().more()
                             : getIssuesList()
                          )
                )
        );
    }

    /**
     * The type of content to be stored.
     */
    enum Content {
        /** Info Content **/
        INFO,
        /** List of detected issues **/
        ISSUES,
        /** Error Content **/
        ERROR,
        /** Commit Request Content **/
        COMMIT,
        /** Source file **/
        SOURCE;

        boolean isSame(Content that){
            return this == that;
        }
    }
}