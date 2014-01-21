package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "inspect", description = "Shows the issues in the Source that should be fixed")
public class InspectCommand extends VesperCommand {

    @Arguments(description = "Source to inspect")
    public String name;

    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final boolean inspectOrigin = Strings.isNullOrEmpty(name)
                || "java".equals(Files.getFileExtension(name));


        final Result result = Result.empty(Result.Content.ISSUES);
        if(environment.isSourceTracked() && inspectOrigin){
            final List<Issue> issues = environment.getCodeRefactorer().getIssues(environment.getTrackedSource());
            if(issues.isEmpty()){
                return environment.unit();
            }

            for(Issue each : issues){
                result.add(each);
            }
        }

        return result;

    }

    @Override public String toString() {
        return Objects.toStringHelper("InspectCommand")
                .toString();
    }

}
