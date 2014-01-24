package edu.ucsc.refactor.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.internal.visitors.SelectedASTNodeVisitor;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.util.List;
import java.util.Set;

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

    @Override public List<Location> getLocations(Context context) {
        final Set<Location> locations = Sets.newHashSet();
        final List<Location> instances = Locations.locateWord(context.getSource(), getName());
        for(Location each : instances){
            final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(each);
            context.accept(visitor);

            final SingleVariableDeclaration variable = AstUtil.parent(
                    SingleVariableDeclaration.class,
                    visitor.getMatchedNode()
            );

            if(variable != null){
                locations.add(new ProgramUnitLocation(variable, each));
            }
        }

        return ImmutableList.copyOf(locations);
    }
}
