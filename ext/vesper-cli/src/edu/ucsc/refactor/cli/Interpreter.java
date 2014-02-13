package edu.ucsc.refactor.cli;

import com.google.common.util.concurrent.Atomics;
import edu.ucsc.refactor.Credential;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A very basic CLI Interpreter.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Interpreter {

    public static final String VERSION = "Vesper v0.0.0";

    final Parser      parser;
    final Environment environment;

    final AtomicReference<Credential> credential;

    /**
     * Construct a new Vesper's interpreter.
     */
    public Interpreter(){
        this(Credential.none());
    }

    /**
     * Construct a new Vesper's interpreter.
     * @param accessInfo The appropriate credentials to gist service.
     */
    public Interpreter(Credential accessInfo){
        parser          = new Parser();
        environment     = new Environment();
        this.credential = Atomics.newReference(accessInfo);
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
        environment.restart();
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
    public void printResult(Result result) {
        if(result.isInfo()){
            printInfo(result);
        } else if(result.isWarning()){
            printWarning(result.getDescription());
        } else if(result.isError()){
            printError(result);
        } else {
            printResult("()");
        }
    }

    private void printWarning(String description) {
        System.out.println("? " + description);
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

    void printError(Result result) {
        printError(result.getDescription());
    }

    void printInfo(Result result) {
        final boolean pending  = result.getCommitSummary().isPending();
        final boolean canceled = result.getCommitSummary().isCanceled();

        if(pending | canceled){ // if they are what they say they are, then we don't have an updated source
            printResult(result.getDescription());
        } else {
            printResult(result.getCommitSummary().more());
        }
    }
}