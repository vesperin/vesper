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

    public  static final String         VERSION = "Vesper v0.0.0";
    private static final ResultVisitor  VISITOR = new SysResultVisitor();

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
        this.parser         = new Parser();
        this.environment    = new Environment();
        this.credential     = Atomics.newReference(accessInfo);

        if(!isNoneCredential(this.credential.get())){
            enableUpstream(this.credential.get());
        }
    }

    /**
     * Enable remote commits by providing a credential.
     *
     * @param newCredential Access credential to a remote repository.
     */
    public final void enableUpstream(Credential newCredential){
        if(newCredential == null || newCredential.isNoneCredential()) return;
        if(this.credential.compareAndSet(this.credential.get(), newCredential)){
            getEnvironment().enableUpstream(this.credential.get());
        }
    }


    private static boolean isNoneCredential(Credential credential){
        return credential != null && credential.isNoneCredential();

    }

    /**
     * Evaluate an expression and return its result.
     *
     * @param expression The expression to be evaluated
     * @return The {@code Result} of this evaluation.
     * @throws RuntimeException if unable to evaluate this expression.
     */
    public Result processSingleExpression(String expression) throws RuntimeException {
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
    public Result process(String... args) throws RuntimeException {
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

    public void eval(Result result, ResultVisitor evaluator){
        result.accepts(evaluator);
    }

    /**
     * Prints a result to the screen.
     *
     * @param result The text to be printed.
     */
    public void eval(Result result) {
        eval(result, VISITOR);
    }

    /**
     * Prints an error message to the screen.
     *
     * @param error the error message.
     */
    public void printError(String error) {
        System.out.println("! " + error);
    }
}