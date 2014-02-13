package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.spi.CommitRequest;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "format", description = "Formats the tracked source")
public class FormatCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);


        final ChangeRequest request = ChangeRequest.reformatSource(environment.getOrigin());
        final CommitRequest applied = commitChange(environment, request);

        if(environment.isErrorFree()){
            return Result.failedPackage(environment.getErrorMessage());
        }

        return createResultPackage(applied);
    }

    @Override public String toString() {
        return Objects.toStringHelper("FormatCommand")
                .toString();
    }
}
