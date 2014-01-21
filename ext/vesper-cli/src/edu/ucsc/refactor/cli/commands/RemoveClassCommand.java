package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "class", description = "Remove unused inner class from the indexed Source")
public class RemoveClassCommand extends RemoveCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
        return ChangeRequest.deleteClass(selection);
    }
}
