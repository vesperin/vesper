package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.internal.visitors.MagicNumberVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MagicNumber extends IssueDetector {
    private static final String STRATEGY_NAME        = Smell.MAGIC_NUMBER.getKey();
    private static final String STRATEGY_DESCRIPTION = Smell.MAGIC_NUMBER.getSummary();

    /**
     * Instantiate {@code UnusedImports} detector.
     */
    public MagicNumber() {
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);
    }

    @Override public void scanJava(Context context) {
        final MagicNumberVisitor visitor = new MagicNumberVisitor();

        context.accept(visitor);

        createIssues(visitor.getMagicNumbers());
    }


}
