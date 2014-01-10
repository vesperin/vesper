package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Credential;
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
        this.key = key;
    }

    @Override public String getUser() {
        return this.key.getUsername();
    }

    @Override public Object get() {
        return null;
    }
}
