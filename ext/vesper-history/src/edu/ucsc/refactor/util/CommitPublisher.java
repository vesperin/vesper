package edu.ucsc.refactor.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.Commit;
import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.internal.Upstream;
import edu.ucsc.refactor.spi.Repository;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitPublisher {

    private final CommitHistory history;
    private final Credential    credential;

    /**
     * Creates a new {@code CommitPublisher}.
     */
    public CommitPublisher(){
        this(new CommitHistory(), Credential.none());
    }

    /**
     * Creates a new {@code CommitPublisher}.
     * @param history The commit history of some {@code Source}.
     * @param credential The appropriate credentials to publish to some remote repository.
     */
    public CommitPublisher(CommitHistory history, Credential credential){
        this.history    = Preconditions.checkNotNull(history);
        this.credential = Preconditions.checkNotNull(credential);
    }

    /**
     * Publishes all local commits using a default repository; a repository setup in
     * advanced.
     *
     * @return The list of commits to be deleted.
     * @throws java.lang.NullPointerException if {@code repository} is null.
     * @throws java.lang.IllegalStateException if no remote access; e.g., {@code credentials}
     *      has been provided.
     */
    public List<Commit> publish(){
        Preconditions.checkArgument(
                !this.credential.isNoneCredential(),
                "incorrect credentials; this refactorer is not setup yet for remote publishing!"
        );

        return publish(new Upstream(credential));
    }

    /**
     * Publishes local changes, wrapped in a locally committed request, to a remote upstream
     * and the return tue request with an updated status.
     *
     * @return The list of commits to be deleted.
     * @throws java.lang.NullPointerException if {@code repository} is null.
     * @throws java.lang.IllegalStateException if no remote access; e.g., {@code credentials}
     *      has been provided.
     */
    public List<Commit> publish(Repository to){
        final List<Commit> commitsToDelete = Lists.newArrayList();
        for(Commit eachCommit : history){ // in order

            final Commit pushed = publish(eachCommit, to);

            if(pushed.isValidCommit()){
                commitsToDelete.add(pushed);
            }
        }

        return commitsToDelete;
    }

    /**
     * Publishes local changes, wrapped in a locally committed request, to a remote upstream
     * and the return tue request with an updated status.
     *
     * @param localCommit A valid commit (locally committed and with no errors).
     * @param upstream The upstream.
     * @return The commit with updated status.
     * @throws java.lang.NullPointerException if {@code localCommit} or {@code upstream} are null.
     * @throws java.lang.IllegalStateException if the commit has already been remotely committed.
     */
    public Commit publish(Commit localCommit, Repository upstream){
        return Preconditions.checkNotNull(upstream).push(
                Preconditions.checkNotNull(localCommit)
        );
    }
}
