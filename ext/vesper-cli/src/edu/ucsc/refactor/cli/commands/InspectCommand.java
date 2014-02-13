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

import java.util.Iterator;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "inspect", description = "Shows the issues found in the tracked source")
public class InspectCommand extends VesperCommand {

    @Arguments(description = "Source to inspect")
    public String name;

    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final boolean inspectOrigin = Strings.isNullOrEmpty(name)
                || "java".equals(Files.getFileExtension(name));


        if(environment.isSourceTracked() && inspectOrigin){
            final List<Issue> issues = environment.getCodeRefactorer().getIssues(environment.getTrackedSource());

            if(!issues.isEmpty()){
                final StringBuilder toIssues = new StringBuilder(String.format("Found %d issues in this source:\n\t\t", issues.size() ));

                final Iterator<Issue> itr = issues.iterator();
                while(itr.hasNext()){
                    final Issue issue = itr.next();
                    toIssues.append(issue.getName().getKey()).append(".");
                    if(itr.hasNext()){
                        toIssues.append("\n\t\t");
                    }
                }

                toIssues.append("\n");
                return Result.warningPackage(toIssues.toString());
            }

        }

        return environment.unit();

    }

    @Override public String toString() {
        return Objects.toStringHelper("InspectCommand")
                .toString();
    }

}
