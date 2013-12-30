package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MethodDeclarationVisitor extends ASTVisitor {
    private List<MethodDeclaration> methodDeclarations = new ArrayList<MethodDeclaration>();
    private boolean includeConstructor  = false;


    @Override public boolean visit(MethodDeclaration node) {
        if (shouldAddMethod(node)) {
            methodDeclarations.add(node);
        }

        return super.visit(node);
    }

    public void includeConstructor(boolean includeConstructor) {
        this.includeConstructor = includeConstructor;
    }

    private boolean shouldAddMethod(MethodDeclaration node) {
        return !(node.isConstructor() && !includeConstructor);

    }

    public List<MethodDeclaration> getMethodDeclarations() {
        return Collections.unmodifiableList(methodDeclarations);
    }
}
