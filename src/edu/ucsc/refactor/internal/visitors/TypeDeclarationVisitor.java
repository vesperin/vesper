package edu.ucsc.refactor.internal.visitors;

import com.google.common.collect.Sets;
import edu.ucsc.refactor.internal.SourceVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class TypeDeclarationVisitor extends SourceVisitor {
    final Set<AbstractTypeDeclaration> clazz = Sets.newHashSet();

    /**
     * Constructs a new TypeDeclarationVisitor
     */
    public TypeDeclarationVisitor() {
        super();
    }


    @Override public boolean visit(TypeDeclaration node) {
        if(!(node.getParent() instanceof CompilationUnit)){
            clazz.add(node);
        }

        return super.visit(node);
    }


    @Override public boolean visit(EnumDeclaration node) {
        clazz.add(node);
        return super.visit(node);
    }


    @Override public boolean visit(AnnotationTypeDeclaration node) {
        clazz.add(node);
        return super.visit(node);
    }
}
