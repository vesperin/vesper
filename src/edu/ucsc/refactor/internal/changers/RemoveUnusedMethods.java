package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedMethods extends SourceChanger {
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

            final CompilationUnit root      = getCompilationUnit(cause);
            final ASTRewrite      rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(removeUnusedMethods(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }


    private Delta removeUnusedMethods(CompilationUnit root, ASTRewrite rewrite, CauseOfChange cause){
        final boolean cameFromDetector = cause.getName().isSame(Smell.UNUSED_METHOD);

        for(ASTNode affected : cause.getAffectedNodes()){
            final MethodDeclaration declaration = getMethodDeclaration(affected);
            if(cameFromDetector){
               rewrite.remove(declaration, null);
            } else {
                final List<SimpleName>  usages  = AstUtil.findByNode(root, declaration.getName());
                if(usages.size() > 1){
                    throw new RuntimeException(
                            declaration.getName().getIdentifier() +
                                    " cannot be deleted. It is used somewhere" +
                                    " in the Source."
                    );
                }
            }
        }



        return createDelta(root, rewrite);
    }


    private static MethodDeclaration getMethodDeclaration(ASTNode node){
        if(node instanceof SimpleName){
            return AstUtil.parent(MethodDeclaration.class, node);
        }

        return AstUtil.exactCast(MethodDeclaration.class, node);
    }

}
