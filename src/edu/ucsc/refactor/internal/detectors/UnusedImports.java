package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.internal.visitors.ImportsReferencesVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.util.AstUtil;
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

    private final Set<ASTNode> unusedImportDeclarations;


    /**
     * Instantiate {@code UnusedImports} detector.
     */
    public UnusedImports() {
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);
        this.unusedImportDeclarations  = new HashSet<ASTNode>();
    }

    @Override public void scanJava(Context context) {
        final CompilationUnit unit      = context.getCompilationUnit();
        final boolean visitJavaDocTags  = AstUtil.processJavadocComments(unit);

        final ImportsReferencesVisitor visitor = new ImportsReferencesVisitor(visitJavaDocTags);
        context.accept(visitor);

        final Set<String> importNames   = visitor.getImportNames();
        final Set<String> staticNames   = visitor.getStaticImportNames();


        @SuppressWarnings("unchecked")
        final List<ImportDeclaration> totalImports = unit.imports();
        for(ImportDeclaration eachDeclaration : totalImports){

            final Name name              = eachDeclaration.getName();
            final boolean isNotAsterisk  = !eachDeclaration.toString().contains("*;");
            final String target          = AstUtil.getSimpleNameIdentifier(name);


            if(!importNames.contains(target) && (!staticNames.contains(target) || staticNames
                    .isEmpty()) && isNotAsterisk){
                unusedImportDeclarations.add(eachDeclaration);
            }
        }


        createIssues(unusedImportDeclarations);

    }
}
