package edu.ucsc.refactor;

import edu.ucsc.refactor.spi.Name;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class AbstractCause implements Cause {
    private final List<ASTNode> nodes;

    /**
     * Construct the {@link AbstractCause}.
     */
    protected AbstractCause(){
        this.nodes  = new ArrayList<ASTNode>();
    }


    /**
     * Adds a node where the issue was detected.
     *
     * @param node The suspicious node.
     */
    public void addNode(ASTNode node){
        if(node == null) {
          throw new IllegalArgumentException();
        }

        this.nodes.add(node);
    }

    /**
     * Clears the list of affected nodes that made this {@code CauseOfChange}.
     */
    public void clear(){
        for(ASTNode node : nodes){
            removeNode(node);
        }
    }

    @Override public abstract Name getName();

    @Override public abstract boolean isSame(Name otherName);

    /**
     * Removes a no longer needed node.
     *
     * @param node The node to be removed.
     * @return {@code true} if node was removed.
     */
    public boolean removeNode(ASTNode node){
        if(node == null){
           throw new IllegalArgumentException();
        }

        return this.nodes.remove(node);
    }

    @Override public abstract String more();

    @Override public List<ASTNode> getAffectedNodes() {
        return Collections.unmodifiableList(this.nodes);
    }

    @Override public String toString() {
        return more();
    }
}
