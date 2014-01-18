package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.internal.visitors.FieldDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.internal.visitors.SelectedASTNodeVisitor;
import edu.ucsc.refactor.spi.ProgramUnit;
import edu.ucsc.refactor.spi.UnitLocator;
import edu.ucsc.refactor.util.AstUtil;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ProgramUnitLocator implements UnitLocator {
    private final Context  context;
    private final Record   record;

    /**
     * Constructs a new {@code ProgramUnitLocator} with a {@code Context} as
     * a value.
     *
     * @param context THe Java {@code Context}.
     */
    public ProgramUnitLocator(Context context){
        this.context    = context;
        this.record     = new Record();
    }

    @Override public List<Location> locate(String name, ProgramUnit unit) {
        track(name, unit);

        switch (unit){
           case INNER_CLASS:  return locateInnerClass(name);
           case METHOD:       return locateMethod(name);
           case PARAM:        return locateParam(name);
           case FIELD:        return locateField(name);
        }

        return ImmutableList.of();
    }


    private List<Location> locateInnerClass(String key){
        final List<Location> locations = Lists.newArrayList();
        final List<Location> instances = Locations.locateWord(context.getSource(), key);

        for(Location each : instances){
            final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(each);
            context.accept(visitor);

            final TypeDeclaration declaration = AstUtil.parent(TypeDeclaration.class, visitor.getMatchedNode());

            if(declaration != null){
                locations.add(new ProgramUnitLocation(declaration, each));
            }

        }

        return ImmutableList.copyOf(locations);
    }


    private List<Location> locateMethod(String key){

        final MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        context.accept(visitor);

        final List<Location> locations = Lists.newArrayList();

        final List<MethodDeclaration> methods = visitor.getMethodDeclarations();
        for(MethodDeclaration each : methods){
            if(each.getName().getIdentifier().equalsIgnoreCase(key)){
               locations.add(new ProgramUnitLocation(each, Locations.locate(each)));
            }
        }

        return ImmutableList.copyOf(locations);
    }

    private List<Location> locateParam(String key){
        final Set<Location> locations = Sets.newHashSet();
        final List<Location> instances = Locations.locateWord(context.getSource(), key);
        for(Location each : instances){
            final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(each);
            context.accept(visitor);

            final SingleVariableDeclaration variable = AstUtil.parent(SingleVariableDeclaration.class, visitor.getMatchedNode());
            if(variable != null){
                locations.add(new ProgramUnitLocation(variable, each));
            }
        }

        return ImmutableList.copyOf(locations);
    }

    private List<Location> locateField(String key){

        final FieldDeclarationVisitor visitor = new FieldDeclarationVisitor();
        context.accept(visitor);

        final Set<Location> locations = Sets.newHashSet();

        if(visitor.hasFieldName(key)){
            for(FieldDeclaration each : visitor.getMatchingFieldDeclaration(key)){
               locations.add(new ProgramUnitLocation(each, Locations.locate(each)));
            }
        }

        return ImmutableList.copyOf(locations);
    }


    private void track(String key, ProgramUnit hint){
        record.key  = Preconditions.checkNotNull(key);
        record.hint = Preconditions.checkNotNull(hint);
    }

    @Override public String toString() {
        final String        target = record.key == null ? ".." : record.key;
        final ProgramUnit hint   = record.hint == null ? ProgramUnit.NONE : record.hint;
        return "Search for " + target + " " + hint + " in " + context;
    }


    static class Record {
        String      key;
        ProgramUnit hint;
    }
}
