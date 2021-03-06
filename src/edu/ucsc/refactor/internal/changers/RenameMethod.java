package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.RenameAstNodeVisitor;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Parameters;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RenameMethod extends SourceChanger {

    @Override public boolean canHandle(Cause cause) {
        return cause.isSame(Refactoring.RENAME_METHOD);
    }

    @Override protected Change initChanger(Cause cause,
                                           Map<String, Parameter> parameters) {

        final Change change  = new SourceChange(cause, this, parameters);
        final String newName = (String) parameters.get(Parameters.MEMBER_NEW_NAME).getValue();
        try {
            for(ASTNode each : cause.getAffectedNodes()){ // assumption, there is only one affected node
                final Delta delta = renameMethod(
                        each,
                        newName,
                        Refactoring.from(cause.getName().getKey())
                );

                change.getDeltas().add(delta);
            }
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private Delta renameMethod(ASTNode node, String newName, Refactoring refactoring){
        if(!Refactoring.RENAME_METHOD.isSame(refactoring)){
            throw new IllegalStateException(
                    "wrong refactoring strategy: expected RenameMethod, but got " + refactoring
            );
        }

        final MethodDeclaration method      = AstUtil.parent(MethodDeclaration.class, node);
        final CompilationUnit   parent      = AstUtil.parent(CompilationUnit.class, method);


        final AST               ast     = parent.getAST();
        final ASTRewrite        rewrite = AstUtil.createAstRewrite(ast);
        final String            oldName = method.getName().getIdentifier();
        final Source            src     = Source.from(method);

        final MethodDeclarationVisitor  methodsVisitor  = new MethodDeclarationVisitor();
        parent.accept(methodsVisitor);

        final List<MethodDeclaration>  methods  = methodsVisitor.getMethodDeclarations();

        checkNameNotTaken(methods, newName);

        // I. Rename the actual method's name.
        // begin:

        internalRename(oldName, newName, method, src, rewrite, refactoring);

        // :end

        // II. look for method invocations and references (in JavaDocs) of original method
        // and then rename them using the `new name`.
        // begin:


        for(MethodDeclaration declaration : methods){
            if(declaration.getName().getIdentifier().equals(oldName)){
                continue;
            }

            internalRename(oldName, newName, declaration, src, rewrite, refactoring);
        }

        // :end

        return createDelta(node, rewrite);
    }

    private void checkNameNotTaken(List<MethodDeclaration> methods, String newName) {
        for(MethodDeclaration each : methods){
            if(newName.equals(each.getName().getIdentifier())){
                throw new RuntimeException(newName + " is already taken!");
            }
        }
    }

    private void internalRename(String oldName, String newName,
                                MethodDeclaration declaration, Source src,
                                ASTRewrite rewrite, Refactoring refactoring) {

        final MethodDeclaration methodWithRenamedInvokes = AstUtil.copySubtree(
                MethodDeclaration.class,
                declaration.getAST(),
                declaration
        );

        final Location methodWithRenamedInvokesLocation = Locations.locate(src, methodWithRenamedInvokes);

        renameAstNode(src, methodWithRenamedInvokesLocation, methodWithRenamedInvokes, oldName, newName, refactoring);

        rewrite.replace(declaration, methodWithRenamedInvokes, null);
    }


    static void renameAstNode(Source src, Location location,
                              ASTNode affected, String oldName, String newName, Refactoring refactoring){

        final RenameAstNodeVisitor renameAllMatches = new RenameAstNodeVisitor(
                src, location, oldName, newName
        );

        renameAllMatches.setStrategy(refactoring);

        affected.accept(renameAllMatches);
    }

}
