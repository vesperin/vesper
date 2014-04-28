package edu.ucsc.refactor.cli;

import edu.ucsc.refactor.cli.commands.*;
import io.airlift.airline.Cli;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Grammar {
    final Cli.CliBuilder<VesperCommand> builder;

    /**
     * Construct a grammar for Vesper's CLI.
     */
    public Grammar(){
        this.builder = Cli.<VesperCommand>builder("vesper")
                .withDescription("the nice CLI for Vesper")
                .withDefaultCommand(HelpCommand.class)
                .withCommand(HelpCommand.class)
                .withCommand(LogCommand.class)
                .withCommand(ResetCommand.class)
                .withCommand(InspectCommand.class)
                .withCommand(ReplCommand.class)
                .withCommand(ConfigCommand.class)
                .withCommand(AddCommand.class)
                .withCommand(OriginShow.class)
                .withCommand(PublishCommand.class)
                .withCommand(FormatCommand.class)
                .withCommand(DeduplicateCommand.class)
                .withCommand(OptimizeImportsCommand.class);

        builder.withGroup("whereis")
                .withDescription("Locates a program unit found in the tracked source")
                .withDefaultCommand(LocateClassCommand.class)
                .withCommand(LocateClassCommand.class)
                .withCommand(LocateMethodCommand.class)
                .withCommand(LocateParamCommand.class)
                .withCommand(LocateVarCommand.class)
                .withCommand(LocateFieldCommand.class);

        builder.withGroup("notes")
                .withDescription("Manage set of notes describing the tracked source")
                .withDefaultCommand(NotesShow.class)
                .withCommand(NotesShow.class)
                .withCommand(NoteAdd.class);

        builder.withGroup("rename")
                .withDescription("Manage set of renaming commands")
                .withDefaultCommand(RenameClassCommand.class)
                .withCommand(RenameClassCommand.class)
                .withCommand(RenameMethodCommand.class)
                .withCommand(RenameParameterCommand.class)
                .withCommand(RenameFieldCommand.class)
                .withCommand(RenameLocalVariableCommand.class);

        builder.withGroup("rm")
                .withDescription("Remove file contents from the tracked source")
                .withDefaultCommand(RemoveSourceCommand.class)
                .withCommand(RemoveSourceCommand.class)
                .withCommand(RemoveClassCommand.class)
                .withCommand(RemoveMethodCommand.class)
                .withCommand(RemoveParameterCommand.class)
                .withCommand(RemoveFieldCommand.class)
                .withCommand(RemoveLocalVariableCommand.class)
                .withCommand(RemoveRegionCommand.class);

        builder.withGroup("slice")
                .withDescription("Slice a code section from the tracked source")
                .withDefaultCommand(SliceClassCommand.class)
                .withCommand(SliceClassCommand.class)
                .withCommand(SliceMethodCommand.class)
                .withCommand(SliceRangeCommand.class);

    }


    /**
     * Pack the grammar into a {@code CLI} object.
     * @return a {@code CLI} object.
     */
    public Cli<VesperCommand> pack() {
        return builder.build();
    }
}
