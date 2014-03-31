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
public class RemoveUnusedFields extends SourceChanger {

    /**
     * Instantiates a new {@link RemoveUnusedFields} object.
     */
    public RemoveUnusedFields(){
        super();
    }

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.UNUSED_FIELD)
                || Names.from(Smell.UNUSED_FIELD).isSame(cause.getName());
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {
        final SourceChange change   = new SourceChange(cause, this, parameters);
        try {

            final CompilationUnit root = getCompilationUnit(cause);
            final ASTRewrite rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(removeUnusedFields(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    private Delta removeUnusedFields(CompilationUnit root, ASTRewrite rewrite, CauseOfChange cause){
        final boolean cameFromDetector = cause.getName().isSame(Smell.UNUSED_TYPE);

        for(ASTNode affected : cause.getAffectedNodes()){
            final FieldDeclaration declaration = AstUtil.parent(FieldDeclaration.class, affected);

            if(cameFromDetector){
                rewrite.remove(declaration, null);
            }  else {

                List fragments = declaration.fragments();
                for(Object eachObject : fragments){
                    final VariableDeclarationFragment fragment   = (VariableDeclarationFragment) eachObject;
                    final SimpleName                  name       = fragment.getName();
                    final List<SimpleName>            references = AstUtil.findByNode(root, name);

                    if(AstUtil.isSideEffectFound(name) || references.size() > 1){
                        throw new RuntimeException(
                                name.getIdentifier() +
                                        " cannot be deleted. It is referenced" +
                                        " throughout the source code"
                        );
                    } else {
                        rewrite.remove(declaration, null);
                    }

                }
            }

        }

        return createDelta(root, rewrite);
    }
}
