package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.internal.SourceVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MethodRefInJavaDocVisitor extends SourceVisitor {
    private final Set<MethodRef> methodReferences = new HashSet<MethodRef>();

    public MethodRefInJavaDocVisitor(){
        super(true);
    }

    @SuppressWarnings("unchecked") @Override public boolean visit(TagElement node) {
        final String tagName    = node.getTagName();
        List<Object> list       = node.fragments(); // unchecked warning

        int idx= 0;

        if (tagName != null && !list.isEmpty()) {
            Object first= list.get(0);
            if (first instanceof MethodRef) {
                if ("@throws".equals(tagName) || "@exception".equals(tagName)) {
                    final MethodRef name   = (MethodRef) first;
                    this.methodReferences.add(name);
                } else if ("@see".equals(tagName) || "@link".equals(tagName) || "@linkplain".equals(tagName)) {
                    final MethodRef name   = (MethodRef) first;
                    this.methodReferences.add(name);
                }

                idx++;
            }
        }

        for (int i  = idx; i < list.size(); i++) {
            doVisitNode((ASTNode) list.get(i));
        }

        return false;
    }

    public Set<MethodRef> getMethodReferences(){
        return Collections.unmodifiableSet(this.methodReferences);
    }


    private void doVisitNode(ASTNode node) {
        if (node != null) {
            node.accept(this);
        }
    }
}
