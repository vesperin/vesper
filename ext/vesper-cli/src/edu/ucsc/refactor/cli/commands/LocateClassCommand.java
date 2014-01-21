package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.internal.ClassUnit;
import edu.ucsc.refactor.spi.ProgramUnit;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "class", description = "Locates a class found in the tracked Source")
public class LocateClassCommand extends LocateCommand {
    @Override protected ProgramUnit programUnit(String name) {
        return new ClassUnit(name);
    }
}
