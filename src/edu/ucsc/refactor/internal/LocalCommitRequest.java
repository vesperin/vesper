package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Note;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitStatus;
import edu.ucsc.refactor.spi.Name;
import edu.ucsc.refactor.util.CommitInformation;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Changes will be committed locally, and not to Github's Gist service.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class LocalCommitRequest extends AbstractCommitRequest {
    private static final Logger LOGGER  = Logger.getLogger(LocalCommitRequest.class.getName());

    private final AtomicLong timeOfCommit;

    /**
     * Instantiates a new {@link LocalCommitRequest}
     *
     * @param change The change to be applied and transmitted.
     */
    public LocalCommitRequest(Change change) {
        super(change);

        this.timeOfCommit = new AtomicLong(Long.MIN_VALUE);
    }


    @Override public CommitStatus commit() throws RuntimeException {
        final Source    current             = getLoad().peek().getSource();
        final boolean   isAboutToBeUpdated  = !getLoad().isEmpty();


        if(!isAboutToBeUpdated){ return CommitStatus.nothingStatus(); }


        final String    username            = System.getProperty("user.name");
        final String    fileName            = StringUtil.extractName(current.getName());
        final ASTNode   node                = getChange().getCause().getAffectedNodes().get(0); // never null

        try {
            final String    updatedSourceContent = squashedDeltas(fileName, getLoad(), node);
            // todo(Huascar) since we already set the updated source into the compilation unit,
            // I wonder weather we can just call Source.from(node) and get the latest version of
            // the Source instead of doing this:
            final Source    updatedSource        = new Source(
                    current.getName(),
                    updatedSourceContent,
                    current.getDescription()
            );

            updatedSource.setId(current.getId());
            updatedSource.setSignature(current.getUniqueSignature());

            for(Note each : current.getNotes()){
                updatedSource.addNote(each);
            }

            updateSource(updatedSource);

            // fill out the `more` information
            final Name info = getChange().getCause().getName();

            tick();

            final Date  date  = new Date(commitTimestamp());

            updateStatus(
                    CommitStatus.succeededStatus(
                            new CommitInformation()
                                    .commit("")
                                    .author(username)
                                    .date(date)
                                    .comment(info.getKey(), info.getSummary())
                    )
            );

            return getStatus();
        } catch (Throwable ex){
            updateStatus(
                    CommitStatus.failedStatus(
                            new CommitInformation()
                                    .error(ex.getMessage()
                                    )
                    )
            );

            LOGGER.throwing("unable to commit change", "commit(Upstream)", ex);

            throw new RuntimeException(ex);
        }

    }

    private void tick(){
        timeOfCommit.set(System.currentTimeMillis());
    }

    @Override public long commitTimestamp() {
        return timeOfCommit.get();
    }
}
