package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.UnitLocator;
import edu.ucsc.refactor.util.CommitHistory;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Environment {
    final AtomicReference<Refactorer>       refactorer;
    final AtomicReference<Source>           origin;
    final AtomicReference<Configuration>    remoteConfig;
    final Queue<CommitRequest>              checkpoints;

    /**
     * Constructs a new Interpreter's Environment.
     */
    public Environment(){
        refactorer      = new AtomicReference<Refactorer>();
        origin          = new AtomicReference<Source>();
        remoteConfig    = new AtomicReference<Configuration>();
        checkpoints     = new LinkedList<CommitRequest>();
    }


    /**
     * Clears the environment.
     */
    public void clear() {
        this.refactorer.set(null);
        this.origin.set(null);
        this.remoteConfig.set(null);
        this.checkpoints.clear();
    }


    /**
     * Enable remote commits by providing a credential.
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
     * @return the tracked {@code Source}.
     */
    public Source getTrackedSource() {
        return origin.get();
    }

    /**
     * @return The Unit locator
     */
    public UnitLocator getCodeLocator(){
        return getCodeRefactorer().getLocator(getTrackedSource());
    }

    /**
     * @return the commit history of a tracked {@code Source}.
     */
    public CommitHistory getCommitHistory(){
        return (getCodeRefactorer() == null
                ? new CommitHistory()
                : getCodeRefactorer().getHistory(getTrackedSource())
        );
    }


    /**
     * @return the {@code Refactorer} for the tracked {@code Source}
     */
    public Refactorer getCodeRefactorer() {
        return refactorer.get();
    }

    /**
     * @return a {@code Queue} of committed requests.
     */
    public Queue<CommitRequest> getCommittedRequests(){
        return checkpoints;
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
                    ? Vesper.createRefactorer(getTrackedSource())
                    : Vesper.createRefactorer(remote, getTrackedSource());

            this.refactorer.set(refactorer);
        } else {
            this.refactorer.set(null);
        }
    }

    /**
     * @return the unit result.
     */
    public Result unit(){
        return Result.unit();
    }

    /**
     * Collects commit requests for later publishing.
     *
     * @param request The commit request to be collected.
     */
    public void collect(CommitRequest request){
        checkpoints.add(request);
    }

    /**
     * Updates the origin or tracked {@code Source}.
     *
     * @param updatedSource The new version of a {@code Source}
     */
    public void update(Source updatedSource) {
        // if origin != null, then refactorer.source === origin
        this.origin.set(updatedSource);
        assert isTracked(getTrackedSource());
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
        resetSource(getTrackedSource().getName());
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

                final boolean isOrigin = each.equals(getTrackedSource());

                final Source to         = getCodeRefactorer().rewind(each);
                final Source indexed    = getCodeRefactorer().rewrite(to);

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
