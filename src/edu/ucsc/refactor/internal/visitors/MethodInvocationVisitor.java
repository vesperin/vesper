package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MethodInvocationVisitor extends ASTVisitor {

    private final Set<MethodInvocation>         methodInvocations;
    private final AtomicReference<SimpleName>   targetInvocation;

    /**
     * Instantiates a new {@link MethodInvocationVisitor}.
     */
    public MethodInvocationVisitor() {
        this(null);
    }

    /**
     * Instantiates a new {@link MethodInvocationVisitor} with the matching method name
     * as a value.
     *
     * @param invoke The method name this visitor is looking for.
     */
    public MethodInvocationVisitor(SimpleName invoke){
        this(new HashSet<MethodInvocation>(), invoke);
    }

    /**
     * Instantiates a new {@link MethodInvocationVisitor} with the data structure to store
     * collected method invocations, and the matching method name as values.
     *
     * @param collector The data structure used to store the found invocations.
     * @param invoke The method name this visitor is looking for.
     */
    MethodInvocationVisitor(Set<MethodInvocation> collector, SimpleName invoke){
        this.methodInvocations = collector;
        this.targetInvocation  = new AtomicReference<SimpleName>(invoke);
    }

    /**
     * Adds all available methodInvocations to the methodInvocation list.
     *
     * @param methodInvocation a current method invocation in AST
     * @return whether the children of this node should be visited.
     */
    @Override public boolean visit(MethodInvocation methodInvocation) {
        if (methodInvocation.resolveMethodBinding() != null) {
            if(isSimpleNameMatchingRequired()){
                if(inTheClub(methodInvocation.getName(), targetInvocation.get())){
                    methodInvocations.add(methodInvocation);
                }
            } else {
                methodInvocations.add(methodInvocation);
            }
        } else{ // unable to resolve method binding b/c we are not using Eclipse's Java Model
            if(isSimpleNameMatchingRequired()){
                if(inTheClub(methodInvocation.getName(), targetInvocation.get())){
                    methodInvocations.add(methodInvocation);
                }
            } else {
                methodInvocations.add(methodInvocation);
            }
        }
        return super.visit(methodInvocation);
    }


    private static boolean inTheClub(SimpleName traversed, SimpleName target){
        return traversed.getIdentifier().equals(target.getIdentifier());
    }


    private boolean isSimpleNameMatchingRequired(){
        return this.targetInvocation.get() != null;
    }

    public Set<MethodInvocation> getMethodInvocations() {
        return methodInvocations;
    }
}
