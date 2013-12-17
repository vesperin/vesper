package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NumberLiteral;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MagicNumberVisitor extends ASTVisitor {
    private Set<ASTNode> magicNumbers;

    /**
     * Constructs a new {@code MagicNumberVisitor}.
     */
    public MagicNumberVisitor() {
        magicNumbers = new HashSet<ASTNode>();
    }

    @Override public boolean visit(NumberLiteral node) {
        final ASTNode parent = node.getParent();
        if (parent.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
            magicNumbers.add(node);
        }
        return super.visit(node);
    }


    public Set<ASTNode> getMagicNumbers() {
        return Collections.unmodifiableSet(magicNumbers);
    }
}
