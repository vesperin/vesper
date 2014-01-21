package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.spi.ProgramUnit;
import edu.ucsc.refactor.spi.UnitLocator;
import io.airlift.airline.Arguments;

import java.util.Iterator;
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
        final UnitLocator   locator = environment.getCodeLocator();

        final List<Location>    locations   = locator.locate(programUnit(name));
        final StringBuilder     message     = new StringBuilder();

        final Iterator<Location> itr = locations.iterator();
        message.append("offsets").append("(");

        while(itr.hasNext()){
            final Location each = itr.next();
            message.append("[");
            message.append(each.getStart().getOffset()).append(",").append(each.getEnd().getOffset());
            message.append("]");
            if(itr.hasNext()){
                message.append(", ");
            }
        }

        message.append(")");

        return Result.infoPackage(message.toString());
    }

    protected abstract ProgramUnit programUnit(String name);

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("params", patterns)
                .toString();
    }
}
