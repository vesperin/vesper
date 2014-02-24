package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.util.CommitHistory;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "log", description = "Show commit logs")
public class LogCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws Exception {
        Preconditions.checkNotNull(environment);


        final CommitHistory entire  = environment.getCommitHistory();

        if(entire.isEmpty()){
            return Results.unit();
        }

        return Results.commitHistoryResult(
                String.format("Commit history consisting of %d checkpoints:\n\t\t", entire.size()),
                entire
        );
    }
}
