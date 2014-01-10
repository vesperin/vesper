package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.spi.Upstream;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Host {
    /**
     * Records an exception, the full details of which will be logged,
     * and the message of which will be presented to the user at a later
     * time. If your Module calls something that you worry may fail, you
     * should catch the exception and pass it into this.
     */
    void addError(Throwable t);

    /**
     * Records an error message which will be presented to the user at a
     * later time. Unlike throwing an exception, this enable us to continue
     * configuring the Refactorer and discover more errors. Uses {@link
     * String#format(String, Object[])} to insert the arguments into the
     * message.
     */
    void addError(String message, Object... arguments);

    /**
     * Adds a {@link JavaParser} implementation.
     *
     * @param parser A {@link JavaParser} implementation.
     */
    void addJavaParser(JavaParser parser);

    /**
     * Adds a {@link IssueDetector} implementation.
     *
     * @param detector A {@link IssueDetector} implementation.
     */
    void addIssueDetector(IssueDetector detector);

    /**
     * Adds a {@link SourceChanger} implementation.
     *
     * @param changer A {@link SourceChanger} implementation.
     */
    void addSourceChanger(SourceChanger changer);

    /**
     * User credentials to stored the refactored {@code Source}.
     *
     * @param credential The access credentials
     */
    void addCredentials(Credential credential);

    /**
     * @return The commit destination.
     */
    Upstream getUpstream();

    /**
     * Creates a new Java context for the source file.  This
     * process includes the parsing of the created Java context.
     *
     * @param source The source file.
     * @return a new Java context.
     */
    Context createContext(Source source);

    /**
     * Returns only available issue detectors.
     *
     * @return The list of available issue detectors.
     */
    List<IssueDetector> getIssueDetectors();


    /**
     * Returns only available {@code Source} changers.
     *
     * @return The list of available source changers.
     */
    List<SourceChanger> getSourceChangers();


    /**
     * Gets the Java parser required for constructing the {@code Refactorer}.
     *
     * @return The {@link JavaParser}.
     */
    JavaParser getJavaParser();

    /**
     * Installs a configuration that automatically configures this host.
     */
    void install(Configuration configuration);

    /**
     * @return {@code true} if changes will be
     *      committed locally, {@code false} otherwise (i.e., remotely)
     */
    boolean isCommittedLocally();

    /**
     * Sets offline mode.
     *
     * @param mode {@code true} if changes should be
     *             committed locally. {@code false} if changes will be committed remotely.
     */
    void commitLocally(boolean mode);

    /**
     * Throws a {@code CreationException} if any exception has been
     * thrown and caught during the creation of the {@code Refactorer}.
     *
     * @throws CreationException if the {@code Refactorer} could not be created.
     */
    void throwCreationErrorIfErrorsExist() throws CreationException;
}
