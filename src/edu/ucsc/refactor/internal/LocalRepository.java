package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.Upstream;

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

    @Override public CommitRequest publish(CommitRequest request) {
        // do not publish anything, just return its current status.
        return request;
    }

    public Credential getCredential() {
        return this.key;
    }

    @Override public String toString() {
        return Objects.toStringHelper("LocalRepository")
                .add("credential", getCredential())
                .toString();
    }
}
