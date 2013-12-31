package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameParameterVisitor extends SourceVisitor {
    private final Source src;
    private final Location selection;
    private final String    oldName;
    private final String    newName;


    public RenameParameterVisitor(Source src, Location selection, String oldName, String newName){
        super(true);
        this.src        = src;
        this.selection  = selection;
        this.oldName    = oldName;
        this.newName    = newName;
    }


    @Override public boolean visit(SingleVariableDeclaration node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node) || isNodeExactlyAtLocation(node)) {
            if(oldName.equals(node.getName().getIdentifier())){
                node.setName(node.getAST().newSimpleName(newName));
            }
        }

        return true;
    }


    @Override public boolean visit(Block node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }
        // Visits a block node. Stops traversing the tree if we come to a new class.
        final Object first = node.statements().get(0);
        return !(first instanceof TypeDeclaration) && super.visit(node);
    }

    @Override public boolean visit(MethodInvocation node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node)) {
            for(Object eachArg : node.arguments()){
                final SimpleName next = (SimpleName)eachArg;
                if(oldName.equals(next.getIdentifier())){
                    next.setIdentifier(newName);
                }
            }
        }

        return true;
    }




    @Override public boolean visit(VariableDeclarationFragment node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }


        if (isNodeWithinSelection(node)){
            if(node.getInitializer() instanceof SimpleName){
                if(oldName.equals(((SimpleName)node.getInitializer()).getIdentifier())){
                    ((SimpleName) node.getInitializer()).setIdentifier(newName);
                }
            } else {
                doVisitNode(node.getInitializer());
            }
        }

        return false;
    }




    @Override public boolean visit(InfixExpression node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node)){

            if(node.getLeftOperand() instanceof SimpleName){
                if(oldName.equals(((SimpleName)node.getLeftOperand()).getIdentifier())){
                    ((SimpleName) node.getLeftOperand()).setIdentifier(newName);
                }
            } else {
                doVisitNode(node.getLeftOperand());
            }

            if(node.getRightOperand() instanceof SimpleName){
                if(oldName.equals(((SimpleName)node.getRightOperand()).getIdentifier())){
                    ((SimpleName) node.getRightOperand()).setIdentifier(newName);
                }
            } else {
                doVisitNode(node.getRightOperand());
            }

            for(Object extended : node.extendedOperands()){
                if(extended instanceof SimpleName){
                    final SimpleName next = (SimpleName) extended;
                    if(oldName.equals(next.getIdentifier())){
                        next.setIdentifier(newName);
                    }
                } else {
                    doVisitNode((ASTNode) extended);
                }
            }

        }

        return false;
    }

    @Override public boolean visit(PostfixExpression node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node)){
            if(node.getOperand() instanceof SimpleName){
                if(oldName.equals(((SimpleName)node.getOperand()).getIdentifier())){
                    ((SimpleName) node.getOperand()).setIdentifier(newName);
                }
            } else {
                doVisitNode(node.getOperand());
            }
        }

        return false;
    }

    @Override public boolean visit(PrefixExpression node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node)){
            if(node.getOperand() instanceof SimpleName){
                if(oldName.equals(((SimpleName)node.getOperand()).getIdentifier())){
                    ((SimpleName) node.getOperand()).setIdentifier(newName);
                }
            } else {
                doVisitNode(node.getOperand());
            }
        }

        return true;
    }


    @Override
    public boolean visit(ParenthesizedExpression node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node)){
           doVisitNode(node.getExpression());
        }

        return false;
    }

    private boolean isNodeWithinSelection(ASTNode node) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = this.selection;


        return (Locations.inside(methodLocation, nodeLocation))
                || (Locations.covers(methodLocation, nodeLocation));
    }

    private boolean isNodeEnclosingMethod(ASTNode node) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = this.selection;

        // Is the method completely enclosed by the node?
        return (Locations.inside(nodeLocation, methodLocation));
    }


    private boolean isNodeExactlyAtLocation(ASTNode node) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = this.selection;

        // Is the method at the same position as the other node?
        return (Locations.bothSame(nodeLocation, methodLocation));
    }


    private boolean isFurtherTraversalNecessary(ASTNode node) {
        return isNodeWithinSelection(node)
                || isNodeEnclosingMethod(node)
                || isNodeExactlyAtLocation(node);
    }

    private void doVisitNode(ASTNode node) {
        if (node != null) {
            node.accept(this);
        }
    }
}
