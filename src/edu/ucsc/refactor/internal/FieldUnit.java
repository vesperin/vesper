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

        return getNamedLocations(context);
    }

    @Override protected void addDeclaration(List<NamedLocation> namedLocations, Location each, ASTNode eachNode) {
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
