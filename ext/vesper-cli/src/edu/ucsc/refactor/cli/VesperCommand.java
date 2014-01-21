package edu.ucsc.refactor.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.Vesper;
import edu.ucsc.refactor.spi.CommitRequest;
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
                return Result.failedPackage(firstNonNull(ex.getMessage(), "Unknown error"));
            }
        }

        return firstNonNull(result, Result.nothing());
    }


    public static void initializeLogging(boolean debug) throws IOException {
        if (debug) {
            logger = Logger.getLogger(Vesper.class.getPackage().getName());
        }
    }

    protected CommitRequest commitChange(Environment environment, ChangeRequest request){
        final CommitRequest applied = environment.getRefactorer().apply(environment.getRefactorer().createChange(request));
        environment.updateOrigin(applied.getSource());
        return applied;
    }

    protected static void ensureValidState(Environment environment){
        Preconditions.checkNotNull(environment, "No environment available");
        Preconditions.checkNotNull(environment.getOrigin(), "No source code available");
        Preconditions.checkNotNull(environment.getRefactorer(), "No refactorer available");
    }


    protected Result createResultPackage(CommitRequest applied, String message){
        if(applied == null){
            return Result.failedPackage(message);
        }

        return Result.committedPackage(applied.getStatus());
    }

    protected SourceSelection createSelection(Environment environment, String head){
        final Iterable<String> rangeSplit = Splitter.on(',').split(head);

        final int start = Integer.valueOf(rangeSplit.iterator().next());
        final int end   = Integer.valueOf(Iterables.getLast(rangeSplit));

        return new SourceSelection(environment.getOrigin(), start, end);
    }


    public abstract Result execute(Environment environment) throws Exception;
}
