package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Refactorer;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class InternalCheckpointedRefactorerCreator {
    private final Refactorer refactorer;

    public InternalCheckpointedRefactorerCreator(Refactorer refactorer){
        this.refactorer = refactorer;
    }

    public CheckpointedJavaRefactorer build(){
        return new CheckpointedJavaRefactorer(refactorer);
    }
}
