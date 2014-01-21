package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.util.Checkpoint;
import edu.ucsc.refactor.util.CommitHistory;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "log", description = " Show commit logs")
public class LogCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws Exception {
        Preconditions.checkNotNull(environment);

        final boolean       verboseMode = globalOptions.verbose;
        final CommitHistory entire      = environment.getHistory();
        final Result        commits     = Result.empty(Result.Content.COMMIT);

        if(verboseMode) {
            for(Checkpoint each : entire){
                commits.add(each.getCommitStatus());
            }
        }

        return commits;
    }
}
