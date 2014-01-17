package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.spi.CommitRequest;

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

    public Environment(){
        refactorer      = new AtomicReference<Refactorer>();
        origin          = new AtomicReference<Source>();
        remoteConfig    = new AtomicReference<Configuration>();
        checkpoints     = new LinkedList<CommitRequest>();
    }


    public static Result unit(){
        return Result.nothing();
    }

    public boolean containsOrigin() {
        return this.origin.get() != null;
    }

    public void setOrigin(Source origin) {
        updateOrigin(origin);

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

    public Source getOrigin() {
        return origin.get();
    }

    public boolean setCredential(final String username, final String password) {
        return !(Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password))
                && setCredential(new Credential(username, password));

    }

    public boolean setCredential(final Credential credential){
        remoteConfig.set(new AbstractConfiguration() {
            @Override protected void configure() {
                installDefaultSettings();
                addCredentials(credential);
            }
        });

        return true;
    }

    public Refactorer getRefactorer() {
        return refactorer.get();
    }

    public void put(CommitRequest request){
        checkpoints.add(request);
    }


    public Queue<CommitRequest> getRequests(){
        return checkpoints;
    }

    public void clears() {
        setCredential(null);
        setOrigin(null);
    }

    public void updateOrigin(Source updatedSource) {
        // if origin != null, then refactorer.source === origin
        this.origin.set(updatedSource);
        assert knows(getOrigin());
    }

    /**
     * Checks whether this {@code Refactorer} knows its seed (i.e., the one for which it was
     * created).
     *
     * @param code The The {@code Source}
     * @return {@code true} if it knows, {@code false} otherwise.
     */
    public boolean knows(Source code) {
        for(Source each : getRefactorer().getVisibleSources()){
            if(each.equals(code)){ return true; }
        }

        return false;
    }

    public void reset() {
        reset(getOrigin().getName());
    }

    public Source reset(String name) {
        Preconditions.checkNotNull(name);

        final List<Source> all = getRefactorer().getVisibleSources();

        for(Source each : all){
            if(each.getName().equals(name)){

                final boolean isOrigin = each.equals(getOrigin());

                final Source to         = getRefactorer().rewind(each);
                final Source indexed    = getRefactorer().rewrite(to);

                final boolean isUpdateNeeded = !each.equals(indexed);

                if(isOrigin && isUpdateNeeded){
                    updateOrigin(indexed);
                }

                return each;
            }
        }

        throw new NoSuchElementException("Source with the name = " + name + " was not found.");
    }
}
