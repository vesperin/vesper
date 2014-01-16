package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedImports extends SourceChanger {

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.UNUSED_IMPORTS)
                || Names.from(Smell.UNUSED_METHOD).isSame(Refactoring.DELETE_UNUSED_IMPORTS);
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final SourceChange change = new SourceChange(cause, this, parameters);

        try {
            for(ASTNode eachUnusedImportDeclaration : cause.getAffectedNodes()){
                final ASTRewrite rewrite = ASTRewrite.create(
                        eachUnusedImportDeclaration.getRoot().getAST()
                );

                rewrite.remove(eachUnusedImportDeclaration, null);
                change.getDeltas().add(createDelta(eachUnusedImportDeclaration, rewrite));
            }
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }
}
