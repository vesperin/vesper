package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "show", description = "Shows the current indexed source")
public class OriginShow extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        if(!environment.isSourceTracked()){
            return environment.unit();
        }

        return Result.infoPackage(environment.getOrigin().getContents());
    }

    @Override public String toString() {
        return Objects.toStringHelper("OriginShow")
                .toString();
    }
}
