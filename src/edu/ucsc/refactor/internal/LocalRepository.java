package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.CommitStatus;
import edu.ucsc.refactor.spi.Upstream;
import edu.ucsc.refactor.util.ToStringBuilder;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class LocalRepository implements Upstream {
    private final Credential key;

    /**
     * Construct a new Gist service provider.
     *
     * @param key The storage service key
     */
    public LocalRepository(Credential key){
        this.key = getCredential(key, Credential.none());
    }

    private static Credential getCredential(Credential key, Credential defaultValue){
        return key == null ? defaultValue : key;
    }

    @Override public CommitStatus publish(CommitRequest request) {
        // do not publish anything, just return its current status.
        return request.getStatus();
    }

    public Credential getCredential() {
        return this.key;
    }

    @Override public String toString() {
        return new ToStringBuilder("LocalRepository")
                .add("credential", getCredential())
                .toString();
    }
}
