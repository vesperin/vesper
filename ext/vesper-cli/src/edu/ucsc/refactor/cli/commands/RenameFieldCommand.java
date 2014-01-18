package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "field", description = "Renames a field found in the indexed Source's class")
public class RenameFieldCommand extends RenameCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
        return ChangeRequest.renameField(selection, newName);
    }
}
