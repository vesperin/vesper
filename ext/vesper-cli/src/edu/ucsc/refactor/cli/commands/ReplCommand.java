package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.cli.*;
import edu.ucsc.refactor.cli.results.Results;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import io.airlift.airline.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "ivp", description = "Interactive Vesper")
public class ReplCommand extends VesperCommand {
    @Option(name = "-c", description = "Enter remote credentials")
    public boolean config = false;

    @Option(name = "--simple-prompt", description = "Simple prompt")
    public boolean prompt = false;

    @Arguments(description = "Interactive Vesper parameters")
    public List<String> patterns;

    @Override public Result execute(Environment environment) throws RuntimeException {
        Preconditions.checkNotNull(environment);

        try {
            Credential credential = null;
            if(config){

                Preconditions.checkNotNull(patterns);
                Preconditions.checkArgument(!patterns.isEmpty());
                Preconditions.checkArgument(patterns.size() == 4, "Unknown parameters");

                Preconditions.checkArgument("username".equals(patterns.get(0)));
                Preconditions.checkArgument("password".equals(patterns.get(2)));


                final String username  = patterns.get(1);
                final String password  = patterns.get(3);

                credential = new Credential(username, password);

            }

            if(runRepl(credential, environment, prompt)){
                final Source origin = environment.getOrigin();
                if(origin == null){
                    return Results.infoResult("Good bye!");
                } else {
                    return Results.infoResult(origin.getContents());
                }
            }

        } catch (Throwable ex){
            throw new RuntimeException(ex);
        }

        return Results.unit();
    }

    private static boolean runRepl(Credential credential, Environment global, boolean simplePrompt) throws IOException {
        System.out.println();
        System.out.println(Interpreter.VERSION);
        System.out.println("-----------");
        System.out.println("Type 'quit' and press Enter to quit.");


        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);

        final Repl repl = new Repl(new Parser(), new Interpreter());


        if(credential != null){
            repl.authorizeUpstreamAccess(credential);
        }


        Result result;

        final String prompt = simplePrompt ? "vesper> " : Interpreter.VERSION + "> ";

        while (true) {
            System.out.print(prompt);

            String line = in.readLine();

            if (line.equals("quit")) {
                // ask to continue
                if (!AskQuestion.ask("Are you sure you would like to quit " + Interpreter.VERSION, false)) {
                    continue;
                } else {
                    // bubble up changes done in REPL mode to the global
                    // environment (scope), and then clear the local
                    // environment.
                    global.restart();
                    global.track(repl.getEnvironment().getOrigin());
                    repl.clears();
                    repl.print("quitting " + Interpreter.VERSION + "....\n");
                    return true; // exiting ivr
                }
            }

            if (line.equals("help")) {
                repl.process("help");
                continue;
            }

            if(line.equals("ivp")){
                repl.print(Interpreter.VERSION + ", yeah! that's me.\n");
                continue; // no need to call it again
            }


            try {
                result = repl.processSingleLine(line);
            } catch (ParseException ex){
                repl.printError("Unknown command");
                continue;
            }

            if("log".equals(line)){
                ResultProcessor.process(result);
                continue;
            }

            ResultProcessor.process(result);
        }

    }


    @Override public String toString() {
        return Objects.toStringHelper("ReplCommand")
                .add("config", config)
                .toString();
    }


    private static class Repl {
        private final Parser        parser;
        private final Interpreter   interpreter;

        Repl(Parser parser, Interpreter interpreter){
            this.parser      = parser;
            this.interpreter = interpreter;
        }

        public final void authorizeUpstreamAccess(Credential newCredential){
            interpreter.authorizeUpstreamAccess(newCredential);
        }

        /**
         * Flush out any info stored in the interpreter.
         */
        void clears(){
            interpreter.clears();
        }


        Environment getEnvironment(){
            return interpreter.getEnvironment();
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
         * Evaluate an expression and return its result.
         *
         * @param expression The expression to be evaluated
         * @return The {@code Result} of this evaluation.
         * @throws RuntimeException if unable to evaluate this expression.
         */
        public Result processSingleLine(String expression) throws RuntimeException {
            final VesperCommand command = parser.parse(expression);
            return interpreter.eval(command);
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
            final VesperCommand command = parser.parse(args);
            return interpreter.eval(command);
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
}
