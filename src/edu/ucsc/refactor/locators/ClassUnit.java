package edu.ucsc.refactor.locators;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.internal.ProgramUnitLocation;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * This element represents classes in the base Source.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ClassUnit extends AbstractProgramUnit {
    /**
     * Construct a new {@code Class} program unit.
     *
     * @param name The class's name
     */
    public ClassUnit(String name) {
        super(name);
    }

    @Override public List<NamedLocation> getLocations(Context context) {

        Preconditions.checkNotNull(context);

        return getNamedLocations(context);
    }

    @Override protected void addDeclaration(List<NamedLocation> namedLocations, Location each, ASTNode eachNode) {
        final TypeDeclaration classDeclaration = AstUtil.parent(
                TypeDeclaration.class,
                eachNode
        );

        if(classDeclaration != null){
            if(!AstUtil.contains(namedLocations, classDeclaration) && getName().equals(classDeclaration.getName().getIdentifier())){
                namedLocations.add(new ProgramUnitLocation(classDeclaration, each));
            }
        }
    }
}
