package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.internal.visitors.SelectedStatementNodesVisitor;
import edu.ucsc.refactor.spi.ProgramUnit;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class AbstractProgramUnit implements ProgramUnit {
    private final String    name;

    /**
     * Construct a new {@code AbstractProgramUnit}.
     *
     * @param name The name of unit
     */
    protected AbstractProgramUnit(String name){
        this.name   = Preconditions.checkNotNull(name);
    }

    @Override public String getName() {
        return name;
    }

    protected List<NamedLocation> getNamedLocations(Context context){
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
                addDeclaration(namedLocations, each, eachNode);
            }
        }

        return namedLocations;
    }

    protected abstract void addDeclaration(List<NamedLocation> namedLocations, Location each, ASTNode eachNode);


    @Override public String toString() {
        return Objects.toStringHelper(getName())
                .toString();
    }
}
