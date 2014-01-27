package edu.ucsc.refactor.internal.visitors;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceLocation;
import edu.ucsc.refactor.internal.SourceVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class SelectedASTNodesVisitor extends SourceVisitor {
    private final Location      selectedArea;
    private final boolean       traverseSelectedNode;
    private final List<ASTNode> selectedNodes;

    private ASTNode lastCoveringNode;


    /**
     * Instantiates a new {@link SelectedASTNodesVisitor} object.
     *
     * @param selectedArea The selected area.
     * @param traverseSelectedNode {@code true} if selected node
     *             should be traversed, {@code false} otherwise.
     */
    public SelectedASTNodesVisitor(Location selectedArea, boolean traverseSelectedNode){
        super(true);
        this.selectedArea           = selectedArea;
        this.traverseSelectedNode   = traverseSelectedNode;
        this.selectedNodes          = new ArrayList<ASTNode>();
    }


    public abstract boolean checkIfSelectionCoversValidStatements();


    /**
     * @return {@code true} if the selected area by user
     *      has mapped into covered AST nodes, {@code false} otherwise.
     */
    public boolean hasSelectedNodes() {
        return !selectedNodes.isEmpty();
    }

    /**
     * @return the list of AST nodes covered by
     *      selected area, empty list if none selected..
     */
    public List<ASTNode> getSelectedNodes() {
        return selectedNodes;
    }

    /**
     *
     * @return
     */
    public ASTNode getFirstSelectedNode() {
        if (!hasSelectedNodes()) return null;
        return selectedNodes.get(0);
    }

    /**
     *
     * @return
     */
    public ASTNode getLastSelectedNode() {
        if (!hasSelectedNodes()) return null;
        return selectedNodes.get(selectedNodes.size() - 1);
    }

    /**
     *
     * @return
     */
    public boolean isExpressionSelected() {
        return hasSelectedNodes()
                && getFirstSelectedNode() instanceof Expression;
    }

    /**
     *
     * @return
     */
    protected Location getSelection() {
        return selectedArea;
    }

    /**
     * @return The last covering node by user selected area.
     */
    public ASTNode getLastCoveringNode() {
        return lastCoveringNode;
    }

    /**
     * @return The range, in location form, of all selected nodes.
     */
    public Location getSelectedNodeRange() {
        if (!hasSelectedNodes()) return null;


        final ASTNode   firstNode   = getFirstSelectedNode();
        final ASTNode   lastNode    = getLastSelectedNode();
        final int       start       = firstNode.getStartPosition();
        final Source    code        = selectedArea.getSource();

        return SourceLocation.createLocation(
                code,
                code.getContents(),
                start,
                lastNode.getStartPosition() + lastNode.getLength() - start
        );
    }

    public abstract boolean isSelectionCoveringValidStatements();


    @Override protected boolean visitNode(ASTNode node) {
        final Location nodeLocation = Locations.locate(node);

        if(Locations.liesOutside(selectedArea, nodeLocation)) return false;

        if(Locations.covers(selectedArea, nodeLocation)) {
            if (isFirstNode()) {
                handleFirstSelectedNode(node);
            } else {
                handleNextSelectedNode(node);
            }

            return traverseSelectedNode;
        }

        if(Locations.coveredBy(selectedArea, nodeLocation)){
            lastCoveringNode = node;
            return true;
        } else if(Locations.endsIn(selectedArea, nodeLocation)){
            return handleSelectionEndsIn(node);
        }

        return true;
    }

    /**
     * @return {@code true} if we have selected any node, and the one
     *      we are exploring is the first one to check.
     */
    private boolean isFirstNode() {
        return !hasSelectedNodes();
    }


    protected void handleFirstSelectedNode(ASTNode node) {
        selectedNodes.clear();
        selectedNodes.add(node);
    }


    protected void handleNextSelectedNode(ASTNode node) {
        if (getFirstSelectedNode().getParent() == node.getParent()) {
            selectedNodes.add(node);
        }
    }


    protected boolean handleSelectionEndsIn(ASTNode node) {
        Preconditions.checkNotNull(node);
        return false;
    }
}
