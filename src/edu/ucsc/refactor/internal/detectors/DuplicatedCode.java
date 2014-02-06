package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.internal.util.DetectedClone;
import edu.ucsc.refactor.internal.util.DetectedClones;
import edu.ucsc.refactor.internal.visitors.DuplicateCodeVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class DuplicatedCode extends IssueDetector  {

    private static final String STRATEGY_NAME        = Smell.DUPLICATED_CODE.getKey();
    private static final String STRATEGY_DESCRIPTION = Smell.DUPLICATED_CODE.getSummary();

    /**
     * Instantiate a new duplicated code detector.
     */
    public DuplicatedCode() {
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);
    }

    @Override public void scanJava(Context context) {
        final DuplicateCodeVisitor visitor = new DuplicateCodeVisitor(context.getSource());
        context.accept(visitor);

        final DetectedClones clones = visitor.getClones();

        for(List<DetectedClone> candidates : clones){
            final Issue issue = createIssue();
            for(DetectedClone candidate : candidates){
                issue.addNode(candidate.getParseTree());
            }
        }

    }
}
