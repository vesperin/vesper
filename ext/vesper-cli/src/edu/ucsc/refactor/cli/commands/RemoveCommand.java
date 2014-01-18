package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "origin", description = "Remove the indexed source (i.e., the origin)")
public class RemoveCommand extends RemoveMemberCommand {

    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        if(file){
            // ask to continue
            if (!ask("Are you sure you would like to remove the origin?", false)) {
                return Result.nothing();
            }

            environment.setOrigin(null);

            return Result.infoPackage("origin was removed!\n");
        }

        return Result.infoPackage("There is nothing to remove!\n");
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
