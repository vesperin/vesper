package edu.ucsc.refactor.cli;

import com.google.common.collect.ImmutableList;
import io.airlift.airline.Cli;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Parser {
    final Cli<VesperCommand> cliParser;
    final StringReader       reader;

    /**
     * Construct a parser for Vesper's commands.
     */
    public Parser(){
        this(new Grammar());
    }

    /**
     * Construct a parser for Vesper's commands having a grammar as
     * the main value.
     *
     * @param grammar The Grammar.
     */
    public Parser(Grammar grammar){
        this.reader    = new StringReader();
        this.cliParser = grammar.pack();
    }

    /**
     * Parse an array of tokens.
     *
     * @param args The array of tokens
     * @return The matched {@code VesperCommand}
     */
    public VesperCommand parse(String... args){
      return parse(ImmutableList.copyOf(args));
    }

    /**
     * Parse an array of tokens.
     *
     * @param args The array of tokens
     * @return The matched {@code VesperCommand}
     */
    public VesperCommand parse(Iterable<String> args){
        return this.cliParser.parse(args);
    }

    /**
     * Parse a vesper command in text form.
     *
     * @param command The command in text form
     * @return The matched {@code VesperCommand}
     */
    public VesperCommand parse(String command){
        final Iterable<String> args = reader.process(command);
        return parse(args);
    }
}
