package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.SelectedStatementNodesVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import java.util.List;

/**
 * This represents a field of a class.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class FieldUnit extends AbstractProgramUnit {
    /**
     * Construct a new {@code Field} program unit.
     *
     * @param name The field's name
     */
    public FieldUnit(String name){
        super(name);
    }

    @Override public List<NamedLocation> getLocations(Context context) {
        Preconditions.checkNotNull(context);

        final List<NamedLocation> namedLocations = Lists.newArrayList();
        final List<Location> instances = Locations.locateWord(context.getSource(), getName());
        for(Location each : instances){

            final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(
                    each,
                    true
            );

            context.accept(statements);
            statements.checkIfSelectionCoversValidStatements();

            if(!statements.isSelectionCoveringValidStatements()){ return namedLocations; }

            // Note: once formatted, it is hard to locate a method. This mean that statements getSelectedNodes
            // is empty, and the only non null node is the statements.lastCoveringNode, which can be A BLOCK
            // if method is the selection. Therefore, I should get the parent of this block to get the method
            // or class to remove.

            for(ASTNode eachNode : statements.getSelectedNodes()){
                // ignore instance creation, parameter passing,... just give me its declaration

                final FieldDeclaration field = AstUtil.parent(
                        FieldDeclaration.class,
                        eachNode
                );

                if(field != null){
                    if(!AstUtil.contains(namedLocations, field)){
                        namedLocations.add(new ProgramUnitLocation(field, each));
                    }
                }

            }
        }

        return namedLocations;
    }
}
