package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.*;
import edu.ucsc.refactor.util.ToStringBuilder;

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
        return new ToStringBuilder("Configuration")
               .add("host", this.host)
               .toString();
    }
}
