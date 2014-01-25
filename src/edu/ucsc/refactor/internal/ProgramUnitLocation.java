package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.Position;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.NoSuchElementException;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ProgramUnitLocation implements NamedLocation {

    private final ASTNode           node;
    private final Location          location;


    private static final int METHOD_DECLARATION   = ASTNode.METHOD_DECLARATION;
    private static final int TYPE_DECLARATION     = ASTNode.TYPE_DECLARATION;
    private static final int VARIABLE_DECLARATION = ASTNode.SINGLE_VARIABLE_DECLARATION;
    private static final int FIELD_DECLARATION    = ASTNode.FIELD_DECLARATION;


    /**
     * Construct a new ProgramUnitLocation
     *
     * @param node the actual ASTNode
     * @param location the ASTNode location
     */
    public ProgramUnitLocation(ASTNode node, Location location){
        this.node       = Preconditions.checkNotNull(node);
        this.location   = Preconditions.checkNotNull(location);
    }

    @Override public String getName() {
        final ASTNode node = getNode();

        switch (node.getNodeType()){
            case METHOD_DECLARATION: {
                final MethodDeclaration method = AstUtil.exactCast(MethodDeclaration.class, node);
                final TypeDeclaration clazz =  AstUtil.exactCast(TypeDeclaration.class, method.getParent());
                return "type(" + clazz.getName().getIdentifier() + ")";
            }

            case TYPE_DECLARATION: {
                final TypeDeclaration clazz =  AstUtil.exactCast(TypeDeclaration.class, node);
                return "type(" + clazz.getName().getIdentifier() + ")";
            }

            case VARIABLE_DECLARATION: {
                final SingleVariableDeclaration param = AstUtil.exactCast(SingleVariableDeclaration.class, node);
                final MethodDeclaration method = AstUtil.exactCast(MethodDeclaration.class, param.getParent());
                return "method(" + method.getName().getIdentifier() + ")";
            }

            case FIELD_DECLARATION: {
                final FieldDeclaration field = AstUtil.exactCast(FieldDeclaration.class, node);
                final TypeDeclaration  clazz = AstUtil.exactCast(TypeDeclaration.class, field.getParent());
                return "type(" + clazz.getName().getIdentifier() + ")";
            }

            default: throw new NoSuchElementException("unknown ASTNode");
        }
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
