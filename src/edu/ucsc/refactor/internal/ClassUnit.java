package edu.ucsc.refactor.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.internal.visitors.SelectedASTNodeVisitor;
import edu.ucsc.refactor.util.AstUtil;
import edu.ucsc.refactor.util.Locations;
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

    @Override public List<Location> getLocations(Context context) {
        final List<Location> locations = Lists.newArrayList();
        final List<Location> instances = Locations.locateWord(context.getSource(), getName());

        for(Location each : instances){
            final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(each);
            context.accept(visitor);

            final TypeDeclaration declaration = AstUtil.parent(
                    TypeDeclaration.class,
                    visitor.getMatchedNode()
            );

            if(declaration != null && getName().equals(declaration.getName().getIdentifier())){
                // ignore instance creation, parameter passing,... just give me its declaration
                locations.add(new ProgramUnitLocation(declaration, each));
            }

        }

        return ImmutableList.copyOf(locations);
    }
}
