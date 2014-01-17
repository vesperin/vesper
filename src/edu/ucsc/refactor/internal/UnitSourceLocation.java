package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Position;
import edu.ucsc.refactor.Source;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class UnitSourceLocation implements Location {

    private final ASTNode           node;
    private final Location          location;

    /**
     * Construct a new UnitSourceLocation
     *
     * @param node the actual ASTNode
     * @param location the ASTNode location
     */
    public UnitSourceLocation(ASTNode node, Location location){
        this.node       = Preconditions.checkNotNull(node);
        this.location   = Preconditions.checkNotNull(location);
    }

    public ASTNode getNode(){
        return node;
    }

    @Override public Source getSource() {
        return location.getSource();
    }

    @Override public Position getStart() {
        return location.getStart();
    }

    @Override public Position getEnd() {
        return location.getEnd();
    }

    @Override public int compareTo(Location that) {
        return location.compareTo(that);
    }

    @Override public String toString() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(node.getClass());
        builder.add("start", getStart());
        builder.add("end", getEnd());
        return builder.toString();
    }
}
