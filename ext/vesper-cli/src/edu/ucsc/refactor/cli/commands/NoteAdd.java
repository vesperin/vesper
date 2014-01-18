package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Note;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "add", description = "Adds a note")
public class NoteAdd extends VesperCommand {
    @Arguments(description = "Note to add")
    public String note;

    @Override public Result execute(Environment environment) throws RuntimeException {
        ensureValidState(environment);

        final String noteToAdd = Preconditions.checkNotNull(note);

        environment.getOrigin().addNote(
                // todo(Huascar) add SourceRange (1, 2) or [1,2]
                new Note(/*[1,2]*/noteToAdd)
        );

        return Environment.unit();
    }
}
