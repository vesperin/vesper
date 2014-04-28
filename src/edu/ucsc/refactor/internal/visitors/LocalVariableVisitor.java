package edu.ucsc.refactor.internal.visitors;

import com.google.common.collect.Lists;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class LocalVariableVisitor extends ASTVisitor {

    private final List<VariableDeclarationStatement> localVariables;
    private final String name;

    public LocalVariableVisitor(String name){
        this.name           = name;
        this.localVariables = Lists.newArrayList();
    }

    @Override public boolean visit(CompilationUnit node) {
        // visit only the type declarations
        final List types = node.types();

        for(Object eachType : types) {
            final TypeDeclaration typeDeclaration = AstUtil.exactCast(TypeDeclaration.class, ((ASTNode)eachType));
            typeDeclaration.accept(this);
        }

        return false;
    }

    @Override public boolean visit(TypeDeclaration node) {
        // visit the method declarations
        final MethodDeclaration[] methods = node.getMethods();

        for(MethodDeclaration eachMethod : methods){
            eachMethod.accept(this);
        }


        // visit inner types
        TypeDeclaration[] types = node.getTypes();
        for(TypeDeclaration eachType : types){
            eachType.accept(this);
        }

        return false;
    }

    @Override public boolean visit(MethodDeclaration node) {
        // visit the method declarations
        final Block block = node.getBody();
        final List types  = block.statements();

        for(Object eachType : types){
            final ASTNode eachNode = (ASTNode) eachType;
            if(AstUtil.isOfType(VariableDeclarationStatement.class, eachNode)){
                final VariableDeclarationStatement statement = AstUtil.exactCast(VariableDeclarationStatement.class, eachNode);
                statement.accept(this);
            }
        }

        return false;
    }


    @Override public boolean visit(VariableDeclarationStatement node) {
        final String varName = AstUtil.getSimpleName(node);
        if(varName != null){
            if(name != null && varName.equals(name)){
                localVariables.add(node);
            }
        }

        return false;
    }


    public List<VariableDeclarationStatement> getLocalVariables(){
        return localVariables;
    }
}
