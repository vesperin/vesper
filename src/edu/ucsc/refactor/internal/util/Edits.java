package edu.ucsc.refactor.internal.util;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.SingleEdit;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Edits {
    private Edits(){
        throw new RuntimeException("Edits cannot be instantiated.");
    }


    /**
     * Resolves rename* refactorings.
     *
     * @param edit The edit to be resolved
     * @return The resolved edit, or simply the unresolved edit if rename selection is not found.
     */
    public static SingleEdit resolve(SingleEdit edit){

        if(Refactoring.RENAME_SELECTION.isSame(edit.getName())){
            Preconditions.checkArgument(
                    edit.getAffectedNodes().size() == 1,
                    "exactly one selected member; no more"
            );

            final ASTNode node = edit.getAffectedNodes().get(0);

            final SingleEdit resolved;
            if(isClass(node)) {
                resolved = SingleEdit.renameClassOrInterface(edit.getSourceSelection());
                resolved.addNode(node);
            } else if(isMethod(node)) {
                resolved = SingleEdit.renameMethod(edit.getSourceSelection());
                resolved.addNode(node);
            } else if(isMethodInvocation(node)){
                final ASTNode  methodDeclaration = AstUtil.getMethodDeclaration(node);
                final Location location          = Locations.locate(methodDeclaration);
                resolved = SingleEdit.renameMethod(new SourceSelection(location));
                resolved.addNode(methodDeclaration);
            } else if(isParameter(node)){
                resolved = SingleEdit.renameParameter(edit.getSourceSelection());
                resolved.addNode(node);
            } else if(isField(node)){
                resolved = SingleEdit.renameField(edit.getSourceSelection());
                resolved.addNode(node);
            } else if(isFieldAccess(node)){
                final FieldDeclaration  fieldDeclaration = AstUtil.getFieldDeclaration(node);
                final Location          location         = Locations.locate(fieldDeclaration);
                resolved = SingleEdit.renameField(new SourceSelection(location));
                resolved.addNode(fieldDeclaration);
            }  else {
                throw new RuntimeException("invalid selection");
            }

            return resolved;

        } else if(Refactoring.DELETE_REGION.isSame(edit.getName())){
            Preconditions.checkArgument(
                    edit.getAffectedNodes().size() >= 1,
                    "at least one selected member; no less"
            );

            if(edit.getAffectedNodes().size() == 1){
                final ASTNode node = edit.getAffectedNodes().get(0);

                final SingleEdit resolved;
                if(isClass(node)){
                    resolved = SingleEdit.deleteClass(edit.getSourceSelection());
                    resolved.addNode(node);
                } else if(isMethod(node)) {
                    resolved = SingleEdit.deleteMethod(edit.getSourceSelection());
                    resolved.addNode(node);
                } else if(isMethodInvocation(node)) {
                    final ASTNode  methodDeclaration = AstUtil.getMethodDeclaration(node);
                    final Location location          = Locations.locate(methodDeclaration);
                    resolved = SingleEdit.deleteMethod(new SourceSelection(location));
                    resolved.addNode(methodDeclaration);
                } else if(isParameter(node)){
                    resolved = SingleEdit.deleteParameter(edit.getSourceSelection());
                    resolved.addNode(node);
                } else if(isField(node)){
                    resolved = SingleEdit.deleteField(edit.getSourceSelection());
                    resolved.addNode(node);
                } else if(isFieldAccess(node)){
                    final FieldDeclaration  fieldDeclaration = AstUtil.getFieldDeclaration(node);
                    final Location          location         = Locations.locate(fieldDeclaration);
                    resolved = SingleEdit.deleteField(new SourceSelection(location));
                    resolved.addNode(fieldDeclaration);
                } else {
                    return edit; // either is a comment, block comment.
                }

                return resolved;
            }
        }

        return edit;
    }

    private static boolean isFieldAccess(ASTNode node) {
        return AstUtil.getFieldDeclaration(node) != null;
    }

    private static boolean isField(ASTNode node) {
        return (AstUtil.isOfType(FieldDeclaration.class, node)
                ||  AstUtil.isParent(node, AstUtil.immediateAncestor(FieldDeclaration.class, node.getParent())))
                || (AstUtil.isOfType(VariableDeclarationFragment.class, node)
        || AstUtil.isParent(node, AstUtil.immediateAncestor(VariableDeclarationFragment.class, node.getParent())));
    }

    private static boolean isParameter(ASTNode node) {
        return AstUtil.isOfType(SingleVariableDeclaration.class, node)
                ||  AstUtil.isParent(node, AstUtil.immediateAncestor(SingleVariableDeclaration.class, node.getParent()));
    }

    private static boolean isMethod(ASTNode node) {
        return AstUtil.isOfType(MethodDeclaration.class, node)
                ||  AstUtil.isParent(node, AstUtil.immediateAncestor(MethodDeclaration.class, node.getParent()));
    }

    private static boolean isMethodInvocation(ASTNode node) {
        return AstUtil.isOfType(MethodInvocation.class, node)
                || AstUtil.isParent(node, AstUtil.immediateAncestor(MethodInvocation.class, node.getParent()));
    }


    private static boolean isClass(ASTNode node) {
        return AstUtil.isOfType(TypeDeclaration.class, node)
                ||  AstUtil.isParent(node, AstUtil.immediateAncestor(TypeDeclaration.class, node.getParent()));
    }
}
