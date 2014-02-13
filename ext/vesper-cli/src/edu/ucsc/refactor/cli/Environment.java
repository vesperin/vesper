package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.CommitSummary;
import edu.ucsc.refactor.spi.ProgramUnit;
import edu.ucsc.refactor.spi.UnitLocator;
import edu.ucsc.refactor.util.CommitHistory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Environment {
    final AtomicReference<Refactorer>       refactorer;
    final AtomicReference<Source>           origin;
    final AtomicReference<Configuration>    remoteConfig;
    final Queue<CommitRequest>              checkpoints;
    final Queue<String>                     errors;

    /**
     * Constructs a new Interpreter's Environment.
     */
    public Environment(){
        refactorer      = new AtomicReference<Refactorer>();
        origin          = new AtomicReference<Source>();
        remoteConfig    = new AtomicReference<Configuration>();
        checkpoints     = new LinkedList<CommitRequest>();
        errors          = new LinkedList<String>();
    }

    /**
     * Adds the errors messages returned by an invalid change. After being returned, these
     * message will be deleted.
     *
     * @param messages The logged errors messages
     */
    public void addError(List<String> messages) {
        final StringBuilder whole = new StringBuilder(messages.size() * 1000);

        final Iterator<String> iterator = messages.iterator();
        while(iterator.hasNext()){
            whole.append(iterator.next());
            if(iterator.hasNext()){
                whole.append(". ");
            }
        }

        this.errors.add(whole.toString().trim());
    }


    /**
     * Enable remote commits by providing a credential.
     *
     * @param username the username
     * @param password the password
     * @return {@code true} if the upstream access was enabled, {@code false} otherwise.
     */
    public boolean enableUpstream(final String username, final String password) {
        return !(Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password))
                && enableUpstream(new Credential(username, password));

    }

    /**
     * Enable remote commits by providing a credential.
     *
     * @param credential Access credential to a remote repository.
     * @return {@code true} if the upstream access was enabled, {@code false} otherwise.
     */
    public boolean enableUpstream(final Credential credential){
        Preconditions.checkNotNull(credential, "invalid credential");
        remoteConfig.set(new AbstractConfiguration() {
            @Override protected void configure() {
                installDefaultSettings();
                addCredentials(credential);
            }
        });

        return true;
    }


    /**
     * @return a recently caught error message.
     */
    public String getErrorMessage(){
        return errors.remove();
    }

    /**
     * @return the tracked {@code Source}.
     */
    public Source getOrigin() {
        return origin.get();
    }

    /**
     * Un-tracked a given Source, which may be the base Source.
     *
     * @param code The Source
     */
    public void untrack(Source code){
        if(code.equals(getOrigin())){
            restart();
        }
    }


    /**
     * Perform a ChangeRequest and then returns the committed request.
     *
     * @param request The ChangeRequest to be performed.
     * @return the committed request.
     */
    public CommitRequest perform(ChangeRequest request){
        final Change        change  = getCodeRefactorer().createChange(request);
        final CommitRequest applied = getCodeRefactorer().apply(change);
        if(applied != null){
            update(applied.getCommitSummary().getSource());
            enqueueCommitRequest(applied);
        } else {
            addError(change.getErrors());
        }
        return applied;
    }

    /**
     * Re-starts the environment.
     */
    public void restart() {
        this.refactorer.set(null);
        this.origin.set(null);
        this.remoteConfig.set(null);
        this.checkpoints.clear();
    }

    /**
     * Lookup for a given program unit.
     *
     * @param unit The program unit to be searched.
     * @return The locations where the program unit was found.
     */
    public List<NamedLocation> lookup(ProgramUnit unit){
        return getCodeLocator().locate(unit);
    }

    /**
     * @return The Unit locator
     */
    public UnitLocator getCodeLocator(){
        return getCodeRefactorer().getLocator(getOrigin());
    }

    /**
     * @return the commit history of a tracked {@code Source}.
     */
    public CommitHistory getCommitHistory(){
        return (getCodeRefactorer() == null
                ? new CommitHistory()
                : getCodeRefactorer().getCommitHistory(getOrigin())
        );
    }

    /**
     * Get all the detected issues in the tracked Source
     *
     * @return The list of issues.
     */
    public List<Issue> getIssues(){
        return getCodeRefactorer().getIssues(getOrigin());
    }

    /**
     * Publishes a commit request.
     *
     * @param commitRequest The commit request to be published to a remote upstream.
     * @return The updated commit summary.
     */
    public CommitSummary publish(CommitRequest commitRequest){
        return getCodeRefactorer().publish(commitRequest).getCommitSummary();
    }


    /**
     * @return the {@code Refactorer} for the tracked {@code Source}
     */
    public Refactorer getCodeRefactorer() {
        return refactorer.get();
    }

    /**
     * @return {@code true} if there are requests to be published, {@code false} otherwise.
     */
    public boolean isFilledWithRequests(){
        return !getCommittedRequests().isEmpty();
    }


    /**
     * @return a {@code Queue} of committed requests.
     */
    public Queue<CommitRequest> getCommittedRequests(){
        return checkpoints;
    }


    /**
     * @return {@code true} if there is logged {@code error}.
     */
    public boolean isErrorFree() {
        return !this.errors.isEmpty();
    }


    /**
     * @return {@code true} if the origin {@code Source} has been set.
     */
    public boolean isSourceTracked() {
        return this.origin.get() != null;
    }

    /**
     * Tracks the {@code Source} origin, which in turn will be used to initialize a
     * {@code Refactorer}.
     *
     * @param origin the {@code Source} origin.
     */
    public void track(Source origin) {
        update(origin);

        if(origin != null){
            final Configuration remote      = remoteConfig.get();
            final Refactorer    refactorer  = remote == null
                    ? Vesper.createRefactorer(getOrigin())
                    : Vesper.createRefactorer(remote, getOrigin());

            this.refactorer.set(refactorer);
        } else {
            this.refactorer.set(null);
        }
    }

    /**
     * Collects commit requests for later publishing.
     *
     * @param request The commit request to be collected.
     */
    public void enqueueCommitRequest(CommitRequest request){
        checkpoints.add(request);
    }

    /**
     * @return the de-queued CommitRequest.
     */
    public CommitRequest dequeueCommitRequest(){
        return getCommittedRequests().remove();
    }

    /**
     * Updates the origin or tracked {@code Source}.
     *
     * @param updatedSource The new version of a {@code Source}
     */
    public void update(Source updatedSource) {
        // if origin != null, then refactorer.source === origin
        this.origin.set(updatedSource);
        assert isTracked(getOrigin());
    }

    /**
     * Checks whether this {@code Refactorer} knows its seed (i.e., the one for which it was
     * created).
     *
     * @param code The The {@code Source}
     * @return {@code true} if it knows, {@code false} otherwise.
     */
    public boolean isTracked(Source code) {
        for(Source each : getCodeRefactorer().getTrackedSources()){
            if(each.equals(code)){ return true; }
        }

        return false;
    }

    /**
     * Reset current Source to its first version.
     */
    public void reset() {
        resetSource(getOrigin().getName());
    }

    /**
     * Reset the Source matching the given {@code name} to its first version.
     *
     * @param name the name of the Source
     * @return the {@code Source}'s first version.
     */
    public Source resetSource(String name) {
        Preconditions.checkNotNull(name);

        final List<Source> all = getCodeRefactorer().getTrackedSources();

        for(Source each : all){
            if(each.getName().equals(name)){

                final boolean isOrigin  = each.equals(getOrigin());

                final Source to         = getCodeRefactorer().regress(each);
                final Source indexed    = getCodeRefactorer().rewriteHistory(to);

                final boolean isUpdateNeeded = !each.equals(indexed);

                if(isOrigin && isUpdateNeeded){
                    update(indexed);
                }

                return each;
            }
        }

        throw new NoSuchElementException("Source with the name = " + name + " was not found.");
    }
}
