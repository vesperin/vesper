package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitStatus;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Result {
    private final Map<Content, List<Object>> storage;

    /**
     * Construct a {@code Result}.
     * @param key The type of content
     * @param value The content's value
     */
    private Result(Content key, Object value){
        this.storage = createMap(key);
        if(value != null){
            add(value);
        }
    }

    private static Result createResult(Content content, Object value){
        return new Result(content, value);
    }


    private static Map<Content, List<Object>> createMap(Content content){
        return ImmutableMap.<Content, List<Object>> builder()
                .put(content, Lists.newArrayList())
                .build();
    }

    /**
     * Creates an empty {@code Result} matching its content type.
     *
     * @param content the type of content to be stored in this package.
     * @return a new Result object.
     */
    public static Result empty(Content content){
        return createResult(content, null);
    }


    /**
     * Creates a Failed Result, which will consist of only the error message and
     * no commit request.
     *
     * @param message The error message.
     * @return a new failed Result
     */
    public static Result failedPackage(String message){
        return createResult(Content.ERROR, message);
    }


    /**
     * Creates a Success Result, which will consist of only the submitted commit status
     * object and no error message.
     *
     * @param status The commit status
     * @return a new failed Result
     */
    public static Result committedPackage(CommitStatus status){
        return createResult(Content.COMMIT, status);
    }


    /**
     * Creates a Source Result, which will consist of only the submitted source code
     * object and no error message.
     *
     * @param request The Source
     * @return a new source Result
     */
    public static Result sourcePackage(Source request){
        return createResult(Content.SOURCE, request);
    }


    /**
     * Creates an Info Result, which will consist of only the submitted info
     * object and no error message.
     *
     * @param request The Info message
     * @return a new info Result
     */
    public static Result infoPackage(String request){
        return createResult(Content.INFO, request);
    }


    /**
     * Creates a Issues List Result.
     *
     * @param issues THe list of detected issues.
     * @return a new issues list Result
     */
    public static Result issuesListPackage(List<Issue> issues){
        return createResult(Content.ISSUES, issues);
    }


    /**
     * Creates a nothing-to-report Result.
     *
     * @return a new nothing-to-report Result
     */
    public static Result unit(){
        return infoPackage("()");
    }


    /**
     * Adds content to the package.
     *
     * @param value the Content's value.
     * @throws NullPointerException if <tt>key</tt> or <tt>value</tt> are <tt>null</tt>
     */
    public final void add(Object value) {
        Preconditions.checkNotNull(value, "called add() with a null content value" );
        getValue().add(value);
    }


    /**
     * Removes a content from the package.
     * @param key the key to be deleted.
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     */
    public List<Object> delete(Content key) {
        Preconditions.checkNotNull(key, "called delete() with a null key");

        return storage.remove(key);
    }

    /**
     * @return The current content type.
     */
    public Content getKey(){
        Preconditions.checkState(storage.size() == 1, "Result is supposed to be a singleton container.");
        return Preconditions.checkNotNull(Iterables.getFirst(storage.keySet(), null), "getKey() return null");

    }

    /**
     * Gets the value of this content.
     *
     * @return The value stored for this content.
     */
    public List<Object> getValue(){
        return storage.get(getKey());
    }


    /**
     * @return {@code true} if this is failed result package, false otherwise.
     */
    public boolean isError(){
        return storage.containsKey(Content.ERROR);
    }

    /**
     * @return {@code true} if this is an info result package, false otherwise.
     */
    public boolean isInfo(){
        return storage.containsKey(Content.INFO);
    }

    /**
     * @return {@code true} if this is a 'list of issues' result package, false otherwise.
     */
    public boolean isIssuesList(){
        return storage.containsKey(Content.ISSUES);
    }


    /**
     * @return {@code true} if this is a commit request result package, false otherwise.
     */
    public boolean isCommit(){
        return storage.containsKey(Content.COMMIT);
    }


    /**
     * @return {@code true} if this is a Source result package, false otherwise.
     */
    public boolean isSource(){
        return storage.containsKey(Content.SOURCE);
    }


    @Override public String toString() {
        return storage.toString();
    }

    /**
     * The type of content to be stored.
     */
    public enum Content {
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