package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.CommitSummary;
import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;
import edu.ucsc.refactor.util.SourceHistory;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Downstream implements Repository {
    private final ASTNode node;

    /**
     * Construct a new {@code Downstream} repo.
     * @param node The ASTNode object
     */
    public Downstream(ASTNode node){
        this.node = Preconditions.checkNotNull(node);
    }

    @Override public Commit push(Commit commit) {

        try {
            AstUtil.syncSourceProperty(commit.getSourceAfterChange(), node);
        } catch (Throwable ex){
            commit.amendSummary(CommitSummary.forFailedCommit(ex.getMessage()));
        }

        return commit;
    }

    @Override public SourceHistory pull(String thatHistory) {
        return new SourceHistory(); // empty one
    }
}
