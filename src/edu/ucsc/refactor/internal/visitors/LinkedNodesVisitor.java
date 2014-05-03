package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.internal.util.AstUtil;
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
        this.binding = AstUtil.getDeclaration(binding);
        this.result  = new ArrayList<SimpleName>();
    }

    @Override public boolean visit(SimpleName node) {
        IBinding binding    = node.resolveBinding();
        if (binding == null) {
            return false;
        }

        binding = AstUtil.getDeclaration(binding);

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

}
