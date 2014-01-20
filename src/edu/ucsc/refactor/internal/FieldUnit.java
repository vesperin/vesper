package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.internal.visitors.FieldDeclarationVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.FieldDeclaration;

import java.util.List;
import java.util.Set;

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

    @Override public List<Location> getLocations(Context context) {
        Preconditions.checkNotNull(context);
        final FieldDeclarationVisitor visitor = new FieldDeclarationVisitor();
        context.accept(visitor);

        final Set<Location> locations = Sets.newHashSet();

        if(visitor.hasFieldName(getName())){
            for(FieldDeclaration each : visitor.getMatchingFieldDeclaration(getName())){
                locations.add(new ProgramUnitLocation(each, Locations.locate(each)));
            }
        }

        return ImmutableList.copyOf(locations);
    }
}
