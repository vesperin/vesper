package edu.ucsc.refactor;

import edu.ucsc.refactor.cli.Interpreter;
import edu.ucsc.refactor.cli.Result;

import java.util.List;

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
            final Result result = interpreter.eval(args);
            if(result.isError()){
                interpreter.printError(result.getErrorMessage());
            } else if (result.isInfo()){
                if(!result.getInfo().isEmpty()){
                    interpreter.print("= " + result.getInfo() + "\n");
                }
            } else if (result.isIssuesList()){
                final List<Issue> issues = result.getIssuesList();
                for(int i = 1; i <= issues.size(); i++){
                    interpreter.print(String.valueOf(i) + ". ");
                    interpreter.print(issues.get(i).getName().getKey() + ".");
                    interpreter.print("\n");
                }
            } else if(result.isSource()){
                interpreter.printResult(result.getSource().getContents());
            } else {  // is a commit
                interpreter.printResult(result.getCommitRequest().more());
            }
        } catch (Throwable e) {
            System.out.println(firstNonNull(e.getMessage(), "Unknown command line parser error"));
            System.exit(EXIT_PERMANENT);
        }
    }
}
