package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.AstUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedMethods extends SourceChanger {
    private final static int METHOD_DECLARATION             = 0;

    /**
     * Instantiates a new {@link RemoveUnusedMethods} object.
     */
    public RemoveUnusedMethods(){
        super();
    }

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.UNUSED_METHOD)
                || Names.from(Smell.UNUSED_METHOD).isSame(cause.getName());
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final SourceChange      change      = new SourceChange(cause, this, parameters);
        try {
            final MethodDeclaration declaration = getMethodDeclaration(cause.getAffectedNodes());
            final Delta             delta       = removeFromDeclaration(declaration);

            change.getDeltas().add(delta);
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private static MethodDeclaration getMethodDeclaration(List<ASTNode> nodes){
        final ASTNode node = nodes.get(METHOD_DECLARATION);

        if(node instanceof SimpleName){
            return AstUtil.parent(MethodDeclaration.class, node);
        }

        return (MethodDeclaration) node;
    }


    private Delta removeFromDeclaration(MethodDeclaration methodDeclaration) {
        final ASTRewrite rewrite = ASTRewrite.create(methodDeclaration.getRoot().getAST());
        rewrite.remove(methodDeclaration, null);
        return createDelta(methodDeclaration, rewrite);
    }
}
