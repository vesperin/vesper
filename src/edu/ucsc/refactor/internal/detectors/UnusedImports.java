package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.internal.visitors.ImportsReferencesVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UnusedImports extends IssueDetector {
    private static final String STRATEGY_NAME        = Smell.UNUSED_IMPORTS.getKey();
    private static final String STRATEGY_DESCRIPTION = Smell.UNUSED_IMPORTS.getSummary();

    /**
     * Instantiate {@code UnusedImports} detector.
     */
    public UnusedImports() {
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);
    }

    @Override public void scanJava(Context context) {
        final CompilationUnit unit = context.getCompilationUnit();
        createIssues(AstUtil.getUnusedImports(unit));
    }
}
