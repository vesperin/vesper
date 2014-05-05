package edu.ucsc.refactor.cli.commands;

import edu.ucsc.refactor.internal.VarUnit;
import edu.ucsc.refactor.spi.ProgramUnit;
import io.airlift.airline.Command;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "var", description = "Locates a local variables")
public class LocateVarCommand extends LocateCommand {
    @Override protected ProgramUnit programUnit(String name) {
        return new VarUnit(name);
    }
}
