package edu.ucsc.refactor.cli.commands;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.cli.Environment;
import edu.ucsc.refactor.cli.Result;
import edu.ucsc.refactor.cli.VesperCommand;
import edu.ucsc.refactor.cli.results.Results;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@Command(name = "config", description = "Configure access to a remote repository")
public class ConfigCommand  extends VesperCommand {

    static final int USERNAME       = 0;
    static final int USERNAME_VALUE = 1;
    static final int PASSWORD       = 2;
    static final int PASSWORD_VALUE = 3;


    @Arguments(description = "Credentials to a remote repository")
    public List<String> credentials;

    @Override public Result execute(Environment environment) throws RuntimeException {
        Preconditions.checkNotNull(environment);
        Preconditions.checkNotNull(credentials);
        Preconditions.checkState(!credentials.isEmpty());

        Preconditions.checkArgument(credentials.size() == 4);

        Preconditions.checkArgument("username".equals(credentials.get(USERNAME)));
        Preconditions.checkArgument("password".equals(credentials.get(PASSWORD)));


        final String username  = credentials.get(USERNAME_VALUE);
        final String password  = credentials.get(PASSWORD_VALUE);

        final boolean allSet  = environment.enableUpstream(username, password);

        if(allSet){
            if(globalOptions.verbose){
                return Results.infoResult("Ok, credentials have been set!\n");
            }
        }

        return Results.unit();
    }

    @Override public String toString() {
        return Objects.toStringHelper("ConfigCommand")
                .add("username", credentials.get(USERNAME_VALUE))
                .add("password", credentials.get(PASSWORD_VALUE))
                .toString();
    }
}
