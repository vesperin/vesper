package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SideEffectNodesVisitor extends ASTVisitor {
    private final List<Expression> sideEffectNodes;

    public SideEffectNodesVisitor(){
        this.sideEffectNodes = new ArrayList<Expression>();
    }


    @Override public boolean visit(Assignment node) {
        sideEffectNodes.add(node);
        return false;
    }

    @Override public boolean visit(PostfixExpression node) {
        sideEffectNodes.add(node);
        return false;
    }

    @Override public boolean visit(PrefixExpression node) {
        Object operator= node.getOperator();
        if (operator == PrefixExpression.Operator.INCREMENT || operator == PrefixExpression.Operator.DECREMENT) {
            sideEffectNodes.add(node);
            return false;
        }
        return true;
    }

    @Override public boolean visit(MethodInvocation node) {
        sideEffectNodes.add(node);
        return false;
    }

    @Override public boolean visit(ClassInstanceCreation node) {
        sideEffectNodes.add(node);
        return false;
    }

    @Override public boolean visit(SuperMethodInvocation node) {
        sideEffectNodes.add(node);
        return false;
    }

    public List<Expression> getSideEffectNodes(){
        return sideEffectNodes;
    }
}
