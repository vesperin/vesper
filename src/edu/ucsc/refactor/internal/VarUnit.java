package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.internal.visitors.LocalVariableVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;
import java.util.Set;

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
        final LocalVariableVisitor visitor = new LocalVariableVisitor(getName());
        context.accept(visitor);

        final Set<NamedLocation> locations = Sets.newHashSet();

        for(VariableDeclarationStatement statements : visitor.getLocalVariables()){
            locations.add(new ProgramUnitLocation(statements, Locations.locate(statements)));
        }

        return ImmutableList.copyOf(locations);
    }
}
