package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "publish", description = "Publish all recorded commits")
public class PublishCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final StringBuilder details = new StringBuilder();
        final CommitHistory current = environment.getCommitHistory();
        for(Commit eachCommit : current){ // in order
            final Commit pushed = environment.publish(eachCommit);

            if(pushed.isValidCommit()){
                environment.forgetCommit(pushed);
                details.append(pushed.getCommitSummary().more());
                if(!environment.getCommitHistory().isEmpty()){
                    details.append("\n");
                }
            }
        }


        if(environment.getCommitHistory().isEmpty()){
            return Results.infoResult(
                    "\nGreat!, all commits have been published. See details:\n"
                            + details.toString()
            );
        } else {
            return Results.infoResult(
                    "A total of "
                            + environment.getCommitHistory().size()
                            + " commits were not published. Tried again later."
            );
        }
    }

    @Override public String toString() {
        return Objects.toStringHelper("PublishCommand")
                .toString();
    }
}
