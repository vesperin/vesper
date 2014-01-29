package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.spi.Names;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

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


        final int size          = cause.getAffectedNodes().size();
        final int cloneNumber   = size == 0 ? size : size - 1; // minus the original declaration

        if (cloneNumber == 0) {
            throw new RuntimeException("calling deduplicate() when there are no detected clones");
        } else if (cloneNumber == 1) {
            // TODO
            // One method. So let the other code call that method.
            throw new UnsupportedOperationException();
        } else {
            // TODO
            // More methods. Remove one of the methods.
        }

        return createDelta(root, rewrite);
    }



}
