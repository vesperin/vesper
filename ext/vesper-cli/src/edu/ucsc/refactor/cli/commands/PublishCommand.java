package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.CommitSummary;
import io.airlift.airline.Command;

import java.util.Queue;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "publish", description = "Publish all recorded commits")
public class PublishCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final Queue<CommitRequest> requests = environment.getCommittedRequests();
        final Queue<CommitRequest> skipped  = Lists.newLinkedList();

        final StringBuilder details = new StringBuilder();
        while(!environment.isFilledWithRequests()){
            final CommitRequest request = environment.dequeueCommitRequest();
            final CommitSummary status  = environment.publish(request);

            if(status.isFailure()){
                skipped.add(request);
            } else {
                details.append(status.more());
                if(!requests.isEmpty()){
                    details.append("\n");
                }
            }
        }

        if(requests.isEmpty() && skipped.isEmpty()){
            return Results.infoResult(
                    "\nGreat!, all commits have been published. See details:\n"
                            + details.toString()
            );
        } else {
            while(!skipped.isEmpty()){
                environment.enqueueCommitRequest(skipped.remove());
            }

            return Results.infoResult(
                    "A total of "
                            + environment.getCommittedRequests().size()
                            + " commits were not published. Tried again later."
            );
        }
    }

    @Override public String toString() {
        return Objects.toStringHelper("PublishCommand")
                .toString();
    }
}
