package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.results.Results;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "origin", description = "Remove the indexed source (i.e., the origin)")
public class RemoveSourceCommand extends RemoveCommand {

    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        if(file){
            // ask to continue
            if (!ask("Are you sure you would like to remove the origin?", false)) {
                return Results.unit();
            }

            environment.track(null);

            return Results.infoResult("origin was removed!\n");
        }

        return Results.infoResult("There is nothing to remove!\n");
    }

    @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
        throw new UnsupportedOperationException();
    }

    @Override public String toString() {
        return Objects.toStringHelper("RemoveCommand")
                .add("-f", file)
                .toString();
    }
}
