package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.spi.CommitRequest;
import io.airlift.airline.Arguments;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class SliceCommand extends VesperCommand {
    @Arguments(description = "Slice operation parameters")
    public List<String> patterns;

    @Override public Result execute(Environment environment) throws Exception {
        ensureValidState(environment);

        Preconditions.checkNotNull(patterns);
        Preconditions.checkArgument(!patterns.isEmpty());
        Preconditions.checkArgument(patterns.size() == 1);


        // [1,2]-> head
        final String head = patterns.get(0).replace("[", "").replace("]", "");

        final SourceSelection selection = createSelection(environment, head);

        final ChangeRequest request   = createChangeRequest(selection);
        final CommitRequest applied   = commitChange(environment, request);

        if(environment.isErrorFree()){
            return Result.failedPackage(environment.getErrorMessage());
        }

        return createResultPackage(applied);
    }


    protected abstract ChangeRequest createChangeRequest(SourceSelection selection);

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("params", patterns)
                .toString();
    }
}
