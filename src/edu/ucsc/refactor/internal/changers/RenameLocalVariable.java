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
public class RenameLocalVariable extends SourceChanger {
    @Override public boolean canHandle(Cause cause) {
        return cause.isSame(Refactoring.RENAME_VARIABLE);
    }

    @Override protected Change initChanger(Cause cause, Map<String, Parameter> parameters) {
        final Change change  = new SourceChange(cause, this, parameters);
        final String newName = (String) parameters.get(Parameters.MEMBER_NEW_NAME).getValue();

        try {
            MethodDeclaration unit = null;
            for(ASTNode each : cause.getAffectedNodes()){
                if(unit == null) { unit = AstUtil.parent(MethodDeclaration.class, each); }
                final VariableDeclarationStatement variable = AstUtil.parent(VariableDeclarationStatement.class, each);
                if(variable != null){
                    change.getDeltas().add(renameVariable(variable, null, unit, newName, Refactoring.from(cause.getName().getKey())));
                } else {
                    final VariableDeclarationFragment fragment = AstUtil.parent(VariableDeclarationFragment.class, each);
                    change.getDeltas().add(renameVariable(null, fragment, unit, newName, Refactoring.from(cause.getName().getKey())));
                }
            }
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private Delta renameVariable(VariableDeclarationStatement variable, VariableDeclarationFragment fragment, MethodDeclaration method, String newName, Refactoring refactoring){
        if(!Refactoring.RENAME_VARIABLE.isSame(refactoring)){
            throw new IllegalStateException(
                    "wrong refactoring strategy: expected RenameVariable, but got " + refactoring
            );
        }

        // checking preconditions
        checkNameIsNotTaken(method, method.parameters(), newName);

        final AST           ast     = method.getAST();
        final ASTRewrite    rewrite = AstUtil.createAstRewrite(ast);
        final String        oldName = fragment != null ? fragment.getName().getIdentifier(): AstUtil.getSimpleName(variable);
        final Source        src     = Source.from(method);


        final MethodDeclaration copy = AstUtil.copySubtree(
                MethodDeclaration.class,
                ast,
                method
        );


        final Location copyLocation = Locations.locate(src, copy);
        final RenameAstNodeVisitor renameVariable = new RenameAstNodeVisitor(
                src, copyLocation, oldName, newName
        );

        renameVariable.setStrategy(refactoring);

        copy.accept(renameVariable);

        rewrite.replace(method, copy, null);

        return createDelta((fragment == null && variable != null ? variable : fragment), rewrite);
    }

    private static void checkNameIsNotTaken(MethodDeclaration methodDeclaration, List declaredVariables, String newName) {
        for(Object eachDeclared : declaredVariables){
            final SingleVariableDeclaration variable = (SingleVariableDeclaration)eachDeclared;
            if(AstUtil.usesVariable(methodDeclaration, variable)){
                if(newName.equals(variable.getName().getIdentifier())){
                    throw new RuntimeException(newName + " is already taken!");
                }
            }
        }
    }
}
