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
@Command(name = "format", description = "Formats the tracked Source")
public class FormatCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);


        final ChangeRequest request = ChangeRequest.reformatSource(environment.getTrackedSource());
        final CommitRequest applied = commitChange(environment, request);

        return createResultPackage(applied, "unable to commit 'format code' change");
    }

    @Override public String toString() {
        return Objects.toStringHelper("FormatCommand")
                .toString();
    }
}
