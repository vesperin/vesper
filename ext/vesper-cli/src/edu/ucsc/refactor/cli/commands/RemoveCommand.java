package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.util.Commit;
import io.airlift.airline.Arguments;
import io.airlift.airline.Option;

import java.util.ArrayList;
import java.util.List;

import static io.airlift.airline.OptionType.GROUP;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class RemoveCommand extends VesperCommand {
    @Option(type = GROUP, name = {"-f", "--file"}, description = "Remove the indexed Source")
    public boolean file = false;

    @Arguments(description = "Remove operation parameters")
    public List<String> patterns = new ArrayList<String>();

    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        if(file){
            return Results.errorResult("Unknown option -f/--file");
        }

        Preconditions.checkNotNull(patterns);
        Preconditions.checkArgument(!patterns.isEmpty());
        Preconditions.checkArgument(patterns.size() == 1);

        // [1,2]-> head
        final String head = patterns.get(0).replace("[", "").replace("]", "");

        final SourceSelection selection = createSelection(environment, head);

        final ChangeRequest request   = createChangeRequest(selection);
        final Commit        applied   = commitChange(environment, request);

        if(environment.isErrorFree()){
            return Results.errorResult(environment.getErrorMessage());
        }

        patterns.clear();

        return createResultPackage(applied);
    }

    protected abstract ChangeRequest createChangeRequest(SourceSelection selection);

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("params", patterns)
                .toString();
    }
}
