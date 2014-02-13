package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.SourceFileReader;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.util.StringUtil;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.util.List;

import static io.airlift.airline.OptionType.COMMAND;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "add", description = "Add file contents to the index")
public class AddCommand extends VesperCommand {
    @Option(type = COMMAND, name = {"-f", "--file"}, description = "Add a file")
    public boolean file = false;

    @Arguments(description = "Patterns of files to be added")
    public List<String> patterns;

    @Override public Result execute(Environment environment) throws RuntimeException {
        Preconditions.checkNotNull(environment);
        Preconditions.checkNotNull(patterns);
        Preconditions.checkArgument(!patterns.isEmpty(), "add... was given no arguments");

        if(file){
            final String path = Preconditions.checkNotNull(Iterables.get(patterns, 0, null));
            final String name = StringUtil.extractName(path);
            final String cont = SourceFileReader.readContent(path);

            return compareAndSet(environment, name + ".java", cont);

        } else {
            final String head    = Preconditions.checkNotNull(Iterables.get(patterns, 0, null));
            final String tail    = Preconditions.checkNotNull(Iterables.get(patterns, 1, null));

            final boolean headWithExt = "java".equals(Files.getFileExtension(head));
            final boolean tailWithExt = "java".equals(Files.getFileExtension(tail));

            final String name       = headWithExt && !tailWithExt ? head : tail;
            final String content    = !headWithExt && tailWithExt ? head : tail;

            return compareAndSet(environment, name, content);
        }
    }

    private Result compareAndSet(Environment environment, String name, String content){
        if(environment.isSourceTracked()){
            // ask to continue
            if (!ask("Are you sure you would like to REPLACE the existing SOURCE?", false)) {
                return environment.unit();
            }
        }

        environment.track(
                new Source(
                        name,
                        content
                )
        );


        return Result.infoPackage(String.format("%s is now being tracked by Vesper", name));
    }

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("-f", file)
                .add("params", patterns)
                .toString();
    }
}
