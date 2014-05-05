package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "var", description = "Renames a local variable")
public class RenameLocalVariableCommand extends RenameCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
        return ChangeRequest.renameLocalVariable(selection, newName);
    }
}
