package edu.ucsc.refactor.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.Vesper;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.util.Commit;
import io.airlift.airline.Inject;

import java.io.IOException;
import java.util.logging.Logger;

import static com.google.common.base.Objects.firstNonNull;

/**
 * {@code Vesper CLI}'s command.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class VesperCommand {
    @Inject
    public GlobalOptions globalOptions = new GlobalOptions();

    static Logger logger = null;

    @VisibleForTesting
    public Result result = null;

    public boolean ask(String question, boolean defaultValue) {
        return AskQuestion.ask(question, defaultValue);
    }

    public Result call(Environment environment) throws RuntimeException {
        try {
            initializeLogging(globalOptions.verbose);
            result = execute(environment);
        } catch (Throwable ex){
            if(globalOptions.verbose){
                throw new RuntimeException(ex);
            } else {
                return Results.errorResult(firstNonNull(ex.getMessage(), "Unknown error"));
            }
        }

        return firstNonNull(result, Results.unit());
    }


    public static void initializeLogging(boolean debug) throws IOException {
        if (debug) {
            logger = Logger.getLogger(Vesper.class.getPackage().getName());
        }
    }

    protected Commit commitChange(Environment environment, ChangeRequest request){
        return environment.perform(request);
    }

    protected static void ensureValidState(Environment environment){
        Preconditions.checkNotNull(environment, "No environment available");
        Preconditions.checkNotNull(environment.getOrigin(), "No source code available");
        Preconditions.checkNotNull(environment.getCodeRefactorer(), "No refactorer available");
    }


    protected Result createResultPackage(Commit applied){
        return (globalOptions.verbose
                ? Results.commitSummaryInfo(
                    "commit summary for " + applied.getCommitSummary().getMessage(),
                    applied.getCommitSummary()
                  )
                : Results.unit()
        );
    }

    protected SourceSelection createSelection(Environment environment, String head){
        final Iterable<String> rangeSplit = Splitter.on(',').split(head);

        final int start = Integer.valueOf(rangeSplit.iterator().next());
        final int end   = Integer.valueOf(Iterables.getLast(rangeSplit));

        return new SourceSelection(environment.getOrigin(), start, end);
    }


    public abstract Result execute(Environment environment) throws Exception;
}
