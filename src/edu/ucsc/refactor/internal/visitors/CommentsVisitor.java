package edu.ucsc.refactor.internal.visitors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.LineComment;

import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommentsVisitor extends ASTVisitor {
    private final Set<BlockComment> blockComments;
    private final Set<LineComment>  lineComments;

    public CommentsVisitor(){
        blockComments = Sets.newHashSet();
        lineComments  = Sets.newHashSet();
    }


    public List<BlockComment> getBlockComments(){
        return Lists.newArrayList(blockComments);
    }

    public List<LineComment> getLineComments(){
        return Lists.newArrayList(lineComments);
    }


    @Override public boolean visit(LineComment node) {
        lineComments.add(node);
        return true;
    }

    @Override public boolean visit(BlockComment node) {
        blockComments.add(node);
        return true;
    }
}
