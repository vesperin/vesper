package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.spi.ProgramUnit;
import io.airlift.airline.Arguments;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class LocateCommand extends VesperCommand {
    @Arguments(description = "Locate operation parameters")
    public List<String> patterns;

    @Override public Result execute(Environment environment) throws RuntimeException {
        Preconditions.checkNotNull(environment);
        Preconditions.checkNotNull(patterns);
        Preconditions.checkArgument(!patterns.isEmpty());
        Preconditions.checkArgument(patterns.size() == 1);

        final String        name    = patterns.get(0);

        final List<NamedLocation>       locations   = environment.lookup(programUnit(name));
        if(locations.isEmpty()){
            return Results.unit();
        }

        return Results.locationsResult(
                String.format("Found %d locations in this source:\n\t\t", locations.size()),
                locations
        );
    }

    protected abstract ProgramUnit programUnit(String name);

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("params", patterns)
                .toString();
    }
}
