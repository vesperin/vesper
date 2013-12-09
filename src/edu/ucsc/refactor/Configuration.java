package edu.ucsc.refactor;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Configuration {
    /**
     * A Configuration configures a Host, which typically includes issue finders and fixers,
     * which will be used to create a {@link Refactorer}.
     *
     * A hanko-based application is ultimately composed of ONE {@code Configuration}.
     *
     * @param host The {@code Host}
     */
    void configure(Host host);
}
