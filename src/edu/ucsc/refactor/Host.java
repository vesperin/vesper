package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.SourceChanger;

import java.lang.RuntimeException;
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
     * User credentials to stored the refactored {@code Source}, null if all changes will
     * be committed locally; otherwise the user must provide a Gist's user name and password.
     *
     * @param credential The access credentials
     */
    void addCredentials(Credential credential);

    /**
     * Creates a new Java context for the source file.  This
     * process includes the parsing of the created Java context.
     *
     * Any errors that occurred during the parsing of the Source will be
     * persisted rather than thrown. If one wants to manage how to handle
     * these exceptions, then use {@link #createContext(Source)} instead.
     *
     * @param source The source file.
     * @return a new Java context.
     */
    Context silentlyCreateContext(Source source);

    /**
     * Creates a new Java context for the source file.  This
     * process includes the parsing of the created Java context.
     *
     * @param source The source file.
     * @return a new Java context.
     * @throws RuntimeException if there is a compilation error.
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
     * @return {@code true} if {@code Vesper} is set to allow remote commits,
     * {@code false} otherwise.
     */
    boolean isRemoteUpstreamEnabled();

    /**
     * Throws a {@code CreationException} if any exception has been
     * thrown and caught during the creation of the {@code Refactorer}.
     *
     * @throws CreationException if the {@code Refactorer} could not be created.
     */
    void throwCreationErrorIfErrorsExist() throws CreationException;
}
