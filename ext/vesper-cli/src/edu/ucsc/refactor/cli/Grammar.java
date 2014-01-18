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
                .withCommand(ResetCommand.class)
                .withCommand(InspectCommand.class)
                .withCommand(ReplCommand.class)
                .withCommand(ConfigCommand.class)
                .withCommand(AddCommand.class)
                .withCommand(OriginShow.class)
                .withCommand(PublishCommand.class)
                .withCommand(FormatCommand.class);

        builder.withGroup("rename")
                .withDescription("Manage set of renaming commands")
                .withDefaultCommand(RenameClassCommand.class)
                .withCommand(RenameClassCommand.class)
                .withCommand(RenameMethodCommand.class)
                .withCommand(RenameParameterCommand.class)
                .withCommand(RenameFieldCommand.class);

        builder.withGroup("rm")
                .withDescription("Remove file contents from the indexed Source")
                .withDefaultCommand(RemoveCommand.class)
                .withCommand(RemoveCommand.class)
                .withCommand(RemoveClassCommand.class)
                .withCommand(RemoveMethodCommand.class)
                .withCommand(RemoveParameterCommand.class)
                .withCommand(RemoveFieldCommand.class);

        builder.withGroup("notes")
                .withDescription("Manage set of notes about SOURCE")
                .withDefaultCommand(NotesShow.class)
                .withCommand(NotesShow.class)
                .withCommand(NoteAdd.class);

    }


    /**
     * Pack the grammar into a {@code CLI} object.
     * @return a {@code CLI} object.
     */
    public Cli<VesperCommand> pack() {
        return builder.build();
    }
}
