package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
// todo(Huascar) use it to see if its possible to delete a field or a param.
// if no linked nodes are available, then we can delete them
public class LinkedNodesVisitor extends ASTVisitor {
    private final IBinding         binding;
    private final List<SimpleName> result;

    /**
     * Construct a visitor that find all nodes connected to the given binding.
     *
     * @param binding The linked binding.
     */
    public LinkedNodesVisitor(IBinding binding) {
        super(true);
        this.binding = getDeclaration(binding);
        this.result  = new ArrayList<SimpleName>();
    }

    @Override public boolean visit(SimpleName node) {
        IBinding binding    = node.resolveBinding();
        if (binding == null) {
            return false;
        }

        binding= getDeclaration(binding);

        if (this.binding == binding) {
            result.add(node);
        } else if (binding.getKind() != this.binding.getKind()) {
            return false;
        } else if (binding.getKind() == IBinding.METHOD) {
            final IMethodBinding currentBinding = (IMethodBinding) binding;
            final IMethodBinding methodBinding  = (IMethodBinding) this.binding;
            if (methodBinding.overrides(currentBinding) || currentBinding.overrides(methodBinding)) {
                result.add(node);
            }
        }
        return false;
    }

    public List<SimpleName> getLinkedNodes(){
        return result;
    }

    private static IBinding getDeclaration(IBinding binding) {
        if (binding instanceof ITypeBinding) {
            return ((ITypeBinding) binding).getTypeDeclaration();
        } else if (binding instanceof IMethodBinding) {
            IMethodBinding methodBinding= (IMethodBinding) binding;
            if (methodBinding.isConstructor()) { // link all constructors with their type
                return methodBinding.getDeclaringClass().getTypeDeclaration();
            } else {
                return methodBinding.getMethodDeclaration();
            }
        } else if (binding instanceof IVariableBinding) {
            return ((IVariableBinding) binding).getVariableDeclaration();
        }
        return binding;
    }

}
