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
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedLocalVariable extends SourceChanger {
    /**
     * Instantiates a new {@link RemoveUnusedFields} object.
     */
    public RemoveUnusedLocalVariable(){
        super();
    }

    @Override public boolean canHandle(Cause cause) {
        return cause.isSame(Smell.UNUSED_VARIABLE)
                || Names.from(Smell.UNUSED_VARIABLE).isSame(cause.getName());
    }

    @Override protected Change initChanger(Cause cause, Map<String, Parameter> parameters) {
        final SourceChange change   = new SourceChange(cause, this, parameters);
        try {

            final CompilationUnit root = getCompilationUnit(cause);
            final ASTRewrite rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(removeUnusedLocalVariable(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }


    private Delta removeUnusedLocalVariable(CompilationUnit root, ASTRewrite rewrite, Cause cause){
        final boolean cameFromDetector = cause.getName().isSame(Smell.UNUSED_VARIABLE);

        for(ASTNode affected : cause.getAffectedNodes()){
            final VariableDeclarationStatement declaration = AstUtil.parent(VariableDeclarationStatement.class, affected);

            if(declaration != null){
                if(cameFromDetector){  // not used anywhere in the code
                    rewrite.remove(declaration, null);
                } else {
                    final List fragments = declaration.fragments();
                    for(Object eachObject : fragments){
                        final VariableDeclarationFragment fragment   = (VariableDeclarationFragment) eachObject;
                        final SimpleName                  name       = fragment.getName();
                        final List<SimpleName>            references = AstUtil.findByNode(root, name);

                        if(AstUtil.isSideEffectFound(name) || references.size() > 1){
                            throw new RuntimeException(
                                    name.getIdentifier() +
                                            " cannot be deleted. It is referenced" +
                                            " within the current scope!"
                            );
                        } else {
                            rewrite.remove(declaration, null);
                        }

                    }
                }
            } else {
                final VariableDeclarationFragment   fragment    = AstUtil.parent(VariableDeclarationFragment.class, affected);
                final SimpleName                    name        = fragment.getName();
                final List<SimpleName>              references  = AstUtil.findByNode(root, name);

                if(AstUtil.isSideEffectFound(name) || references.size() > 1){
                    throw new RuntimeException(
                            name.getIdentifier() +
                                    " cannot be deleted. It is referenced" +
                                    " within the current scope!"
                    );
                } else {
                    rewrite.remove(fragment, null);
                }
            }

        }


        return createDelta(root, rewrite);
    }
}
