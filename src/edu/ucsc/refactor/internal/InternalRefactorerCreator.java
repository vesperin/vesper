package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class InternalRefactorerCreator {
    private final List<Source>  sources;
    private final Host host;

    private final StopWatch stopwatch = new StopWatch();

    /**
     * Instantiates a new {@link InternalRefactorerCreator}.
     *
     * @param host The configured host.
     */
    public InternalRefactorerCreator(Host host){
        this.host           = host;
        this.sources        = new ArrayList<Source>();
    }

    /**
     * Adds a list of sources from which it will build a new refactorer object.
     *
     * @param sources The source objects.
     * @return self
     */
    public InternalRefactorerCreator addSources(Iterable<Source> sources) {
        for (Source source : sources) {
            this.sources.add(source);
        }

        return this;
    }

    /**
     * @return a new {@link edu.ucsc.refactor.Refactorer}.
     */
    public Refactorer build(){
        final Refactorer refactorer = new JavaRefactorer(host);
        stopwatch.resetAndLog("Refactorer construction");

        final int detectors = host.getIssueDetectors().size();
        final int solvers   = host.getSourceChangers().size();

        if(detectors == 0 && solvers == 0) host.addError(
                "Zero detectors AND zero changers is not allowed!",
                detectors,
                solvers
        );

        stopwatch.resetAndLog("Checking issue detector-solver correspondence.");

        for(Source src : sources){
            // detect issues in source file
            refactorer.detectIssues(src);
        }

        stopwatch.resetAndLog("Collecting issue detection requests");

        host.throwCreationErrorIfErrorsExist();
        stopwatch.resetAndLog("Verifying no errors exist");

        return refactorer;
    }
}
