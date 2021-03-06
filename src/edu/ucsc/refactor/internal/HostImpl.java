package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import edu.ucsc.refactor.spi.SourceChanger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class HostImpl implements Host {

    private final List<Throwable>       errors;
    private final List<IssueDetector>   detectors;
    private final List<SourceChanger>   changers;

    private JavaSnippetParser parser;
    private Credential credential;

    /**
     * Creates a new {@code HostImpl}
     */
    public HostImpl(){
        this.errors         = new ArrayList<Throwable>();
        this.detectors      = new ArrayList<IssueDetector>();
        this.changers       = new ArrayList<SourceChanger>();
        this.parser         = null;
        this.credential     = null;
    }

    @Override public void addError(Throwable t) {
        errors.add(t);
    }

    @Override public void addError(String message, Object... arguments) {
        addError(new Throwable(String.format(message, arguments)));
    }

    @Override public void addJavaParser(JavaSnippetParser parser) {
        this.parser = parser;
    }

    @Override public void addIssueDetector(IssueDetector detector) {
        if(detector == null) return;
        if(!detectors.contains(detector)){
            detectors.add(detector);
        }
    }

    @Override public void addSourceChanger(SourceChanger changer) {
        if(changer == null) return;
        if(!changers.contains(changer)){
            changers.add(changer);
        }
    }

    @Override public void addCredentials(Credential credential) {
        this.credential = credential;
    }

    // Internal method
    final Credential getStorageKey(){
        return credential;
    }


    @Override public Context createContext(Source source) {
        if(source.getUniqueSignature() == null){  // very important...
            source.generateUniqueSignature();
        }

        return parseJava(new Context(source));
    }

    /**
     * Convenience method that parses a context and then returns it for
     * further used by this method's caller.
     *
     * @param context The {@link Context} to be parsed.
     * @return a parsed {@link Context} object.
     */
    private Context parseJava(Context context){
        final JavaParser parser = getJavaParser();

        if(parser == null) {
            throw new IllegalStateException();
        }

        if(parser.parseJava(context) == null){
            throw new IllegalStateException("Unable to parse source file");
        }

        return context;
    }

    @Override public List<IssueDetector> getIssueDetectors() {
        return Collections.unmodifiableList(detectors);
    }

    @Override public List<SourceChanger> getSourceChangers() {
        return Collections.unmodifiableList(changers);
    }

    @Override public JavaSnippetParser getJavaParser() {
        return parser;
    }

    @Override public void install(Configuration configuration) {
        configuration.configure(this);
    }

    @Override public void throwCreationErrorIfErrorsExist() throws RuntimeException {
        try {
            // Blow up if we encountered errors.
            if (!errors.isEmpty()) {
                throw new CreationException(errors);
            }
        } finally {
            errors.clear();
        }
    }

    @Override public String toString() {
        final Objects.ToStringHelper builder = Objects.toStringHelper("Host");
        builder.add("detectors", detectors.size());
        builder.add("changers", changers.size());
        builder.add("parser", (getJavaParser() != null ? "Yes" : "No"));
        return builder.toString();
    }
}
