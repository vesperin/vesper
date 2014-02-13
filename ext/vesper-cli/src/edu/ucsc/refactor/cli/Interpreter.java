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

    public  static final String       VERSION = "Vesper v0.0.0";

    final Environment                 environment;
    final AtomicReference<Credential> credential;


    /**
     * Construct a new Vesper's interpreter.
     */
    public Interpreter(){
        this(Credential.none(), new Environment());
    }

    /**
     * Construct a new Vesper's interpreter.
     * @param accessInfo The appropriate credentials to gist service.
     */
    public Interpreter(Credential accessInfo, Environment environment){
        this.environment    = environment;
        this.credential     = Atomics.newReference(accessInfo);

        if(!isNoneCredential(this.credential.get())){
            authorizeUpstreamAccess(this.credential.get());
        }
    }

    /**
     * Enable remote commits by providing a credential.
     *
     * @param newCredential Access credential to a remote repository.
     */
    public final void authorizeUpstreamAccess(Credential newCredential){
        if(newCredential == null || newCredential.isNoneCredential()) return;
        if(this.credential.compareAndSet(this.credential.get(), newCredential)){
            getEnvironment().enableUpstream(this.credential.get());
        }
    }

    /**
     * Evaluate a command given some default environment.
     *
     * @param command The vesper command
     * @return a generated result
     */
    public Result eval(VesperCommand command){
        return eval(command, environment);
    }

    /**
     * Evaluate a command given some environment.
     *
     * @param command The vesper command
     * @param environment The vesper environment
     * @return a generated result
     */
    public Result eval(VesperCommand command, Environment environment){
        return command.call(environment);
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

    private static boolean isNoneCredential(Credential credential){
        return credential != null && credential.isNoneCredential();

    }
}