package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.internal.MethodUnit;
import edu.ucsc.refactor.spi.ProgramUnit;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "method", description = "Locates a method found in the tracked Source")
public class LocateMethodCommand extends LocateCommand {
    @Override protected ProgramUnit programUnit(String name) {
        return new MethodUnit(name);
    }
}
