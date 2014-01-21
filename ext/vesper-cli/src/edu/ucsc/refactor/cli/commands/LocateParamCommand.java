package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.internal.ParameterUnit;
import edu.ucsc.refactor.spi.ProgramUnit;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "param", description = "Locates a parameter found in a method in the tracked Source")
public class LocateParamCommand extends LocateCommand {
    @Override protected ProgramUnit programUnit(String name) {
        return new ParameterUnit(name);
    }
}
