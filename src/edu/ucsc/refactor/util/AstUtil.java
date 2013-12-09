package edu.ucsc.refactor.util;

import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class AstUtil {
    private AstUtil(){}

    public static boolean hasAnnotation(MethodDeclaration methodDeclaration) {
        final List modifiers = methodDeclaration.modifiers();
        return !modifiers.isEmpty() && modifiers.get(0) instanceof Annotation;
    }


    public static boolean usesVariable(MethodDeclaration methodDeclaration,
                                       SingleVariableDeclaration variableDeclaration) {
        String methodBlock = methodDeclaration.getBody().toString();

        return methodBlock.contains(variableDeclaration.getName().toString());
    }

    /**
     * Find the nearest parent node of a certain type for an {@link org.eclipse.jdt.core.dom.ASTNode}.
     *
     * @param thatClass The type class of the parent node to find. Must be derived from ASTNode.
     * @param node  The node to find a parent node for.
     * @param <T>   The ASTNode derived type of the parent node.
     * @return The found parent, or null if no such parent exists.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ASTNode> T parent(final Class<T> thatClass, final ASTNode node) {
        ASTNode parent = node;
        do {
            parent = parent.getParent();
            if (parent == null) {
                return null;
            }
        } while (parent.getClass() != thatClass);
        return (T) parent;
    }

    public static int getAnnotationsSize(IBinding binding) {
        if (binding.getAnnotations() != null) {
            return binding.getAnnotations().length;
        }
        return 0;
    }


    public static String getSimpleNameIdentifier(Name name) {
        if (name.isQualifiedName()) {
            return ((QualifiedName) name).getName().getIdentifier();
        } else {
            return ((SimpleName) name).getIdentifier();
        }
    }

    public static boolean isMainMethod(MethodDeclaration methodDeclaration) {
        return (methodDeclaration.getName().toString().equals("main"));
    }

    public static boolean processJavadocComments(CompilationUnit astRoot) {
        return !(astRoot != null && astRoot.getTypeRoot() != null)
                || !"package-info.java".equals(astRoot.getTypeRoot().getElementName());
    }
}
