package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.MethodInvocationVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.util.AstUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UnusedParameters extends IssueDetector {
    private static final String STRATEGY_NAME        = Smell.UNUSED_PARAMETER.getKey();
    private static final String STRATEGY_DESCRIPTION = Smell.UNUSED_PARAMETER.getSummary();


    private final List<MethodDeclaration> methodDeclarations;
    private final List<MethodInvocation>    methodInvocations;

    /**
     * Instantiate a new issue detector.
     */
    public UnusedParameters() {
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);

        methodDeclarations = new ArrayList<MethodDeclaration>();
        methodInvocations  = new ArrayList<MethodInvocation>();
    }

    @Override public void scanJava(Context context) {
        // collect ALL method declarations
        final MethodDeclarationVisitor methodDeclareVisitor = new MethodDeclarationVisitor();
        context.accept(methodDeclareVisitor);
        methodDeclarations.addAll(methodDeclareVisitor.getMethodDeclarations());

        // collect ALL method invocations
        final MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
        context.accept(methodInvocationVisitor);
        methodInvocations.addAll(methodInvocationVisitor.getMethodInvocations());

        // check ALL variables for each method declaration
        for(MethodDeclaration eachMethodDeclaration : methodDeclarations){
            if (!Modifier.isAbstract(eachMethodDeclaration.getModifiers())
                    && !AstUtil.hasAnnotation(eachMethodDeclaration)
                    && !AstUtil.parent(TypeDeclaration.class, eachMethodDeclaration).isInterface()
                    && !AstUtil.isMainMethod(eachMethodDeclaration) && eachMethodDeclaration.parameters() != null) {

                checkAllVariables(eachMethodDeclaration, eachMethodDeclaration.parameters());

            }
        }

    }

    private void checkAllVariables(MethodDeclaration methodDeclaration, List declaredVariables) {
        for(Object eachDeclared : declaredVariables){
            final SingleVariableDeclaration variable = (SingleVariableDeclaration)eachDeclared;
            if(!AstUtil.usesVariable(methodDeclaration, variable)){
                final Issue issue = createIssue();
                issue.addNode(methodDeclaration);
                issue.addNode(variable);

                checkCorrespondingInvocations(methodDeclaration, issue);
            }
        }
    }

    private void checkCorrespondingInvocations(MethodDeclaration methodDeclaration, Issue issue) {
        for (MethodInvocation methodInvocation : methodInvocations) {
            final IMethodBinding binding = methodDeclaration.resolveBinding();
            final IMethodBinding other   = methodInvocation.resolveMethodBinding();
            if (binding.isEqualTo(other)) {
                issue.addNode(methodInvocation);
            }
        }

    }

    @Override public void resetThisDetector() {
        methodDeclarations.clear();
        methodInvocations.clear();
        super.resetThisDetector();
    }
}
