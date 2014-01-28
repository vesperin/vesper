package edu.ucsc.refactor.internal.changers;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveCodeRegion extends SourceChanger {
    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.DELETE_REGION);
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {
        final SourceChange change = new SourceChange(cause, this, parameters);

        try {

            Preconditions.checkState(!cause.getAffectedNodes().isEmpty(), "invalid code selection");

            final CompilationUnit   root      = getCompilationUnit(cause);
            final ASTRewrite        rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(removeCodeRegion(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }


    private Delta removeCodeRegion(CompilationUnit unit, ASTRewrite rewrite, CauseOfChange cause){

        for(ASTNode each : cause.getAffectedNodes()){
            rewrite.remove(each, null);
        }

        return createDelta(unit, rewrite);
    }
}
