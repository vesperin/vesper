package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Atomics;
import edu.ucsc.refactor.CommitPublisher;
import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.Commit;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class GistCommitPublisher implements CommitPublisher {
    final AtomicReference<Repository> target;

    public GistCommitPublisher(Repository repository){
        this.target     = Atomics.newReference(Preconditions.checkNotNull(repository));
    }


    @Override public Commit publish(Commit localCommit) {
        return publish(localCommit, target.get());
    }

    @Override public Commit publish(Commit localCommit, Repository upstream) {
        return null;
    }
}
