package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.internal.FieldUnit;
import edu.ucsc.refactor.spi.ProgramUnit;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "field", description = "Locates a field found in the tracked Source")
public class LocateFieldCommand extends LocateCommand {
    @Override protected ProgramUnit programUnit(String name) {
        return new FieldUnit(name);
    }
}
