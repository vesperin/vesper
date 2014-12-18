package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.ProgramUnit;
import edu.ucsc.refactor.UnitLocator;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;
import edu.ucsc.refactor.util.CommitPublisher;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Environment {
    final AtomicReference<NavigableRefactorer>      refactorer;
    final AtomicReference<Source>                   origin;
    final AtomicReference<Configuration>            remoteConfig;
    final Queue<String>                             errors;

    /**
     * Constructs a new Interpreter's Environment.
     */
    public Environment(){
        refactorer      = new AtomicReference<NavigableRefactorer>();
        origin          = new AtomicReference<Source>();
        remoteConfig    = new AtomicReference<Configuration>();
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
     * @return The list of pushed commits. These commits can now be
     * flushed out of the {@code Refactorer}'s commit history
     */
    public List<Commit> publishCommitHistory(){
        return getCommitPublisher().publish();
    }


    /**
     * Perform a ChangeRequest and then returns the committed request.
     *
     * @param request The ChangeRequest to be performed.
     * @return the committed request.
     */
    public Commit perform(ChangeRequest request){
        final Change change  = getCodeRefactorer().createChange(request);
        final Commit applied = getCodeRefactorer().apply(change);
        if(applied != null){
            update(applied.getSourceAfterChange());
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
     * Deletes a commit from a commit history.
     *
     * @param commit The commit to be deleted.
     * @return {@code true} if commit was deleted, {@code false} otherwise.
     */
    public boolean forgetCommit(Commit commit) {
        return getCodeRefactorer().getCommitHistory(getOrigin()).delete(commit);
    }

    /**
     * @return The Unit locator
     */
    public UnitLocator getCodeLocator(){
        return Vesper.createUnitLocator(getOrigin());
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
     * @return The Commit Publisher
     */
    public CommitPublisher getCommitPublisher() {
        return getCodeRefactorer().getCommitPublisher(getOrigin());
    }


    /**
     * @return the {@code Refactorer} for the tracked {@code Source}
     */
    public NavigableRefactorer getCodeRefactorer() {
        return refactorer.get();
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
                    ? Vesper.createRefactorer()
                    : Vesper.createRefactorer(remote);

            this.refactorer.set(NavigableVesper.createNavigableRefactorer(refactorer, getOrigin()));
        } else {
            this.refactorer.set(null);
        }
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
        for(Source each : getCodeRefactorer().getSources()){
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

        final List<Source> all = getCodeRefactorer().getSources();

        for(Source each : all){
            if(each.getName().equals(name)){
                final boolean isOrigin  = each.equals(getOrigin());

                Source result =  each;

                final int size = getCommitHistory().size();

                for(int idx = 0; idx < size; idx++){
                    final Source to         = getCodeRefactorer().previous(result);
                    final Source indexed    = getCodeRefactorer().rewriteHistory(to);

                    final boolean isUpdateNeeded = !each.equals(indexed);

                    if(isOrigin && isUpdateNeeded){
                        update(indexed);
                        result =  indexed;
                    }
                }

                return result;
            }
        }

        throw new NoSuchElementException("Source with the name = " + name + " was not found.");
    }
}
