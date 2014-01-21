package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "param", description = "Remove unused param from a method in the indexed Source")
public class RemoveParameterCommand extends RemoveCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
        return ChangeRequest.deleteParameter(selection);
    }
}
