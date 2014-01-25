package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.FieldDeclarationVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UnusedFields  extends IssueDetector {
    private static final String STRATEGY_NAME        = Smell.UNUSED_FIELD.getKey();
    private static final String STRATEGY_DESCRIPTION = Smell.UNUSED_FIELD.getSummary();

    /**
     * Instantiate {@code UnusedFields} issue detector.
     */
    public UnusedFields(){
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);
    }

    @Override public void scanJava(Context context) {
        final FieldDeclarationVisitor visitor = new FieldDeclarationVisitor();
        context.accept(visitor);

        for(FieldDeclaration eachField : visitor.getFieldDeclarations()){
            List fragments = eachField.fragments();
            for(Object eachObject : fragments){

                final VariableDeclarationFragment fragment   = (VariableDeclarationFragment) eachObject;
                final SimpleName                  name       = fragment.getName();
                final List<SimpleName>            references = AstUtil.findByNode(context.getCompilationUnit(), name);

                if(!AstUtil.isSideEffectFound(name) && references.size() <= 1){ // implies un-used field
                    createIssue(eachField);
                }
            }
        }
    }
}
