package edu.ucsc.refactor.cli;

import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.Source;

import java.util.List;

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
        environment.clear();
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
        if(result.isError()){
            printError(result);
        } else if (result.isInfo()){
            printInfo(result);
        } else if (result.isIssuesList()){
            printIssueList(result);
        } else if(result.isSource()){
            printSource(result);
        } else if(result.isCommit()){
            printCommit(result);
        } else {  // todo(Huascar) should I throw an error instead?
            printResult("()");
        }
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

    void printCommit(Result result){
        final List<Object> values = result.getValue();

        for(Object each : values){
            print(String.valueOf(each));
        }
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
        final List<Object> values = result.getValue();

        for(Object each : values){
            printError(String.valueOf(each));
        }
    }

    void printInfo(Result result) {
        final List<Object> values = result.getValue();

        for(Object each : values){
            printResult(String.valueOf(each));
        }
    }


    void printIssueList(Result result){
        final List<Object> values = result.getValue();

        for(int i = 0; i < values.size(); i++){
            print(String.valueOf(i + 1) + ". ");
            final Issue issue = (Issue)values.get(i);
            print(issue.getName().getKey() + ".");
            print("\n");
        }
    }

    void printSource(Result result){
        final List<Object> values = result.getValue();

        for(Object each : values){
            final Source src = (Source) each;
            printResult(src.getContents());
        }
    }
}