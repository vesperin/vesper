package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.internal.SourceLocation;
import edu.ucsc.refactor.internal.SourceVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SelectedASTNodeVisitor extends SourceVisitor {
    private final Location selectedArea;

    private ASTNode coveringNode;
    private ASTNode coveredNode;


    /**
     * Instantiates a new {@link SelectedASTNodeVisitor} object.
     * @param selectedArea The selected area corresponding to an AST node.
     */
    public SelectedASTNodeVisitor(Location selectedArea){
        this.selectedArea = selectedArea;
    }


    @Override protected boolean visitNode(ASTNode node) {

        final Location nodeLocation = locate(node);

        if(Locations.liesOutside(selectedArea, nodeLocation)
                && !(Locations.bothSame(selectedArea, nodeLocation))){
            return false;
        }


        if(Locations.covers(nodeLocation, selectedArea)){
            coveringNode = node;
        }


        if(Locations.covers(nodeLocation, selectedArea)){
            if(coveringNode == node){
                coveredNode = node;
                if(coveredNode.getParent() != null){
                    coveringNode = coveredNode.getParent();
                }
                return true; // look further for node with the same length as parent
            } else if (coveredNode == null) {
                coveredNode = node;
            }

            return false;
        }

        return true;
    }


    /**
     * Locates a ASTNode in the {@code Source}.
     *
     * @param node The ASTNode to be located.
     * @return A {@code Location} in the {@code Source} where a ASTNode is found.
     */
    public Location locate(ASTNode node) {
        return SourceLocation.createLocation(
                selectedArea.getSource(),
                selectedArea.getSource().getContents(),
                node.getStartPosition(),
                node.getStartPosition() + node.getLength()
        );
    }

    /**
     * @return The matched ASTNode.
     */
    public ASTNode getMatchedNode(){
        if(getCoveredNode() != null){
            if(Locations.covers(selectedArea, locate(getCoveredNode()))){
               return getCoveredNode();
            }
        }

        return getCoveringNode();
    }


    /**
     * @return the covered {@link ASTNode node}. If more than one nodes are covered by the
     *         selection, the returned node is first covered node found in a top-down traversal
     *         of the AST.
     */
    public ASTNode getCoveredNode() {
        return coveredNode;
    }

    /**
     * @return the covering {@link ASTNode node}. If more than one nodes are covering the
     *         selection, the returned node is last covering node found in a top-down traversal
     *         of the AST.
     */
    public ASTNode getCoveringNode() {
        return coveringNode;
    }



}
