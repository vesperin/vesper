package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.MethodRefInJavaDocVisitor;
import edu.ucsc.refactor.internal.visitors.RenameAstNodeVisitor;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.AstUtil;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Parameters;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        for(ASTNode each : cause.getAffectedNodes()){ // assumption, there is only one affected node
            final Delta delta = renameMethod(
                    each,
                    (String) parameters.get(Parameters.METHOD_NEW_NAME).getValue()
            );

            change.getDeltas().add(delta);
        }

        return change;
    }

    private Delta renameMethod(ASTNode node, String newName){

        final MethodDeclaration method = AstUtil.parent(MethodDeclaration.class, node);
        final CompilationUnit   parent = AstUtil.parent(CompilationUnit.class, method);


        final AST               ast     = parent.getAST();
        final ASTRewrite        rewrite = ASTRewrite.create(ast);
        final String            oldName = method.getName().getIdentifier();


        // I. create a new method similar to original method
        // but with new name.
        // begin:

        // todo(Huascar) do we need to create a copy?
        final MethodDeclaration copy    = AstUtil.copySubtree(MethodDeclaration.class, ast, method);

        final MethodDeclaration newMethod = ast.newMethodDeclaration();
        final Block newBlock = AstUtil.copySubtree(Block.class, ast, copy.getBody());

        final Source    src             = Source.from(method);
        final Location  blockLocation   = Locations.locate(src, newBlock);

        final RenameAstNodeVisitor visitor = new RenameAstNodeVisitor(
                src,
                blockLocation,  // block
                oldName,        // old name
                newName         // new name
        );

        newBlock.accept(visitor);

        newMethod.setBody(newBlock);

        newMethod.setReturnType2((Type) ASTNode.copySubtree(ast, copy.getReturnType2()));
        newMethod.modifiers().addAll(ast.newModifiers(Modifier.PUBLIC));

        AstUtil.copyParameters(copy.parameters(), newMethod);

        newMethod.setName(ast.newSimpleName(newName));

        rewrite.replace(method, newMethod, null);

        // :end

        // II. look for method invocations of original method
        // and the rename them to new method having the new name.
        // begin:

        final MethodDeclarationVisitor  methodsVisitor  = new MethodDeclarationVisitor();
        parent.accept(methodsVisitor);

        final List<MethodDeclaration>  methods  = methodsVisitor.getMethodDeclarations();
        for(MethodDeclaration declaration : methods){
            if(declaration.getName().getIdentifier().equals(oldName)){
                continue;
            }

            final MethodDeclaration methodCopy = AstUtil.copySubtree(
                    MethodDeclaration.class,
                    declaration.getAST(),
                    declaration
            );

            final Location  methodLocation = Locations.locate(src, methodCopy);
            final RenameAstNodeVisitor renameInvocations = new RenameAstNodeVisitor(
                    src, methodLocation, oldName, newName
            );

            methodCopy.accept(renameInvocations);

            rewrite.replace(declaration, methodCopy, null);
        }

        // :end

        // III. look for method references in JavaDocs
        // and the rename them to new method having the new name.
        // begin:

        final MethodRefInJavaDocVisitor referencesVisitor = new MethodRefInJavaDocVisitor();
        parent.accept(referencesVisitor);

        final Set<MethodRef> references = referencesVisitor.getMethodReferences();
        for(MethodRef eachReference : references){
            if(!eachReference.getName().getIdentifier().equals(oldName)){
                continue;
            }

            final MethodRef referenceCopy     = AstUtil.copySubtree(MethodRef.class, ast, eachReference);
            final Location  referenceLocation = Locations.locate(src, referenceCopy);
            final RenameAstNodeVisitor renameReferences = new RenameAstNodeVisitor(
                    src, referenceLocation, oldName, newName
            );

            referenceCopy.accept(renameReferences);

            rewrite.replace(eachReference, referenceCopy, null);
        }


        // :end

        return createDelta(node, rewrite);
    }


}
