package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.Commit;
import io.airlift.airline.Command;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "publish", description = "Publish all recorded commits")
public class PublishCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final StringBuilder     details     = new StringBuilder();

        try {
            final List<Commit>    toReview  = environment.publishCommitHistory();

            for(Commit eachCommit : toReview){ // in order

                if(eachCommit.isValidCommit()){
                    environment.forgetCommit(eachCommit);
                    details.append(eachCommit.getCommitSummary().more());
                    if(!environment.getCommitHistory().isEmpty()){
                        details.append("\n");
                    }
                }
            }


            return createAppropriateResult(environment, details);

        } catch (Throwable ex){
            return Results.errorResult(ex.getMessage());
        }
    }


    private Result createAppropriateResult(Environment environment, StringBuilder details){

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
