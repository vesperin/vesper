package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "class", description = "Slice a class from the tracked Source")
public class SliceClassCommand extends SliceCommand {
    @Override
    protected ChangeRequest createChangeRequest(SourceSelection selection) {
        throw new UnsupportedOperationException("to be implemented");
    }
}
