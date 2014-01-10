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


    @Override public CommitStatus commit() throws RuntimeException {
        final Source    current             = getLoad().peek().getSource();
        final boolean   isAboutToBeUpdated  = !getLoad().isEmpty();


        if(!isAboutToBeUpdated){ return CommitStatus.nothingStatus(); }


        final String    username            = System.getProperty("user.name");
        final String    fileName            = StringUtil.extractName(current.getName());
        final ASTNode   node                = getChange().getCause().getAffectedNodes().get(0); // never null

        try {
            final String    updatedSourceContent = squashedDeltas(fileName, getLoad(), node);
            final Date      date                 = new Date();
            final Source    updatedSource        = new Source(
                    current.getName(),
                    updatedSourceContent,
                    current.getDescription()
            );

            updatedSource.setId(current.getId());

            for(Note each : current.getNotes()){
                updatedSource.addNote(each);
            }

            updateSource(updatedSource);

            // fill out the `more` information
            final Name info = getChange().getCause().getName();

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
}
