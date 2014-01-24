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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameClassOrInterface extends SourceChanger {
    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.RENAME_TYPE);
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {
        final Change change  = new SourceChange(cause, this, parameters);
        final String newName = (String) parameters.get(Parameters.TYPE_NEW_NAME).getValue();

        try {
            for(ASTNode each : cause.getAffectedNodes()){
                final TypeDeclaration unit = AstUtil.parent(TypeDeclaration.class, each);
                change.getDeltas().add(renameType(unit, newName, Refactoring.from(cause.getName().getKey())));
            }
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private Delta renameType(TypeDeclaration unit, String newName, Refactoring refactoring){
        if(!Refactoring.RENAME_TYPE.isSame(refactoring)){
            throw new IllegalStateException(
                    "wrong refactoring strategy: expected RenameType, but got " + refactoring
            );
        }

        checkNameIsNotTaken(unit, newName);

        final AST           ast     = unit.getAST();
        final ASTRewrite    rewrite = AstUtil.createAstRewrite(ast);
        final String        oldName = unit.getName().getIdentifier();
        final Source        src     = Source.from(unit);

        final TypeDeclaration copy = AstUtil.copySubtree(
                TypeDeclaration.class,
                ast,
                unit
        );

        final Location copyLocation = Locations.locate(src, copy);
        final RenameAstNodeVisitor renameType = new RenameAstNodeVisitor(
                src, copyLocation, oldName, newName
        );

        renameType.setStrategy(refactoring);

        copy.accept(renameType);

        rewrite.replace(unit, copy, null);

        src.setName(newName);

        return createDelta(src, rewrite);
    }

    private static void checkNameIsNotTaken(TypeDeclaration unit, String newName){
        if(unit.getName().getIdentifier().equals(newName)){
            throw new RuntimeException(newName + " is already taken!");
        }

        for(TypeDeclaration declaration : unit.getTypes()){
            if(declaration.getName().getIdentifier().equals(newName)){
                throw new RuntimeException(newName + " is already taken!");
            }
        }
    }
}
