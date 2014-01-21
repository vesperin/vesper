package edu.ucsc.refactor;

import edu.ucsc.refactor.cli.Interpreter;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Vesper's very own CLI
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Cli {
    static final int EXIT_PERMANENT = 100;

    private Cli(){}

    public static void main(String[] args) {
        final Interpreter interpreter = new Interpreter();

        try {
            interpreter.printResult(
                    interpreter.eval(args)
            );
        } catch (Throwable e) {
            System.out.println(firstNonNull(e.getMessage(), "Unknown command line parser error"));
            System.exit(EXIT_PERMANENT);
        }
    }
}
