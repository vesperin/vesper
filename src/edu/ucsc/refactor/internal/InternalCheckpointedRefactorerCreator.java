package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Refactorer;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.util.StopWatch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class InternalCheckpointedRefactorerCreator {
    private final List<Source> sources;
    private final Refactorer refactorer;

    private final StopWatch stopwatch = new StopWatch();

    public InternalCheckpointedRefactorerCreator(Refactorer refactorer){
        this.refactorer = refactorer;
        this.sources    = new ArrayList<Source>();
    }

    /**
     * Adds a list of sources from which it will build a new refactorer object.
     *
     * @param sources The source objects.
     * @return self
     */
    public InternalCheckpointedRefactorerCreator addSources(Iterable<Source> sources) {
        for (Source source : sources) {
            this.sources.add(source);
        }

        return this;
    }

    public NavigableJavaRefactorer build(){

        final NavigableJavaRefactorer checkpointedJavaRefactorer = new NavigableJavaRefactorer(refactorer);

        for(Source src : sources){
            // detect issues in source file
            checkpointedJavaRefactorer.detectIssues(src);
        }

        stopwatch.resetAndLog("Collecting issue detection requests");

        return checkpointedJavaRefactorer;
    }
}
