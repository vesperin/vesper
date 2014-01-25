package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.visitors.RenameAstNodeVisitor;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Parameters;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameField extends SourceChanger {
    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.RENAME_FIELD);
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {
        final Change change  = new SourceChange(cause, this, parameters);
        final String newName = (String) parameters.get(Parameters.FIELD_NEW_NAME).getValue();

        try {
            TypeDeclaration unit = null;
            for(ASTNode each : cause.getAffectedNodes()){
                if(unit == null) { unit = AstUtil.parent(TypeDeclaration.class, each); }
                final FieldDeclaration variable = AstUtil.parent(FieldDeclaration.class, each);
                change.getDeltas().add(renameField(variable, unit, newName, Refactoring.from(cause.getName().getKey())));
            }
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private Delta renameField(FieldDeclaration field, TypeDeclaration unit, String newName, Refactoring refactoring){
        if(!Refactoring.RENAME_FIELD.isSame(refactoring)){
            throw new IllegalStateException(
                    "wrong refactoring strategy: expected RenameField, but got " + refactoring
            );
        }

        checkNameIsNotTaken(unit, newName);

        final AST        ast     = unit.getAST();
        final ASTRewrite rewrite = AstUtil.createAstRewrite(ast);

        final String     oldName = getSimpleName(field);
        final Source     src     = Source.from(field);

        final TypeDeclaration copy = AstUtil.copySubtree(
                TypeDeclaration.class,
                ast,
                unit
        );

        final Location copyLocation = Locations.locate(src, copy);
        final RenameAstNodeVisitor renameFieldVisitor = new RenameAstNodeVisitor(
                src, copyLocation, oldName, newName
        );

        renameFieldVisitor.setStrategy(refactoring);

        copy.accept(renameFieldVisitor);

        rewrite.replace(unit, copy, null);

        return createDelta(field, rewrite);
    }


    static String getSimpleName(FieldDeclaration field){
        final List fragments = field.fragments();
        final Object element = fragments.get(0);
        final VariableDeclarationFragment fragment = (VariableDeclarationFragment)element;
        return fragment.getName().getIdentifier();
    }

    private static void checkNameIsNotTaken(TypeDeclaration unit, String newName){
        final FieldDeclaration[] declarations = unit.getFields();
        for(FieldDeclaration each : declarations){
            if(getSimpleName(each).equals(newName)){
                throw new RuntimeException(newName + " is already taken!");
            }
        }
    }
}
