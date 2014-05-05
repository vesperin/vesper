package edu.ucsc.refactor.internal.visitors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.List;

import static edu.ucsc.refactor.internal.util.AstUtil.isFurtherTraversalNecessary;
import static edu.ucsc.refactor.internal.util.AstUtil.isNodeWithinSelection;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SimpleNameVisitor extends ASTVisitor {
    private final List<SimpleName>  names;
    private final Location          selection;

    public SimpleNameVisitor(Location selection){
        this.selection = Preconditions.checkNotNull(selection);
        this.names     = Lists.newArrayList();

    }

    @Override public boolean visit(SimpleName node) {
        final Source src = Source.from(node);
        if (!isFurtherTraversalNecessary(src, node, this.selection)) {
            return false;
        }

        if (isNodeWithinSelection(src, node, this.selection)) {
           names.add(node);
        }

        return true;
    }


    public boolean isFound(){
        return !names.isEmpty();
    }

    public List<SimpleName> getNames(){
        return names;
    }
}
