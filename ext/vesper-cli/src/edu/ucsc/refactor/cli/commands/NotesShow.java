package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.Note;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.util.Notes;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "show", description = "Shows all recorded notes")
public class NotesShow extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final Notes notes   = environment.getOrigin().getNotes();
        final StringBuilder text    = new StringBuilder();

        for(Note each : notes){
            text.append(each.getContent()).append("\n");
        }

        return Result.infoPackage(text.toString());
    }

    @Override public String toString() {
        return Objects.toStringHelper("NotesShow")
                .toString();
    }
}
