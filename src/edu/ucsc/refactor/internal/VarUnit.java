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
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

/**
 * This represents a local variable of a class.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class VarUnit extends AbstractProgramUnit {
    /**
     * Construct a new {@code Local Variable} program unit.
     *
     * @param name The field's name
     */
    public VarUnit(String name){
        super(name);
    }

    @Override public List<NamedLocation> getLocations(Context context) {
        Preconditions.checkNotNull(context);

        return getNamedLocations(context);
    }

    @Override protected void addDeclaration(List<NamedLocation> namedLocations, Location each, ASTNode eachNode) {
        final VariableDeclarationStatement localVar = AstUtil.parent(
                VariableDeclarationStatement.class,
                eachNode
        );

        if(localVar != null){
            if(!AstUtil.contains(namedLocations, localVar)){
                namedLocations.add(new ProgramUnitLocation(localVar, each));
            }
        }
    }
}
