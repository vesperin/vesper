package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

/**
 * This represents a method of a class.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MethodUnit extends AbstractProgramUnit {
    /**
     * Construct a new {@code Method} program unit.
     *
     * @param name The method's name
     */
    public MethodUnit(String name){
        super(name);
    }

    @Override public List<NamedLocation> getLocations(Context context) {
        Preconditions.checkNotNull(context);

        return getNamedLocations(context);
    }

    @Override protected void addDeclaration(List<NamedLocation> namedLocations, Location each, ASTNode eachNode) {
        final MethodDeclaration methodDeclaration = AstUtil.parent(
                MethodDeclaration.class,
                eachNode
        );

        if(methodDeclaration != null){
            if(!AstUtil.contains(namedLocations, methodDeclaration) && getName().equals(methodDeclaration.getName().getIdentifier())){
                namedLocations.add(new ProgramUnitLocation(methodDeclaration, each));
            }
        }
    }
}
