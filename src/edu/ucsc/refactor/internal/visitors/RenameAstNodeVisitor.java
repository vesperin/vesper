package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameAstNodeVisitor extends SourceVisitor {
    private final Source    src;
    private final Location  selection;
    private final String    oldName;
    private final String    newName;


    public RenameAstNodeVisitor(Source src, Location selection, String oldName, String newName){
        super(true);
        this.src        = src;
        this.selection  = selection;
        this.oldName    = oldName;
        this.newName    = newName;
    }

    @Override public boolean visit(MethodRef node){
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node)) {
            if(oldName.equals(node.getName().getIdentifier())){
                node.setName(node.getAST().newSimpleName(newName));
            }
        }

        return true;
    }


    @Override public boolean visit(MethodInvocation node) {
        if (!isFurtherTraversalNecessary(node)) {
            return false;
        }

        if (isNodeWithinSelection(node)) {
            if(oldName.equals(node.getName().getIdentifier())){
                node.setName(node.getAST().newSimpleName(newName));
            }
        }

        return true;
    }


    private boolean isNodeWithinSelection(ASTNode node) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = this.selection;


        return (Locations.inside(methodLocation, nodeLocation));
    }

    private boolean isNodeEnclosingMethod(ASTNode node) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = this.selection;

        // Is the method completely enclosed by the node?
        return (Locations.inside(nodeLocation, methodLocation));
    }


    private boolean isFurtherTraversalNecessary(ASTNode node) {
        return isNodeWithinSelection(node) || isNodeEnclosingMethod(node);
    }
}
