package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import edu.ucsc.refactor.util.Notes;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "show", description = "Shows all recorded notes")
public class NotesShow extends VesperCommand {
    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final Notes         notes   = environment.getOrigin().getNotes();

        if(notes.isEmpty()){
            return Results.unit();
        }

        return Results.notesResult(
                String.format("Found %d notes:\n\t\t", notes.size()),
               notes
        );
    }

    @Override public String toString() {
        return Objects.toStringHelper("NotesShow")
                .toString();
    }
}
