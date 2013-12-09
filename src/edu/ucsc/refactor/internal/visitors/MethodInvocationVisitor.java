package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MethodInvocationVisitor extends ASTVisitor {

    private final List<MethodInvocation> methodInvocations;

    /**
     * Instantiates a new {@link MethodInvocationVisitor}.
     */
    public MethodInvocationVisitor() {
        methodInvocations = new ArrayList<MethodInvocation>();
    }

    /**
     * Adds all available methodInvocations to the methodInvocation list.
     *
     * @param methodInvocation a current method invocation in AST
     * @return whether the children of this node should be visited.
     */
    @Override public boolean visit(MethodInvocation methodInvocation) {
        if (methodInvocation.resolveMethodBinding() != null) {
            methodInvocations.add(methodInvocation);
        }
        return super.visit(methodInvocation);
    }

    public List<MethodInvocation> getMethodInvocations() {
        return methodInvocations;
    }
}
