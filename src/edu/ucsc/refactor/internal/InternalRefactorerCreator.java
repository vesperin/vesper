package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Host;
import edu.ucsc.refactor.Refactorer;
import edu.ucsc.refactor.util.StopWatch;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class InternalRefactorerCreator {
    private final Host host;

    private final StopWatch stopwatch = new StopWatch();

    /**
     * Instantiates a new {@link InternalRefactorerCreator}.
     *
     * @param host The configured host.
     */
    public InternalRefactorerCreator(Host host){
        this.host           = host;
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

        host.throwCreationErrorIfErrorsExist();
        stopwatch.resetAndLog("Verifying no errors exist");

        return refactorer;
    }
}
