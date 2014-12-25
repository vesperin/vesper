package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.Smell;
import io.airlift.airline.Command;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "deduplicate", description = "Automatically deduplicates members of the tracked source")
public class DeduplicateCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws Exception {
        ensureValidState(environment);

        final List<Issue> issues = environment.getIssues();
        if(issues.isEmpty()){
            return Results.unit();
        }

        for(Issue each : issues){
            final Smell name = (Smell) each.getName();
            if(Names.hasAvailableResponse(name)){
                if(Names.from(name).isSame(Refactoring.DEDUPLICATE)){
                    commitChange(environment, ChangeRequest.forIssue(each));
                }
            }
        }

        if(environment.isErrorFree()){
            return Results.errorResult(environment.getErrorMessage());
        }

        return Results.unit();
    }
}
