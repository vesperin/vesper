package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.ucsc.refactor.internal.util.AstUtil.isFurtherTraversalNecessary;
import static edu.ucsc.refactor.internal.util.AstUtil.isNodeWithinSelection;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class LocalVariableVisitor extends ASTVisitor {
    final List<VariableDeclarationFragment> fragments;
    final Source                            source;
    final Location                          selection;
    final Map<String, Record>               vars;


    public LocalVariableVisitor(Source source, Location selection){
        this.source    = source;
        this.selection = selection;
        this.fragments = new ArrayList<VariableDeclarationFragment>();
        this.vars      = new HashMap<String, Record>();
    }


    /**
     * @see ASTVisitor#visit(CompilationUnit)
     */
    @Override public boolean visit(CompilationUnit node) {
        // visit only the type declarations
        final List types = node.types();

        for (Object type : types) {
            ((TypeDeclaration) type).accept(this);
        }

        return false;
    }

    /**
     * @see ASTVisitor#visit(TypeDeclaration)
     */
    @Override public boolean visit(TypeDeclaration node) {
        // visit the method declarations
        MethodDeclaration[] methods = node.getMethods();
        for (MethodDeclaration m : methods) {
            m.accept(this);
        }

        // visit inner types
        TypeDeclaration[] types = node.getTypes();
        for (TypeDeclaration type : types) {
            type.accept(this);
        }

        return false;
    }


    @Override public boolean visit(MethodDeclaration node) {
        if (!isFurtherTraversalNecessary(source, node, this.selection)) {
            return false;
        }

        if (isNodeWithinSelection(source, node, this.selection)) {
            // visit the method declarations
            Block b = node.getBody();
            List types = b.statements();

            for (Object o : types) {
                if (o instanceof VariableDeclarationStatement) {
                    ((VariableDeclarationStatement) o).accept(this);
                }
            }
        }

        return false;
    }


    @Override public boolean visit(VariableDeclarationStatement node) {

        if (isNodeWithinSelection(source, node, this.selection)) {
            String typeName = node.getType().toString();

            for (Object o : node.fragments()) {
                final VariableDeclarationFragment f = (VariableDeclarationFragment) o;
                fragments.add(f);
                String identifier = f.getName().getIdentifier();
                final Record record = new Record();
                record.typeName = typeName;
                record.fragment = f;
                vars.put(identifier, record);
            }
        }

        return false;

    }

    public boolean attributeStartsWith(String keyPrefix){
        for(String eachKey : vars.keySet()){
            if (eachKey.startsWith(keyPrefix)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasAttribute(String name){
        return vars.containsKey(name);
    }

    static class Record {
        String                      typeName;
        VariableDeclarationFragment fragment;
    }

}
