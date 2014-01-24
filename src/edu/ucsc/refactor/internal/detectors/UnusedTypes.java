package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.internal.visitors.TypeDeclarationVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UnusedTypes extends IssueDetector {

    private static final String STRATEGY_NAME        = Smell.UNUSED_TYPE.getKey();
    private static final String STRATEGY_DESCRIPTION = Smell.UNUSED_TYPE.getSummary();

    /**
     * Instantiate {@code UnusedMethods} issue detector.
     */
    public UnusedTypes() {
        super(STRATEGY_NAME, STRATEGY_DESCRIPTION);
    }

    @Override public void scanJava(Context context) {
        TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
        context.accept(typeDeclarationVisitor);

        for(AbstractTypeDeclaration eachTypeDeclaration : typeDeclarationVisitor.getDeclaredTypes()){

            if(!isDeclarationUsed(eachTypeDeclaration)){
                createIssue(eachTypeDeclaration);
            }
        }

    }


    private static boolean isDeclarationUsed(AbstractTypeDeclaration declaration) {
        final CompilationUnit  root     = AstUtil.parent(CompilationUnit.class, declaration);
        final List<SimpleName> usages   = AstUtil.findByNode(root, declaration.getName());

        if(usages.size() <= 1) { // it will contain at least its actual declaration, if it does then return false
            return false;
        }



        for(SimpleName each : usages){
            if(!isOfExpectedType(each)){
                return false;
            }
        }


        return true; // yes, the given declaration is used

    }


    private static boolean isOfExpectedType(SimpleName name){
        return AstUtil.parent(TypeDeclaration.class, name) != null
                || AstUtil.parent(VariableDeclarationStatement.class, name) != null
                || AstUtil.parent(SingleVariableDeclaration.class, name) != null
                || AstUtil.parent(ClassInstanceCreation.class, name) != null;
    }
}
