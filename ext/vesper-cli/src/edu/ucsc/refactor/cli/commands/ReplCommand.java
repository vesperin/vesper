package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.cli.*;
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
@Command(name = "repl", description = "Interactive Vesper")
public class ReplCommand extends VesperCommand {
    @Option(name = "-c", description = "Enter remote credentials")
    public boolean config = false;

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

            if(runRepl(credential, environment)){
                return Result.sourcePackage(environment.getOrigin());
            }

        } catch (Throwable ex){
            throw new RuntimeException(ex);
        }

        return Environment.unit();
    }

    private static boolean runRepl(Credential credential, Environment global) throws IOException {
        System.out.println();
        System.out.println(Interpreter.VERSION);
        System.out.println("-----------");
        System.out.println("Type 'q' and press Enter to quit.");


        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);

        Interpreter interpreter = new Interpreter();

        if(credential != null){
            interpreter.getEnvironment().setCredential(credential);
        }


        Result result = null;

        while (true) {
            System.out.print("vesper> ");

            String line = in.readLine();

            if (line.equals("q")) {
                // ask to continue
                if (!AskQuestion.ask("Are you sure you would like to quit " + Interpreter.VERSION, false)) {
                    continue;
                } else {
                    // bubble up changes done in REPL mode to the global
                    // environment (scope), and then clear the local
                    // environment.
                    global.clears();
                    global.setOrigin(interpreter.getEnvironment().getOrigin());
                    interpreter.clears();
                    interpreter.print("quitting " + Interpreter.VERSION + " Good bye!\n");
                    return true; // exiting ivr
                }
            }

            if (line.equals("help")) {
                interpreter.eval("help");
                continue;
            }

            if(line.equals("repl")){
                interpreter.print(Interpreter.VERSION + ", yeah! that's me.\n");
                continue; // no need to call it again
            }


            if(line.equals("log")){
                if(result != null){
                    if(result.isCommitRequest()){
                        interpreter.printResult(result.getCommitRequest().more());
                    } else if(result.isSource()){
                        interpreter.printResult(result.getSource().getContents());
                    }
                }

                continue;
            }

            try {
                result = interpreter.evaluateAndReturn(line);
            } catch (ParseException ex){
                interpreter.printError("Unknown command");
                continue;
            }

            if(result.isError()){
                interpreter.printError(result.getErrorMessage());
            } else if (result.isInfo()){
                if(!result.getInfo().isEmpty()){
                    interpreter.print("= " + result.getInfo());
                }
            } else if (result.isIssuesList()){
                final List<Issue> issues = result.getIssuesList();
                for(int i = 0; i < issues.size(); i++){
                    interpreter.print(String.valueOf(i + 1) + ". ");
                    interpreter.print(issues.get(i).getName().getKey() + ".");
                    interpreter.print("\n");
                }
            }  else if(result.isSource()){
                interpreter.printResult(result.getSource().getContents());
            }
        }

    }



    @Override public String toString() {
        return Objects.toStringHelper("ReplCommand")
                .add("config", config)
                .toString();
    }
}
