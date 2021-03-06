package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.Cause;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedTypes extends SourceChanger {

    /**
     * Instantiates a new {@link RemoveUnusedTypes} object.
     */
    public RemoveUnusedTypes(){
        super();
    }

    @Override public boolean canHandle(Cause cause) {
        return cause.isSame(Smell.UNUSED_TYPE)
                || Names.from(Smell.UNUSED_TYPE).isSame(cause.getName());
    }

    @Override protected Change initChanger(Cause cause, Map<String, Parameter> parameters) {

        final SourceChange change   = new SourceChange(cause, this, parameters);
        try {

            final CompilationUnit root      = getCompilationUnit(cause);
            final ASTRewrite      rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(removeUnusedTypeDeclarations(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }


    private Delta removeUnusedTypeDeclarations(CompilationUnit root, ASTRewrite rewrite, Cause cause){

        // if it came from the detector, then there is not need for looking for references
        // why? because there are none! If it came from the SingleEdit, then we must take these
        // references into account and delete them as well.
        final boolean cameFromDetector = cause.getName().isSame(Smell.UNUSED_TYPE);

        for(ASTNode affected : cause.getAffectedNodes()){
            final AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) affected;

            if(cameFromDetector){
               rewrite.remove(declaration, null);
            }  else {
                final List<SimpleName>  usages  = AstUtil.findByNode(root, declaration.getName());

                if(usages.size() > 1){
                    throw new RuntimeException(
                            declaration.getName().getIdentifier() +
                                    " cannot be deleted. It is used somewhere" +
                                    " in the Source."
                    );
                } else {
                    rewrite.remove(declaration, null);
                }
            }

        }

        return createDelta(root, rewrite);

    }

}
