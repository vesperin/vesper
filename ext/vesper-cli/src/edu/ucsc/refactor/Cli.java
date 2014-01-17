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
        final Interpreter singleCommand = new Interpreter();

        try {
            final Result result = singleCommand.eval(args);
            if(result.isError()){
                singleCommand.printError(result.getErrorMessage());
            } else if (result.isInfo()){
                if(!result.getInfo().isEmpty()){
                    singleCommand.print("= " + result.getInfo() + "\n");
                }
            } else if (result.isIssuesList()){
                final List<Issue> issues = result.getIssuesList();
                for(int i = 1; i <= issues.size(); i++){
                    singleCommand.print(String.valueOf(i) + ". ");
                    singleCommand.print(issues.get(i).getName().getKey() + ".");
                    singleCommand.print("\n");
                }
            } else if(result.isSource()){
                singleCommand.printResult(result.getSource().getContents());
            }
        } catch (Throwable e) {
            System.out.println(firstNonNull(e.getMessage(), "Unknown command line parser error"));
            System.exit(EXIT_PERMANENT);
        }
    }
}
