package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "class", description = "Renames a class or interface found in the indexed Source")
public class RenameClassCommand extends RenameCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
        return ChangeRequest.renameClassOrInterface(selection, newName);
    }
}