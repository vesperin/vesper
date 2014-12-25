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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Map;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveUnusedImports extends SourceChanger {

    @Override public boolean canHandle(Cause cause) {
        return cause.isSame(Smell.UNUSED_IMPORTS)
                || Names.from(Smell.UNUSED_IMPORTS).isSame(cause.getName());
    }

    @Override protected Change initChanger(Cause cause,
                                           Map<String, Parameter> parameters) {

        final SourceChange  change           = new SourceChange(cause, this, parameters);
        final boolean       cameFromDetector = cause.getName().isSame(Smell.UNUSED_IMPORTS);

        try {
            final CompilationUnit root = (cameFromDetector
                    ? getCompilationUnit(cause)
                    : getCompilationUnitFromTypeDeclaration(cause)
            );

            final ASTRewrite      rewrite   = ASTRewrite.create(root.getAST());
            change.getDeltas().add(removeUnusedImports(root, rewrite, cause));
        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }


    private static CompilationUnit getCompilationUnitFromTypeDeclaration(Cause cause){
        CompilationUnit unit = AstUtil.parent(CompilationUnit.class, cause.getAffectedNodes().get(0));
        if(unit == null){
            return AstUtil.exactCast(CompilationUnit.class, cause.getAffectedNodes().get(0));
        }

        return unit;
    }


    private Delta removeUnusedImports(CompilationUnit root, ASTRewrite rewrite, Cause cause){
        final boolean cameFromDetector = cause.getName().isSame(Smell.UNUSED_IMPORTS);

        if(cameFromDetector){
            for(ASTNode eachUnusedImportDeclaration : cause.getAffectedNodes()){
                ImportDeclaration importDeclaration = (ImportDeclaration) eachUnusedImportDeclaration;
                rewrite.remove(importDeclaration, null);
            }
        } else {
            final Set<ASTNode> unusedImports = AstUtil.getUnusedImports(root);

            for(ASTNode eachUsed : unusedImports){
                rewrite.remove(eachUsed, null);
            }
        }

        return createDelta(root, rewrite);
    }
}
