package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "method", description = "Renames a method found in the indexed Source")
public class RenameMethodCommand extends RenameCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
        return ChangeRequest.renameMethod(selection, newName);
    }
}