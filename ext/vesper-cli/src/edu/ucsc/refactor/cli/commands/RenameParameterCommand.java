package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "param", description = "Renames a parameter found in the indexed Source's method")
public class RenameParameterCommand extends RenameCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
        return ChangeRequest.renameParameter(selection, newName);
    }
}
