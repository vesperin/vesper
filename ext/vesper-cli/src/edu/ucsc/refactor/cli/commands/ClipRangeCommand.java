package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.ChangeRequest;
import edu.ucsc.refactor.SourceSelection;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "range", description = "Slice a source code range from the tracked Source")
public class ClipRangeCommand extends ClipCommand {
    @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
        return ChangeRequest.clipSelection(selection);
    }
}
