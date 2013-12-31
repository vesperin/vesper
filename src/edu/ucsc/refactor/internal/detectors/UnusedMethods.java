package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.MethodInvocationVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.util.AstUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UnusedMethods extends IssueDetector {
    private static final String STRATEGY_NAME        = Smell.UNUSED_METHOD.getKey();
    private static final String STRATEGY_DESCRIPTION = Smell.UNUSED_METHOD.getSummary();

    private final List<MethodInvocation>                         methodInvocationList;
    private final Map<MethodDeclaration, List<MethodInvocation>> methodUsages;

    /**
     * Instantiate {@code UnusedMethods} issue detector.
     */
    public UnusedMethods() {
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);
        this.methodInvocationList = new ArrayList<MethodInvocation>();
        this.methodUsages         = new WeakHashMap<MethodDeclaration, List<MethodInvocation>>();
    }


    @Override public void scanJava(Context context) {
        MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
        context.accept(methodDeclarationVisitor);

        for (MethodDeclaration methodDeclaration : methodDeclarationVisitor.getMethodDeclarations()) {
            final Location location = context.locate(methodDeclaration);

            System.out.println("Entering ... " + location);

            methodUsages.put(methodDeclaration, new ArrayList<MethodInvocation>());
        }

        MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
        context.accept(methodInvocationVisitor);
        methodInvocationList.addAll(methodInvocationVisitor.getMethodInvocations());

        buildHashMapWithMethodDeclarationsAndInvocations();
        findViolatedNodesAndCreateIssues();
    }


    private void buildHashMapWithMethodDeclarationsAndInvocations() {
        for (MethodInvocation methodInvocation : methodInvocationList) {
            for (MethodDeclaration methodDeclaration : methodUsages.keySet()) {
                final IMethodBinding methodBinding = methodDeclaration.resolveBinding();
                if(methodBinding != null){
                    if (methodBinding.equals(methodInvocation.resolveMethodBinding())) {
                        methodUsages.get(methodDeclaration).add(methodInvocation);
                        break;
                    }
                }
            }
        }
    }


    /**
     * Finds all violated nodes and places them in the violatedNodes list.
     */
    private void findViolatedNodesAndCreateIssues() {
        OUTER_LOOP:
        for (Map.Entry<MethodDeclaration, List<MethodInvocation>> entry : methodUsages.entrySet()) {
            final MethodDeclaration methodDeclaration = entry.getKey();
            final int               modifiers         = methodDeclaration.getModifiers();

            if (Modifier.isPrivate(modifiers)) { continue; }

            for (MethodInvocation methodInvocation : entry.getValue()) {
                if (!Modifier.isPrivate(modifiers) && AstUtil.parent(TypeDeclaration.class,
                        methodDeclaration) != AstUtil.parent(TypeDeclaration.class,
                        methodInvocation)) {

                    continue OUTER_LOOP;
                }
            }

            if ((!Modifier.isPrivate(modifiers) && !methodDeclaration.isConstructor() && !Modifier.isStatic(modifiers)
                    && !AstUtil.hasAnnotation(methodDeclaration)
                    && !AstUtil.isMainMethod(methodDeclaration))
                    && !Modifier.isAbstract(AstUtil.parent(TypeDeclaration.class, methodDeclaration).getModifiers())
                    && !AstUtil.parent(TypeDeclaration.class, methodDeclaration).isInterface()) {

                createIssue(methodDeclaration);
            }
        }
    }
}
