package edu.ucsc.refactor;

import edu.ucsc.refactor.cli.Interpreter;
import edu.ucsc.refactor.cli.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

            if(line.equals("more")){
                if(result != null){
                   if(result.isCommitRequest()){
                      printResult(result.getCommitRequest().more());
                   } else if(result.isSource()){
                       printResult(result.getSource().getContents());
                   }
                }

                continue;
            }

            result = interpreter.eval("vesper " + line);

            if(result.isError()){
                printError(result.getErrorMessage());
            } else if (result.isInfo()){
               print("= " + result.getInfo());
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
        System.out.println("Usage: Vesper ");
        System.out.println();
        System.out.println("vesper> <<commands>|q|help|>");
        System.out.println("  <commands>:");
        System.out.println("      add <source_code> <file_name_with_extension>");
        System.out.println("      trash <origin|file_name_with_extension>");
        System.out.println();
        System.out.println("      rename <member>(start_offset, end_offset) \"new_name\"");
        System.out.println("      delete <member>(start_offset, end_offset)");
        System.out.println("             <member>: <class|method|param|field>");
        System.out.println();
        System.out.println("      move <class|method|methods>(start_offset, end_offset) <file_name_with_extension>");
        System.out.println();
        System.out.println("      health:");
        System.out.println("        This command checks whether the source code (i.e., the origin)");
        System.out.println("        has issues (e.g., code smells). And then it recommends changes");
        System.out.println("        that can be made to the origin.");
        System.out.println("        If the user commits any of the recommended changes, then Vesper");
        System.out.println("        (after committing change) will re-inspect the file and recommends ");
        System.out.println("        new changes.");
        System.out.println();
        System.out.println("      annotate \"Some Description of Source Code\"");
        System.out.println("         Describes the origin. Each time this command is invoked, a  ");
        System.out.println("         a new comment will be recorded.");
        System.out.println();
        System.out.println("  q: exits Vesper's little REPL");
        System.out.println("  q: exits Vesper's little REPL");
        System.out.println("  q: exits Vesper's little REPL");
        System.out.println();
    }
}
