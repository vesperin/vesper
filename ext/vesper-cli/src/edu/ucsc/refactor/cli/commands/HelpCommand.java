package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Inject;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "help", description = "Display help information about vesper")
public class HelpCommand extends VesperCommand {
    @Inject
    public Help help;

    @Override public Result execute(Environment environment) throws RuntimeException {
        help.call();
        return Results.unit(); // nothing to show
    }

    @Override public String toString() {
        return Objects.toStringHelper("HelpCommand")
                .add("help", help)
                .toString();
    }
}
