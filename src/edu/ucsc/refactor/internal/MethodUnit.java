package edu.ucsc.refactor.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.util.Locations;
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

    @Override public List<Location> getLocations(Context context) {
        final MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        context.accept(visitor);

        final List<Location> locations = Lists.newArrayList();

        final List<MethodDeclaration> methods = visitor.getMethodDeclarations();
        for(MethodDeclaration each : methods){
            if(each.getName().getIdentifier().equalsIgnoreCase(getName())){
                locations.add(new ProgramUnitLocation(each, Locations.locate(each)));
            }
        }

        return ImmutableList.copyOf(locations);
    }
}
