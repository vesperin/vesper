package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Credential;
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
                return Results.infoResult(environment.getOrigin().getContents());
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

        Interpreter interpreter = new Interpreter();

        if(credential != null){
            interpreter.enableUpstream(credential);
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
                    global.track(interpreter.getEnvironment().getOrigin());
                    interpreter.clears();
                    interpreter.print("quitting " + Interpreter.VERSION + " Good bye!\n");
                    return true; // exiting ivr
                }
            }

            if (line.equals("help")) {
                interpreter.process("help");
                continue;
            }

            if(line.equals("ivp")){
                interpreter.print(Interpreter.VERSION + ", yeah! that's me.\n");
                continue; // no need to call it again
            }


            try {
                result = interpreter.processSingleExpression(line);
            } catch (ParseException ex){
                interpreter.printError("Unknown command");
                continue;
            }

            if("log".equals(line)){
                interpreter.eval(result);
                continue;
            }

            interpreter.eval(result);
        }

    }


    @Override public String toString() {
        return Objects.toStringHelper("ReplCommand")
                .add("config", config)
                .toString();
    }
}
