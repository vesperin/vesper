package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedParameters extends SourceChanger {

    private final static int METHOD_DECLARATION             = 0;
    private final static int SINGLE_VARIABLE_DECLARATION    = 1;
    private final static int START_METHOD_INVOCATIONS       = 2;


    /**
     * Instantiates a new {@link RemoveUnusedParameters} object.
     */
    public RemoveUnusedParameters(){
        super();
    }

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.UNUSED_PARAMETER);
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final SourceChange solution = new SourceChange(cause, this, parameters);

        try {
            final MethodDeclaration         methodDeclaration   = getMethodDeclarationNode(cause);
            final SingleVariableDeclaration variableDeclaration = getSingleVariableDeclarationNode(cause);



            //1. Remove parameter from the method declaration
            solution.getDeltas().add(removeFromDeclaration(methodDeclaration, variableDeclaration));

            //2. Remove parameter from method invocations
            solution.getDeltas().addAll(removeFromInvocations(cause, methodDeclaration,
                    variableDeclaration));
        } catch (Throwable ex){
            solution.getErrors().add(ex.getMessage());
        }

        return solution;
    }


    private List<Delta> removeFromInvocations(CauseOfChange cause,
                                              MethodDeclaration methodDeclaration,
                                              SingleVariableDeclaration singleVariableDeclaration) {

        final List<Delta> deltas = new ArrayList<Delta>();
        final int size = cause.getAffectedNodes().size();

        if (size > START_METHOD_INVOCATIONS) {
            final List<ASTNode> methodInvocations;
            methodInvocations = cause.getAffectedNodes().subList(
                    START_METHOD_INVOCATIONS,
                    size
            );

            ASTRewrite rewrite;

            for (ASTNode method : methodInvocations) {
                final MethodInvocation methodInvocation = (MethodInvocation) method;
                final Expression argument = (Expression) methodInvocation.arguments().get
                        (methodDeclaration.parameters().indexOf(singleVariableDeclaration));

                rewrite = ASTRewrite.create(method.getAST());
                rewrite.remove(argument, null);

                deltas.add(createDelta(methodInvocation, rewrite));
            }
        }

        return deltas;
    }

    private Delta removeFromDeclaration(MethodDeclaration methodDeclaration,
                                        SingleVariableDeclaration variableDeclaration) {
        final ASTRewrite rewrite = ASTRewrite.create(methodDeclaration.getAST());
        rewrite.remove(variableDeclaration, null);
        return createDelta(methodDeclaration, rewrite);
    }

    private static MethodDeclaration getMethodDeclarationNode(CauseOfChange cause){
        return (MethodDeclaration) getNode(
                cause,
                METHOD_DECLARATION
        );
    }

    private static SingleVariableDeclaration getSingleVariableDeclarationNode(CauseOfChange cause){
        return (SingleVariableDeclaration) getNode(
                cause,
                SINGLE_VARIABLE_DECLARATION
        );
    }

    private static ASTNode getNode(CauseOfChange cause, int index){
        return cause.getAffectedNodes().get(index);
    }
}
