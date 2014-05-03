package edu.ucsc.refactor.internal;

import com.google.common.collect.Lists;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.internal.visitors.SelectedStatementNodesVisitor;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SelectedUnit extends AbstractProgramUnit  {
    private static final String WILD_CARD = "*";

    private final SourceSelection   selection;

    /**
     * Construct a new {@code InferredUnit} program unit with a Java {@code Context}
     * and a {@code SourceSelection}.
     */
    public SelectedUnit(SourceSelection selection) {
        super(WILD_CARD);
        this.selection  = selection;
    }

    @Override public List<NamedLocation> getLocations(Context context) {
        ensureIsWildCard();

        final List<NamedLocation> locations = Lists.newArrayList();

        final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(
                selection.toLocation(),
                true
        );

        context.accept(statements);
        statements.checkIfSelectionCoversValidStatements();

        if(!statements.isSelectionCoveringValidStatements()){ return locations; }

        // Note, once formatted, it is hard to locate a method. This mean that statements getSelectedNodes
        // is empty, and the only non null node is the statements.lastCoveringNode, which can be A BLOCK
        // if method is the selection. Therefore, I should get the parent of this block to get the method
        // or class to remove.

        for(ASTNode each : statements.getSelectedNodes()){
            // ignore instance creation, parameter passing,... just give me its declaration
            locations.add(new ProgramUnitLocation(each, Locations.locate(each)));
        }

        return locations;
    }

    private void ensureIsWildCard(){
        if(!StringUtil.equals(getName(), WILD_CARD)){
            throw new IllegalStateException("Not a wildcard unit");
        }
    }
}
