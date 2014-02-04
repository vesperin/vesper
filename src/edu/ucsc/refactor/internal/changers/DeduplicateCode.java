package edu.ucsc.refactor.internal.changers;

import com.google.common.collect.Iterables;
import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.MethodInvocationVisitor;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        } else if (cloneNumber >= 1) {
            // The strategy this code follows to perform deduplication is the following:
            // per duplicated method declaration, find all of its invocations. Then
            // rename each found invocation using the name of the original method declaration.
            // After that, remove the duplicated method declaration.
            final MethodDeclaration original    = AstUtil.exactCast(MethodDeclaration.class, nodes.get(0));
            final Iterable<ASTNode> rest        = Iterables.skip(nodes, 1);
            for(ASTNode eachClone : rest) {
                final MethodDeclaration duplicate  = AstUtil.exactCast(MethodDeclaration.class, eachClone);
                final MethodDeclaration copied     = AstUtil.copySubtree(MethodDeclaration.class, root.getAST(), duplicate);
                final boolean           sameReturn = sameReturnType(original, copied);

                if(!sameReturn) { // all or none. We don't do partial deduplication
                    throw new RuntimeException("automatic deduplication cannot be done on methods "
                            + "with different return types; please consider "
                            + "manual deduplication."
                    );
                }


                final MethodInvocationVisitor invokesOfDuplication = new MethodInvocationVisitor(copied.getName());
                root.accept(invokesOfDuplication);
                final Set<MethodInvocation>   invokes              = invokesOfDuplication.getMethodInvocations();

                for(MethodInvocation each : invokes){
                    final MethodInvocation copiedInvoke = AstUtil.copySubtree(MethodInvocation.class, root.getAST(), each);
                    copiedInvoke.setName(root.getAST().newSimpleName(original.getName().getIdentifier()));
                    rewrite.replace(each, copiedInvoke, null);
                }


                rewrite.remove(duplicate, null);
            }
        }

        return createDelta(root, rewrite);
    }


    private static boolean sameReturnType(MethodDeclaration a, MethodDeclaration b){
       return a.getReturnType2().resolveBinding() == b.getReturnType2().resolveBinding();
    }
}
