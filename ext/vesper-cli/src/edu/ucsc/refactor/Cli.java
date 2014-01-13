package edu.ucsc.refactor;

import edu.ucsc.refactor.cli.Interpreter;
import edu.ucsc.refactor.cli.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Cli {
    private Cli(){}

    public static void main(String[] args) throws IOException  {
        if (args.length == 0) {
            runRepl();
        } else {
            System.out.println("Vesper expects zero argument.");
        }
    }


    static void print(String text) {
        System.out.print(text);
    }


    static void printResult(String result) {
        System.out.print("= ");
        System.out.println(result);
    }

    static void printError(String message) {
        System.out.println("! " + message);
    }

    private static void runRepl() throws IOException {
        System.out.println("vesper v0.0.0");
        System.out.println("-----------");
        System.out.println("Type 'q' and press Enter to quit.");


        InputStreamReader converter = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(converter);

        Interpreter interpreter = new Interpreter();


        Result result = null;
        while (true) {
            System.out.print("vesper> ");

            String line = in.readLine();

            if (line.equals("q")) break;
            if (line.equals("help")) {
                printUsage();
                continue;
            }

            if (line.equals("clear")) {
                clearScreen();
                continue;
            }

            if(line.equals("log")){
                if(result != null){
                   if(result.isCommitRequest()){
                      printResult(result.getCommitRequest().more());
                   } else if(result.isSource()){
                       printResult(result.getSource().getContents());
                   }
                }

                continue;
            }

            result = interpreter.evaluateAndReturn(line);

            if(result.isError()){
                printError(result.getErrorMessage());
            } else if (result.isInfo()){
               print("= " + result.getInfo());
            } else if (result.isIssuesList()){
                final List<Issue> issues = result.getIssuesList();
                for(int i = 0; i < issues.size(); i++){
                    print(String.valueOf(i));
                    print(issues.get(i).getName().getKey());
                    print("\n");
                }
            }
        }

    }


    static void clearScreen() throws IOException {
        final String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            Runtime.getRuntime().exec("cls");
        } else {
            Runtime.getRuntime().exec("clear");
        }

    }

    private static void printUsage() {
        System.out.println();
        System.out.println("Usage: Vesper ");
        System.out.println();
        System.out.println("vesper> <<commands>|q|help|>");
        System.out.println("  <commands>:");
        System.out.println("      add <source_code> <file_name_with_extension>");    //DONE
        System.out.println("      delete <origin|file_name_with_extension>");        //DONE
        System.out.println();
        System.out.println("      fix <name_of_issue>");
        System.out.println("          Fixes a detected source code issue (e.g., Code smell)");
        System.out.println("      ren <member>(start_offset, end_offset) \"new_name\"");
        System.out.println("      rm <member>(start_offset, end_offset)");
        System.out.println("         <member>: <class|method|param|field>");
        System.out.println();
        System.out.println("      checkout <class|method|selection>(start_offset, end_offset) <file_name_with_extension>");
        System.out.println("         Checkout a class, method, or methods to a new empty class");
        System.out.println();
        System.out.println("      status");                                                           //DONE
        System.out.println("        This command checks whether the source code (i.e., the origin)");
        System.out.println("        has issues (e.g., code smells). And then it recommends changes");
        System.out.println("        that can be made to the origin.");
        System.out.println("        If the user commits any of the recommended changes, then Vesper");
        System.out.println("        (after committing change) will re-inspect the file and recommends ");
        System.out.println("        new changes.");
        System.out.println();
        System.out.println("      publish");
        System.out.println("          Publishes all collected changes to a remote repository");
        System.out.println("      format");
        System.out.println("          Formats the entire source code (origin)");
        System.out.println("      tag \"Some Description of Source Code\"");           // DONE
        System.out.println("         Describes the origin. Each time this command is invoked, a  ");
        System.out.println("         a new comment will be recorded.");
        System.out.println();
        System.out.println("  q: exits Vesper's little REPL");
        System.out.println("  clear: clears REPL screen");
        System.out.println("  help: shows more information on how to use Vesper's little REPL");
        System.out.println("  log:  Show commit logs or Origin (if we have not performed any refactoring)");
        System.out.println();
    }
}
