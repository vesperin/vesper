package edu.ucsc.refactor.internal.changers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedParameters extends SourceChanger {

    private final static int METHOD_DECLARATION             = 0;
    private final static int SINGLE_VARIABLE_DECLARATION    = 1;

    /**
     * Instantiates a new {@link RemoveUnusedParameters} object.
     */
    public RemoveUnusedParameters(){
        super();
    }

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.UNUSED_PARAMETER)
                || Names.from(Smell.UNUSED_PARAMETER).isSame(cause.getName());
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final SourceChange change = new SourceChange(cause, this, parameters);

        try {

            final CompilationUnit root      = getCompilationUnit(cause);
            final ASTRewrite      rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(removeUnusedParameters(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }


    private Delta removeUnusedParameters(CompilationUnit root, ASTRewrite rewrite, CauseOfChange cause){
        final boolean cameFromDetector = cause.getName().isSame(Smell.UNUSED_PARAMETER);

        final List<ASTNode> nodes   = cause.getAffectedNodes();
        final int           length  = nodes.size();

        if(cameFromDetector){
            // at least the method declaration, and the actual parameter
            Preconditions.checkArgument(length >= 2, "invalid detection of unused parameter");

            final MethodDeclaration         method    = AstUtil.exactCast(MethodDeclaration.class, nodes.get(METHOD_DECLARATION));
            final SingleVariableDeclaration parameter = AstUtil.exactCast(SingleVariableDeclaration.class, nodes.get(SINGLE_VARIABLE_DECLARATION));

            rewrite.remove(parameter, null);

            final Iterable<ASTNode> invocations = Iterables.skip(nodes, 2/*skip first two elements*/);

            for(ASTNode invocation : invocations){
                final MethodInvocation methodInvocation = AstUtil.exactCast(MethodInvocation.class, invocation);
                final Expression argument = (Expression) methodInvocation.arguments().get
                        (method.parameters().indexOf(parameter));

                rewrite.remove(argument, null);
            }
        } else {
            final SingleVariableDeclaration parameterReference = AstUtil.exactCast(SingleVariableDeclaration.class, nodes.get(0));
            final List<SimpleName>  usages  = AstUtil.findByNode(parameterReference.getParent(), parameterReference.getName());


            if(usages.size() > 1){
                throw new RuntimeException(
                        parameterReference.getName().getIdentifier() +
                                " cannot be deleted. It is used somewhere" +
                                " in the method's body."
                );
            }

        }



        return createDelta(root, rewrite);
    }
}
