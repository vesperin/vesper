package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.util.AstUtil;
import org.eclipse.jdt.core.dom.*;

import static edu.ucsc.refactor.util.AstUtil.isFurtherTraversalNecessary;
import static edu.ucsc.refactor.util.AstUtil.isNodeWithinSelection;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameAstNodeVisitor extends ASTVisitor {
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


    @Override public boolean visit(SimpleName node) {
        if (!isFurtherTraversalNecessary(src, node, this.selection)) {
            return false;
        }

        if (isNodeWithinSelection(src, node, this.selection)) {
            if(oldName.equals(node.getIdentifier())){
                final VariableDeclaration declaration = AstUtil.getVariableDeclaration(node);
                if(declaration != null){
                    if(!(declaration.getParent() instanceof CompilationUnit)){
                        node.setIdentifier(newName);
                    }
                } else {
                    if(!(node.getParent() instanceof FieldAccess)){
                        node.setIdentifier(newName);
                    }
                }
            }
        }

        return true;
    }

}
