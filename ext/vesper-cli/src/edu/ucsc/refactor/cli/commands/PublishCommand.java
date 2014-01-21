package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.CommitStatus;
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
        while(!requests.isEmpty()){
            final CommitRequest request = requests.remove();
            final CommitStatus status  = environment.getCodeRefactorer().publish(request).getStatus();

            if(status.isAborted()){
                skipped.add(request);
            } else {
                details.append(status.more());
                if(!requests.isEmpty()){
                    details.append("\n");
                }
            }
        }

        if(requests.isEmpty() && skipped.isEmpty()){
            return Result.infoPackage(
                    "\nGreat!, all commits have been published. See details:\n"
                            + details.toString()
            );
        } else {
            while(!skipped.isEmpty()){
                environment.collect(skipped.remove());
            }

            return Result.infoPackage(
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
