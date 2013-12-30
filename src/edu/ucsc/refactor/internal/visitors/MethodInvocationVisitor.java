package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MethodInvocationVisitor extends ASTVisitor {

    private final Set<MethodInvocation> methodInvocations;

    /**
     * Instantiates a new {@link MethodInvocationVisitor}.
     */
    public MethodInvocationVisitor() {
        methodInvocations = new HashSet<MethodInvocation>();
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
        } else{ // unable to resolve method binding b/c we are not using Eclipse's Java Model
            methodInvocations.add(methodInvocation);
        }
        return super.visit(methodInvocation);
    }

    public Set<MethodInvocation> getMethodInvocations() {
        return methodInvocations;
    }
}
