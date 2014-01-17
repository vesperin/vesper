package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class FieldDeclarationVisitor extends ASTVisitor {

    private List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();

    @Override public boolean visit(FieldDeclaration node) {
        final VariableDeclarationFragment fragment = (
                (VariableDeclarationFragment) node.fragments().get(0)
        );

        if(fragment.resolveBinding() != null){
            fieldDeclarations.add(node);
        }

        return super.visit(node);
    }

    public final List<FieldDeclaration> getFieldDeclarations() {
        return fieldDeclarations;
    }

    public final List<FieldDeclaration> getMatchingFieldDeclaration(String name){
        final List<FieldDeclaration> declarations = new ArrayList<FieldDeclaration>();
        for (FieldDeclaration fieldDeclaration : getFieldDeclarations()) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) fieldDeclaration.fragments().get(0);
            if (variableDeclaration.getName().toString().equalsIgnoreCase(name)) {
                declarations.add(fieldDeclaration);
            }
        }

        return declarations;
    }

    public boolean hasFieldName(String name) {
        for (FieldDeclaration fieldDeclaration : getFieldDeclarations()) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) fieldDeclaration.fragments().get(0);
            if (variableDeclaration.getName().toString().equals(name)) {
                return true;
            }
        }
        return false;
    }

}