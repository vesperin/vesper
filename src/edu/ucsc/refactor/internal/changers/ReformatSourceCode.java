package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ReformatSourceCode extends SourceChanger {
    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.REFORMAT_CODE);
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final Change change = new SourceChange(cause, this, parameters);
        for(ASTNode each : cause.getAffectedNodes()){
            final Delta delta = reformat(each);
            change.getDeltas().add(delta);
        }

        return change;
    }


    private Delta reformat(ASTNode astNode) {

        final ASTRewrite rewrite = ASTRewrite.create(astNode.getAST());
        final ASTNode replacement = ASTNode.copySubtree(
                astNode.getAST(),
                astNode
        );

        rewrite.replace(astNode, replacement, null);

        return createDelta(astNode, rewrite);
    }
}