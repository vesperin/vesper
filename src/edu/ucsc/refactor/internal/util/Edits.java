package edu.ucsc.refactor.internal.util;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Edit;
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
    public static Edit resolve(Edit edit){

        if(Refactoring.RENAME_SELECTION.isSame(edit.getName())){
            Preconditions.checkArgument(
                    edit.getAffectedNodes().size() == 1,
                    "exactly one selected member; no more"
            );

            final ASTNode node = edit.getAffectedNodes().get(0);

            final Edit resolved;
            if(AstUtil.isClass(node)) {
                final TypeDeclaration   declaration = AstUtil.getTypeDeclaration(node);
                final Location          location    = Locations.locate(declaration);
                resolved = Edit.renameClassOrInterface(new SourceSelection(location));
                resolved.addNode(declaration);
            } else if(AstUtil.isMethod(node)) {
                final MethodDeclaration declaration = AstUtil.getMethodDeclaration(node);
                final Location          location    = Locations.locate(declaration);
                resolved = Edit.renameMethod(new SourceSelection(location));
                resolved.addNode(declaration);
            } else if(AstUtil.isParameter(node)) {
                final VariableDeclaration       variableDeclaration = AstUtil.getVariableDeclaration(node);
                final SingleVariableDeclaration declaration         = (SingleVariableDeclaration) variableDeclaration;
                resolved = Edit.renameParameter(new SourceSelection(Locations.locate(declaration)));
                resolved.addNode(declaration);
            } else if(AstUtil.isField(node)){
                final FieldDeclaration  fieldDeclaration = AstUtil.getFieldDeclaration(node);
                final Location          location         = Locations.locate(fieldDeclaration);
                resolved = Edit.renameField(new SourceSelection(location));
                resolved.addNode(fieldDeclaration);
            } else if(AstUtil.isLocalVariable(node)){
                final VariableDeclaration           variableDeclaration = AstUtil.getVariableDeclaration(node);
                final VariableDeclarationStatement  declaration         = AstUtil.parent(VariableDeclarationStatement.class, variableDeclaration);

                final Location location;
                if(declaration == null){
                    final VariableDeclarationFragment  fragment         = AstUtil.parent(VariableDeclarationFragment.class, variableDeclaration);
                    location            = Locations.locate(fragment);
                    resolved = Edit.renameLocalVariable(new SourceSelection(location));
                    resolved.addNode(fragment);
                } else {
                    location            = Locations.locate(declaration);
                    resolved = Edit.renameLocalVariable(new SourceSelection(location));
                    resolved.addNode(declaration);
                }

            } else {
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

                final Edit resolved;
                if(AstUtil.isClass(node)){
                    final TypeDeclaration   declaration = AstUtil.getTypeDeclaration(node);
                    final Location          location    = Locations.locate(declaration);
                    resolved = Edit.deleteClass(new SourceSelection(location));
                    resolved.addNode(declaration);
                } else if(AstUtil.isMethod(node)) {
                    final MethodDeclaration declaration = AstUtil.getMethodDeclaration(node);
                    final Location          location    = Locations.locate(declaration);
                    resolved = Edit.deleteMethod(new SourceSelection(location));
                    resolved.addNode(declaration);
                } else if(AstUtil.isParameter(node)){
                    final VariableDeclaration       variableDeclaration = AstUtil.getVariableDeclaration(node);
                    final SingleVariableDeclaration declaration         = (SingleVariableDeclaration) variableDeclaration;
                    resolved = Edit.deleteParameter(new SourceSelection(Locations.locate(declaration)));
                    resolved.addNode(declaration);
                } else if(AstUtil.isField(node)){
                    final VariableDeclaration   variableDeclaration = AstUtil.getVariableDeclaration(node);
                    final FieldDeclaration      fieldDeclaration    = AstUtil.parent(FieldDeclaration.class, variableDeclaration);
                    final Location              location            = Locations.locate(fieldDeclaration);
                    resolved = Edit.deleteField(new SourceSelection(location));
                    resolved.addNode(fieldDeclaration);
                } else if(AstUtil.isLocalVariable(node)){

                    final VariableDeclaration           variableDeclaration = AstUtil.getVariableDeclaration(node);
                    final VariableDeclarationStatement  declaration         = AstUtil.parent(VariableDeclarationStatement.class, variableDeclaration);

                    final Location location;
                    if(declaration == null){
                        final VariableDeclarationFragment  fragment         = AstUtil.parent(VariableDeclarationFragment.class, variableDeclaration);
                        location            = Locations.locate(fragment);
                        resolved            = Edit.deleteLocalVariable(new SourceSelection(location));
                        resolved.addNode(fragment);
                    } else {
                        location            = Locations.locate(declaration);
                        resolved            = Edit.deleteLocalVariable(new SourceSelection(location));
                        resolved.addNode(declaration);
                    }
                } else {
                    return edit; // either is a comment, block comment.
                }

                return resolved;
            }
        }

        return edit;
    }

}
