package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitSummary;
import edu.ucsc.refactor.spi.Name;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Date;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Changes will be committed locally, and not to Github's Gist service.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class LocalCommitRequest extends AbstractCommitRequest {
    private static final Logger LOGGER  = Logger.getLogger(LocalCommitRequest.class.getName());

    /**
     * Instantiates a new {@link LocalCommitRequest}
     *
     * @param change The change to be applied and transmitted.
     */
    public LocalCommitRequest(Change change) {
        super(change);
    }


    @Override public Commit commit() throws RuntimeException {

        final boolean   isDirty         = isDirty();

        CommitSummary summary = CommitSummary.forPendingCommit();


        if(!isDirty){

            summary = summary.updateSummary(CommitSummary.forCanceledCommit("There is nothing to commit!"));
            return Commit.createInvalidCommit(getChange().getCause().getName(), summary);
        }

        final Source    beforeCommit       = getSourceBeforeCommit();
        final String    username           = System.getProperty("user.name");
        final ASTNode   node               = getChange().getCause().getAffectedNodes().get(0); // never null

        final Downstream downstream = new Downstream(node);

        try {

            final Source afterCommit  = getSourceAfterCommit(beforeCommit, getLoad());
            final long   now          = System.nanoTime();

            // fill out the `more` information
            final Name info = getChange().getCause().getName();
            final Date date = new Date(now);

            summary = summary.updateSummary(
                    CommitSummary.forSuccessfulCommit(
                            username,
                            date,
                            (info.getKey() + ":" + info.getSummary())
                    )
            );

            final Commit valid =  Commit.createValidCommit(
                    info,
                    beforeCommit,
                    afterCommit,
                    summary
            );


            downstream.push(valid);

            return valid;

        } catch (Throwable ex){

            summary = summary.updateSummary(
                    CommitSummary.forFailedCommit(
                            ex.getMessage()
                    )
            );

            LOGGER.throwing("unable to commit change", "commit()", ex);

            throw new RuntimeException(summary.more());
        }

    }


    private Source getSourceBeforeCommit(){
        // todo(Huascar) since we already set the updated source into the compilation unit,
        // I wonder weather we can just call Source.from(node) and get the latest version of
        // the Source instead of doing this:
        return getLoad().peek().getSource();
    }


    private static Source getSourceAfterCommit(Source seed, Queue<Delta> load){
        final String    fileName             = StringUtil.extractFileName(seed.getName());
        final String    updatedSourceContent = squashedDeltas(fileName, load);

        return Source.from(seed, updatedSourceContent);
    }


    private boolean isDirty(){
        return !getLoad().isEmpty();
    }

    @Override public String toString() {
        return String.format("CommitRequest for %s", getChange().getCause().getName().getKey());
    }
}
