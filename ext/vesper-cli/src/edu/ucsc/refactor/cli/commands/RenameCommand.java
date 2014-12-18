package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.Commit;
import io.airlift.airline.Arguments;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class RenameCommand extends VesperCommand {
    @Arguments(description = "Rename operation parameters")
    public List<String> patterns;

    @Override public Result execute(Environment environment) throws RuntimeException {
        Preconditions.checkNotNull(environment);
        Preconditions.checkNotNull(patterns);
        Preconditions.checkArgument(!patterns.isEmpty());
        Preconditions.checkArgument(patterns.size() == 2);


        // (1,2)-> head
        // newName-> tail
        final String head = patterns.get(0).replace("[", "").replace("]", "");
        final String tail = patterns.get(1);

        final SourceSelection selection = createSelection(environment, head);

        final ChangeRequest request   = createChangeRequest(selection, tail);
        final Commit        applied   = commitChange(environment, request);

        if(environment.isErrorFree()){
            return Results.errorResult(environment.getErrorMessage());
        }

        return createResultPackage(
                applied
        );
    }

    protected abstract ChangeRequest createChangeRequest(SourceSelection selection, String newName);

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("params", patterns)
                .toString();
    }
}
