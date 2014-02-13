package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "reset", description = "Reset modified source to its original state")
public class ResetCommand extends VesperCommand {
    @Arguments(description = "Reset command parameters")
    public List<String> patterns;

    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        Preconditions.checkArgument((patterns == null) || (patterns.size() == 1));

        if(patterns != null && patterns.size() == 1){
            final Source indexed = environment.resetSource(patterns.get(0));
            return Result.infoPackage(String.format("%s's index has moved to original version", indexed.getName()));
        } else {
            environment.reset();
            return Result.infoPackage(String.format("%s's index has moved to original version", environment.getTrackedSource().getName())); // show the new origin
        }
    }

    @Override public String toString() {
        return Objects.toStringHelper("ResetCommand")
                .add("params", patterns)
                .toString();
    }
}
