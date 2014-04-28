package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "var", description = "Removes a local variable")
public class RemoveLocalVariableCommand extends RemoveCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
        return ChangeRequest.deleteLocalVariable(selection);
    }
}
