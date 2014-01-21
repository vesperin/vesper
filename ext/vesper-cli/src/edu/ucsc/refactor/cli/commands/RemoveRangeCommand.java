package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "range", description = "Remove a specified source code range")
public class RemoveRangeCommand extends RemoveCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
        throw new UnsupportedOperationException("to be implemented");
    }
}
