package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

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

    public static <T extends ASTNode> T copySubtree(final Class<T> thatClass, AST ast, final ASTNode node){
        // similar to //(MethodDeclaration)ASTNode.copySubtree(ast, method);
        return thatClass.cast(ASTNode.copySubtree(ast, node));
    }

    public static ASTRewrite createAstRewrite(AST ast){
        if(ast == null) throw new NullPointerException("createAstRewrite() was given a null AST");
        //  please remember to avoid creating multiple rewrites, one per affected node...
        //  that will make changes to be out of sync and cause source code overrides; e.g.,
        //  delete method A in Src, rename parameter in Src with method A not deleted.
        return ASTRewrite.create(ast);
    }


    public static void copyParameters(List src, MethodDeclaration dst){
        for(Object eachObj : src){
            final SingleVariableDeclaration next  = (SingleVariableDeclaration)eachObj;
            final SingleVariableDeclaration param = AstUtil.copySubtree(SingleVariableDeclaration.class, dst.getAST(), next);
            dst.parameters().add(param);
        }
    }


    public static void syncSourceProperty(Source updatedSource, ASTNode node){
        if(node instanceof CompilationUnit){
            node.setProperty(Source.SOURCE_FILE_PROPERTY, updatedSource);
        }  else {
            // do this after each delta's application
            AstUtil.parent(CompilationUnit.class, node).setProperty(
                    Source.SOURCE_FILE_PROPERTY,
                    updatedSource
            );
        }
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


    public static boolean isNodeWithinSelection(Source src, ASTNode node, Location selection) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = selection;


        return (Locations.inside(methodLocation, nodeLocation))
                || (Locations.covers(methodLocation, nodeLocation));
    }

    public static boolean isNodeEnclosingMethod(Source src, ASTNode node, Location selection) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = selection;

        // Is the method completely enclosed by the node?
        return (Locations.inside(nodeLocation, methodLocation));
    }


    public static boolean isNodeExactlyAtLocation(Source src, ASTNode node, Location selection) {

        final Location nodeLocation     = Locations.locate(src, node);
        final Location methodLocation   = selection;

        // Is the method at the same position as the other node?
        return (Locations.bothSame(nodeLocation, methodLocation));
    }


    public static boolean isFurtherTraversalNecessary(Source src, ASTNode node, Location selection) {
        return isNodeWithinSelection(src, node, selection)
                || isNodeEnclosingMethod(src, node, selection)
                || isNodeExactlyAtLocation(src, node, selection);
    }
}
