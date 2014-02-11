package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SimpleNameVisitor extends ASTVisitor {
    private List<SimpleName> names = new ArrayList<SimpleName>();

    @Override public boolean visit(SimpleName node) {
        names.add(node);
        return super.visit(node);
    }

    public List<SimpleName> getSimpleNames() {
        return names;
    }

}
