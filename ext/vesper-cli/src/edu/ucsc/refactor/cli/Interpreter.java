package edu.ucsc.refactor.cli;

/**
 * A very basic CLI Interpreter.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Interpreter {

    public static final String VERSION = "Vesper v0.0.0";

    final Parser      parser;
    final Environment environment;

    /**
     * Construct a new Vesper's interpreter.
     */
    public Interpreter(){
        parser      = new Parser();
        environment = new Environment();
    }

    /**
     * Evaluate an expression and return its result.
     *
     * @param expression The expression to be evaluated
     * @return The {@code Result} of this evaluation.
     * @throws RuntimeException if unable to evaluate this expression.
     */
    public Result evaluateAndReturn(String expression) throws RuntimeException {
        return parser.parse(expression).call(environment);
    }

    /**
     * Evaluate an array of command arguments. Each argument in the set represent a
     * token that will be evaluated by {@link Parser#parse(String...)}
     *
     * @param args The array of arguments to be evaluated.
     * @return The {@code Result} of this evaluation.
     * @throws RuntimeException if unable to evaluate this array of arguments.
     */
    public Result eval(String... args) throws RuntimeException {
        return parser.parse(args).call(environment);
    }

    /**
     * clears the environment.
     */
    public void clears() {
        environment.clears();
    }

    /**
     * Gets the current environment set for this Interpreter.
     *
     * @return The {@code Environment}
     */
    public Environment getEnvironment(){
        return environment;
    }

    /**
     * Prints a text to the screen.
     *
     * @param text The text to be printed.
     */
    public void print(String text) {
        System.out.print(text);
    }

    /**
     * Prints a result to the screen.
     *
     * @param result The text to be printed.
     */
    public void printResult(String result) {
        System.out.print("= ");
        System.out.println(result);
    }

    /**
     * Prints an error to the screen.
     *
     * @param message The error message to be printed.
     */
    public void printError(String message) {
        System.out.println("! " + message);
    }


}