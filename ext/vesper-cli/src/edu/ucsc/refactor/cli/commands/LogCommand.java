package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.util.Checkpoint;
import edu.ucsc.refactor.util.CommitHistory;
import io.airlift.airline.Command;

import java.util.Iterator;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "log", description = "Show commit logs")
public class LogCommand extends VesperCommand {
    @Override public Result execute(Environment environment) throws Exception {
        Preconditions.checkNotNull(environment);


        final CommitHistory entire      = environment.getCommitHistory();
        if(entire.isEmpty()){
            return Result.unitPackage();
        }

        final StringBuilder toScreen = new StringBuilder(entire.size() * 10000);

        final Iterator<Checkpoint> itr = entire.iterator();
        while(itr.hasNext()){
            final Checkpoint c = itr.next();

            toScreen.append(c.getCommitSummary().more());

            if(itr.hasNext()){
                toScreen.append("\n\t\t");
            }
        }

        return Result.infoPackage(toScreen.toString());
    }
}
