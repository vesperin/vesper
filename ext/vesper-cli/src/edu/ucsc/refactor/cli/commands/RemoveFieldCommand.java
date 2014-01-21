package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "field", description = "Remove unused param from the indexed Source")
public class RemoveFieldCommand extends RemoveCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
        return ChangeRequest.deleteField(selection);
    }
}
