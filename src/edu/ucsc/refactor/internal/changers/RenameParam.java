package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.visitors.RenameParameterVisitor;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.AstUtil;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Parameters;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameParam extends SourceChanger {
    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.RENAME_PARAMETER);
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {

        final Change change  = new SourceChange(cause, this, parameters);
        final String newName = (String) parameters.get(Parameters.PARAMETER_NEW_NAME).getValue();

        try {
            MethodDeclaration method = null;
            for(ASTNode each : cause.getAffectedNodes()){
                if(method == null) { method = AstUtil.parent(MethodDeclaration.class, each); }
                final SingleVariableDeclaration variable = AstUtil.parent(SingleVariableDeclaration.class, each);
                change.getDeltas().add(renameParameter(variable, method, newName));
            }
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private Delta renameParameter(SingleVariableDeclaration node, MethodDeclaration method, String newName){
        final AST           ast     = method.getAST();
        // todo(Huascar) avoid using multiple rewrites,
        final ASTRewrite    rewrite = ASTRewrite.create(ast);
        final String        oldName = node.getName().getIdentifier();
        final Source        src     = Source.from(method);

        // checking preconditions
        checkNameIsNotTaken(method, method.parameters(), newName);

        final MethodDeclaration copy = AstUtil.copySubtree(
                MethodDeclaration.class,
                ast,
                method
        );


        final Location copyLocation = Locations.locate(src, copy);
        final RenameParameterVisitor renameParameterVisitor = new RenameParameterVisitor(
                src, copyLocation, oldName, newName
        );

        copy.accept(renameParameterVisitor);

        rewrite.replace(method, copy, null);

        return createDelta(node, rewrite);
    }


    private static void checkNameIsNotTaken(MethodDeclaration methodDeclaration, List declaredVariables, String newName) {
        for(Object eachDeclared : declaredVariables){
            final SingleVariableDeclaration variable = (SingleVariableDeclaration)eachDeclared;
            if(AstUtil.usesVariable(methodDeclaration, variable)){
               if(newName.equals(variable.getName().getIdentifier())){
                   throw new RuntimeException(newName + " is already taken! Please select another name.");
               }
            }
        }
    }
}
