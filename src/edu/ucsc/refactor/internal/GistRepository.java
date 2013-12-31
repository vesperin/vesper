package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.spi.Upstream;
import org.eclipse.egit.github.core.service.GistService;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class GistRepository implements Upstream {
    private final GistService service;
    private final Credential  credential;

    /**
     * Construct a new Gist service provider.
     *
     * @param key The storage service key
     */
    public GistRepository(Credential key){
        GistService service = new GistService();
        service.getClient().setCredentials(key.getUsername(), key.getPassword());
        this.service    = service;
        this.credential = key;
    }

    Credential getCredential(){
        return credential;
    }

    @Override public String getUser() {
        return getCredential().getUsername();
    }

    @Override public GistService get() {
        return service;
    }
}