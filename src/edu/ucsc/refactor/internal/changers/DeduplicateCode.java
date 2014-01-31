package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class DeduplicateCode extends SourceChanger {

    /**
     * Instantiates a new {@link DeduplicateCode} object.
     */
    public DeduplicateCode(){
        super();
    }

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.DUPLICATED_CODE)
                || Names.from(Smell.DUPLICATED_CODE).isSame(cause.getName());
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {
        final SourceChange change   = new SourceChange(cause, this, parameters);
        try {

            final CompilationUnit root      = getCompilationUnit(cause);
            final ASTRewrite      rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(deduplicate(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }


    private Delta deduplicate(CompilationUnit root, ASTRewrite rewrite, CauseOfChange cause){

        final List<ASTNode> nodes         = cause.getAffectedNodes();
        final int           size          = nodes.size();
        final int           cloneNumber   = size == 0 ? size : size - 1; // minus the original declaration

        if (cloneNumber == 0) {
            throw new RuntimeException("calling deduplicate() when there are no detected clones");
        } else if (cloneNumber == 1) {
            // One method. So let the other code call that method.
            final MethodDeclaration original    = AstUtil.exactCast(MethodDeclaration.class, nodes.get(0));
            final MethodDeclaration duplicate   = AstUtil.exactCast(MethodDeclaration.class, nodes.get(1));

            // sanity check
            final boolean sameReturn            = sameReturnType(original, duplicate);
            if(!sameReturn) {
                throw new RuntimeException("automatic deduplication cannot be done on methods "
                        + "with different return types; please consider "
                        + "manual deduplication."
                );
            }

            final MethodDeclaration clone       = AstUtil.copySubtree(MethodDeclaration.class, root.getAST(), duplicate);
            final AST ast = clone.getAST();

            final Block block = ast.newBlock();

            final MethodInvocation invocation       = ast.newMethodInvocation();
            invocation.setName(ast.newSimpleName(original.getName().getIdentifier()));

            AstUtil.copyArguments(duplicate.parameters(), invocation);

            if(isVoidReturn(original)){
                ExpressionStatement expressionStatement = ast.newExpressionStatement(invocation);
                block.statements().add(expressionStatement);
            } else {
                final ReturnStatement  returnStatement  = ast.newReturnStatement();
                returnStatement.setExpression(invocation);
                block.statements().add(returnStatement);
            }

            clone.setBody(block);

            rewrite.replace(duplicate, clone, null);
        } else {
            // TODO
            // More methods. Remove one of the methods.
        }

        return createDelta(root, rewrite);
    }


    private static boolean sameReturnType(MethodDeclaration a, MethodDeclaration b){
       return a.getReturnType2().resolveBinding() == b.getReturnType2().resolveBinding();
    }


    private static boolean isVoidReturn(MethodDeclaration method){
        if(method.getReturnType2().isPrimitiveType() ){
            final PrimitiveType primitiveType = AstUtil.exactCast(
                    PrimitiveType.class,
                    method.getReturnType2()
            );

            return primitiveType.getPrimitiveTypeCode() == PrimitiveType.VOID;
        }

        return false;
    }


}
