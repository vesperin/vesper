package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.NoSuchElementException;

import static edu.ucsc.refactor.internal.util.AstUtil.isFurtherTraversalNecessary;
import static edu.ucsc.refactor.internal.util.AstUtil.isNodeWithinSelection;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameAstNodeVisitor extends ASTVisitor {
    private final Source    src;
    private final Location  selection;
    private final String    oldName;
    private final String    newName;


    private Refactoring     refactoring;


    public RenameAstNodeVisitor(Source src, Location selection, String oldName, String newName){
        super(true);
        this.src            = src;
        this.selection      = selection;
        this.oldName        = oldName;
        this.newName        = newName;
        this.refactoring    = null;
    }


    public void setStrategy(Refactoring refactoring){
        if(!refactoring.isSame(this.refactoring)){
            this.refactoring = refactoring;
        }
    }


    @Override public boolean visit(SimpleName node) {
        if (!isFurtherTraversalNecessary(src, node, this.selection)) {
            return false;
        }

        if (isNodeWithinSelection(src, node, this.selection)) {
            if(oldName.equals(node.getIdentifier())){
                final VariableDeclaration declaration = AstUtil.getVariableDeclaration(node);
                switch (this.refactoring){
                    case RENAME_PARAMETER:
                        renameMethodParameter(node, declaration, newName);
                        break;
                    case RENAME_METHOD:
                        renameMethod(node, declaration, newName);
                        break;
                    case RENAME_FIELD:
                        renameField(node, declaration, newName);
                        break;
                    case RENAME_TYPE: // rename class or interface
                        renameType(node, newName);
                        break;
                    default:
                        throw new NoSuchElementException(this.refactoring + " not found!");
                }
            }
        }

        return true;
    }


    static void renameMethodParameter(SimpleName node, VariableDeclaration declaration, String newName){
        if(declaration != null){
            if(!(declaration.getParent() instanceof FieldAccess)){
                node.setIdentifier(newName);
            }
        } else {
            if(!(node.getParent() instanceof FieldAccess)){ // local variables
                node.setIdentifier(newName);
            }
        }

    }

    static void renameMethod(SimpleName node, VariableDeclaration declaration, String newName){
        if(declaration == null){
            if(!(node.getParent() instanceof FieldAccess)){ // method declarations and invocations
                node.setIdentifier(newName);
            }
        }
    }

    static void renameField(SimpleName node, VariableDeclaration declaration, String newName){
        // todo(Huascar) should we rename the getter and setter?
        if(declaration == null){
            if((node.getParent() instanceof FieldAccess)
                || node.getParent() instanceof Assignment){ // field
                node.setIdentifier(newName);
            }
        } else {
            if(declaration.getParent() instanceof FieldDeclaration){
                node.setIdentifier(newName);
            }
        }
    }

    static void renameType(SimpleName node, String newName){
        if(node.getParent() instanceof TypeDeclaration
                || node.getParent() instanceof ConstructorInvocation
                || node.getParent() instanceof MethodRef){
            node.setIdentifier(newName);
        }
    }

}
