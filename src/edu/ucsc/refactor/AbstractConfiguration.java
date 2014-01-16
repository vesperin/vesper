package edu.ucsc.refactor;

import com.google.common.base.Objects;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.changers.*;
import edu.ucsc.refactor.internal.detectors.MagicNumber;
import edu.ucsc.refactor.internal.detectors.UnusedImports;
import edu.ucsc.refactor.internal.detectors.UnusedMethods;
import edu.ucsc.refactor.internal.detectors.UnusedParameters;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.SourceChanger;

/**
 * A support class for {@link Configuration} which reduces repetition and results in
 * a more readable configuration. Simply extend this class, implement {@link
 * #configure()}, and call the inherited methods which mirror those found in
 * {@link Host}. For example:
 *
 * <pre>
 *
 * public class MyConfiguration extends AbstractConfiguration {
 *   protected void configure() {
 *     addJavaParser(new MyJavaParser());
 *     addIssueDetector(new MyDetector());
 *     ...and many more
 *     addSourceChanger(new MySourceAmender());
 *     ...and many more
 *   }
 * }
 * </pre>
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class AbstractConfiguration implements Configuration {
    protected Host host;

    /**
     * Configures a {@link Host} via the exposed methods.
     */
    protected abstract void configure();


    @Override public final synchronized void configure(Host host) {
        try {
            if (this.host != null) {
                throw new IllegalStateException("Re-entry is not allowed.");
            }

            if(host == null){
                throw new IllegalArgumentException("configure() was given a null host.");
            }

            this.host = host;

            configure();

        } finally {
            this.host = null;
        }
    }

    /**
     * Installs default settings. Extenders of this class should use it
     * when creating their configuration.
     */
    protected void installDefaultSettings(){
        addJavaParser(new EclipseJavaParser());
        addIssueDetector(new UnusedImports());
        addSourceChanger(new RemoveUnusedImports());
        addIssueDetector(new UnusedMethods());
        addSourceChanger(new RemoveUnusedMethods());
        addIssueDetector(new UnusedParameters());
        addSourceChanger(new RemoveUnusedParameters());
        addIssueDetector(new MagicNumber());
        addSourceChanger(new RemoveMagicNumber());
        addSourceChanger(new ReformatSourceCode());
        addSourceChanger(new RenameMethod());
        addSourceChanger(new RenameParam());
        addSourceChanger(new RenameField());
        addSourceChanger(new RenameClassOrInterface());

        // credentials must be added here..
        addCredentials(null);
    }

    /**
     * @see {@link Host#addCredentials(Credential)}}
     */
    protected void addCredentials(Credential credential){
        this.host.addCredentials(credential);
    }

    /**
     * @see {@link Host#addJavaParser(JavaParser)}
     */
    protected void addJavaParser(JavaParser parser){
        this.host.addJavaParser(parser);
    }

    /**
     * @see {@link Host#addIssueDetector(IssueDetector)}}
     */
    protected void addIssueDetector(IssueDetector detector){
        this.host.addIssueDetector(detector);
    }

    /**
     * @see {@link Host#addSourceChanger(SourceChanger)}
     */
    protected void addSourceChanger(SourceChanger changer){
       this.host.addSourceChanger(changer);
    }


    @Override public String toString() {
        return Objects.toStringHelper("Configuration")
               .add("host", this.host)
               .toString();
    }
}
