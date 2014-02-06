package edu.ucsc.refactor.internal.util;

import com.google.common.collect.Lists;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Iterator;
import java.util.List;

/**
 * Stores detected clones.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class DetectedClones implements Iterable <List<DetectedClone>> {

    private final List<List<DetectedClone>> clones;

    /**
     * Stores detected clones.
     */
    public DetectedClones(){
        this.clones = Lists.newArrayList();
    }

    /**
     * Add a clone pair. If the clone pair already exists as subtree the
     * clone of the subtree is removed.
     *
     * @param left First subtree
     * @param right Second subtree
     */
    public void addClonePair(DetectedClone left, DetectedClone right) {
        removeSubtreeClones(left, right);

        // Add to a existing pair if left or right is already in a pair.
        for(List<DetectedClone> pair : clones){      // dealing singleton maps
            for(DetectedClone cloneItem : pair){
                if (cloneItem.getParseTree().equals(left.getParseTree())) {
                    pair.add(right);
                    return;
                } else if (cloneItem.getParseTree().equals(
                        right.getParseTree())) {
                    pair.add(left);
                    return;
                }
            }
        }

        // The clone does not exists, add a new pair.
        List<DetectedClone> clone = Lists.newArrayList();
        clone.add(left);
        clone.add(right);

        clones.add(clone);
    }


    /**
     * Get clone pairs by index.
     *
     * @param index Index
     * @return Clone pairs at the index.
     */
    public List<DetectedClone> getItem(int index) {
        return clones.get(index);
    }


    /**
     * Check if subtree is a subtree of tree.
     *
     * @param tree Tree
     * @param subtree Tree
     * @return true if subtree is a subtree of tree.
     */
    private boolean isSubtree(ASTNode tree, ASTNode subtree) {
        if (tree.equals(subtree)) {
            return true;
        }

        final List<ASTNode> children = AstUtil.getChildren(tree);
        for(ASTNode each : children){
            if(isSubtree(each, subtree)){
                return true;
            }
        }

        return false;
    }


    @Override public Iterator<List<DetectedClone>> iterator() {
        return clones.iterator();
    }


    /**
     * Remove subtree clones of the subtree left and right.
     *
     * @param left Left subtree
     * @param right Right subtree
     */
    private void removeSubtreeClones(DetectedClone left, DetectedClone right) {
        for (int i = clones.size() - 1; i >= 0; i--) {
            removeSubtreeClones(clones.get(i), left, right);
        }
    }

    /**
     * Remove subtree clones of the subtree.
     *
     * @param items Clone pair items to test.
     * @param left Left subtree
     * @param right Right subtree
     */
    private void removeSubtreeClones(List<DetectedClone> items,
                                     DetectedClone left, DetectedClone right) {
        DetectedClone matchLeft = null;
        DetectedClone matchRight = null;

        for (int i = items.size() - 1; i >= 0; i--) {
            if (isSubtree(left.getParseTree(), items.get(i).getParseTree())) {
                matchLeft = items.get(i);
            } else if (isSubtree(right.getParseTree(),
                    items.get(i).getParseTree())) {
                matchRight = items.get(i);
            }
        }

        if (matchLeft != null && matchRight != null) {
            if (items.size() == 2) {
                clones.remove(items);
            } else if (items.size() > 3) {
                /*
                 * There are more clones (for example 4). The remove of left
                 * and right keep the other clones.
                 */
                items.remove(matchLeft);
                items.remove(matchRight);
            } else {
                /*
                 * There are more clones (for example 3). Remove the left so
                 * that the other clone stile exists.
                 */
                items.remove(matchLeft);
            }
        }
    }

    /**
     * Get the number of clones.
     *
     * @return number of clones.
     */
    public int size() {
        return clones.size();
    }
}
