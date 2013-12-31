package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.RenameMethodVisitor;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.AstUtil;
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

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.RENAME_METHOD);
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final Change change = new SourceChange(cause, this, parameters);
        try {
            for(ASTNode each : cause.getAffectedNodes()){ // assumption, there is only one affected node
                final Delta delta = renameMethod(
                        each,
                        (String) parameters.get(Parameters.METHOD_NEW_NAME).getValue()
                );

                change.getDeltas().add(delta);
            }
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private Delta renameMethod(ASTNode node, String newName){

        final MethodDeclaration method = AstUtil.parent(MethodDeclaration.class, node);
        final CompilationUnit   parent = AstUtil.parent(CompilationUnit.class, method);


        final AST               ast     = parent.getAST();
        final ASTRewrite        rewrite = ASTRewrite.create(ast);
        final String            oldName = method.getName().getIdentifier();
        final Source            src     = Source.from(method);

        final MethodDeclarationVisitor  methodsVisitor  = new MethodDeclarationVisitor();
        parent.accept(methodsVisitor);

        final List<MethodDeclaration>  methods  = methodsVisitor.getMethodDeclarations();

        checkNameNotTaken(methods, newName);

        // I. Rename the actual method's name.
        // begin:

        internalRename(oldName, newName, method, src, rewrite);

        // :end

        // II. look for method invocations and references (in JavaDocs) of original method
        // and then rename them using the `new name`.
        // begin:


        for(MethodDeclaration declaration : methods){
            if(declaration.getName().getIdentifier().equals(oldName)){
                continue;
            }

            internalRename(oldName, newName, declaration, src, rewrite);
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

    private void internalRename(String oldName, String newName, MethodDeclaration declaration, Source src, ASTRewrite rewrite) {
        final MethodDeclaration methodWithRenamedInvokes = AstUtil.copySubtree(
                MethodDeclaration.class,
                declaration.getAST(),
                declaration
        );

        final Location methodWithRenamedInvokesLocation = Locations.locate(src, methodWithRenamedInvokes);

        renameAstNode(src, methodWithRenamedInvokesLocation, methodWithRenamedInvokes, oldName, newName );

        rewrite.replace(declaration, methodWithRenamedInvokes, null);
    }


    static void renameAstNode(Source src, Location location,
                              ASTNode affected, String oldName, String newName){
        final RenameMethodVisitor renamingVisitor = new RenameMethodVisitor(
                src, location, oldName, newName
        );

        affected.accept(renamingVisitor);
    }

}
