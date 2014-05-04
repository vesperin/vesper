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
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.List;

/**
 * This represents a parameter of a class's method.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ParameterUnit extends AbstractProgramUnit {
    /**
     * Construct a new {@code Parameter} program unit.
     *
     * @param name The parameter's name
     */
    public ParameterUnit(String name){
        super(name);
    }

    @Override public List<NamedLocation> getLocations(Context context) {
        Preconditions.checkNotNull(context);

        return getNamedLocations(context);
    }

    @Override protected void addDeclaration(List<NamedLocation> namedLocations, Location each, ASTNode eachNode) {
        final SingleVariableDeclaration parameter = AstUtil.parent(
                SingleVariableDeclaration.class,
                eachNode
        );

        if(parameter != null){
            if(!AstUtil.contains(namedLocations, parameter)){
                namedLocations.add(new ProgramUnitLocation(parameter, each));
            }
        }
    }
}
