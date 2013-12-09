package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Position;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceLocation;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Locations {
    private Locations(){
        throw new Error(
                "This class cannot be instantiated."
        );
    }


    /**
     * Locates a ASTNode in the {@code Source}.
     *
     * @param node The ASTNode to be located.
     * @return A {@code Location} in the {@code Source} where a ASTNode is found.
     */
    public static Location locate(ASTNode node) {
        final Source src = Source.from(node);
        return SourceLocation.createLocation(
                src,
                src.getContents(),
                node.getStartPosition(),
                node.getStartPosition() + node.getLength()
        );
    }


    /**
     * Checks whether both locations are the same.
     */
    public static boolean bothSame(Location base, Location other){
        final Position start = base.getStart();
        final Position end   = base.getEnd();

        final Position otherStart   = other.getStart();
        final Position otherEnd     = other.getEnd();

        return start.equals(otherStart) && end.equals(otherEnd);
    }

    /**
     * Returns {@code true} if one node's location (the base) cover another node's location.
     *
     * @param base The base location.
     * @param other The location of another node.
     * @return {@code true} if a code location of a node covers the location of another node.
     */
    public static boolean covers(Location base, Location other){
        final Position start = base.getStart();
        final Position end   = base.getEnd();

        final int startLine         = start.getLine();
        final int exclusiveEndLine  = end.getLine() + 1;
        final int otherStartLine    = other.getStart().getLine();
        final int otherEndLine      = other.getEnd().getLine();

        return startLine <= otherStartLine
                &&  otherEndLine <= exclusiveEndLine;
    }



    /**
     * Returns {@code true} if this node's location covers a node at given start position.
     *
     * @param base The base location.
     * @param position The other node's start position.
     * @return {@code true} if this node's location covers a node at given start position.
     */
    public static boolean coversAtPosition(Location base, Position position){
        final Position start    = base.getStart();
        final Position end      = base.getEnd();

        final int startLine     = start.getLine();
        final int endLine       = end.getLine();

        return startLine <= position.getLine()
                && position.getLine() < endLine;

    }


    /**
     * Returns {@code true} if one node's location (base) is covered by another node's location.
     *
     * @param other The location of another node.
     * @return {@code true} if a code location of a node is covered by the location of another
     * node.
     */
    public static boolean coveredBy(Location base, Location other){
        //new SourceCovering(other).covers(this.base);
        return Locations.covers(other, base);
    }


    /**
     * Returns {@code true} if one node's location ends in another node's location.
     *
     * @param base The base location.
     * @param other The location of another node.
     * @return {@code true} if a given code location ends in another code location.
     */
    public static boolean endsIn(Location base, Location other){
        final Position otherStart   = other.getStart();
        final Position otherEnd     = other.getEnd();
        final Position end          = base.getEnd();

        final int otherStartLine    = otherStart.getLine();
        final int otherEndLine      = otherEnd.getLine();
        final int exclusiveEndLine  = end.getLine() + 1;

        return otherStartLine < exclusiveEndLine && exclusiveEndLine < otherEndLine;
    }


    /**
     * Returns {@code true} if one node's location (base) lies outside another node's location.
     *
     * @param base The base location.
     * @param other The location of another node.
     * @return {@code true} if a given node location lies outside another node's location.
     */
    public static boolean liesOutside(Location base, Location other){
        final Position otherStart   = other.getStart();
        final Position otherEnd     = other.getEnd();
        final Position start        = base.getStart();
        final Position end          = base.getEnd();

        final int otherStartLine    = otherStart.getLine();
        final int otherEndLine      = otherEnd.getLine();
        final int exclusiveEndLine  = end.getLine() + 1;
        final int startLine         = start.getLine();

        final boolean locationBeforeSelection = startLine < otherStartLine;
        final boolean selectionBeforeLocation = otherEndLine < exclusiveEndLine;

        return locationBeforeSelection || selectionBeforeLocation;
    }


    /**
     * Returns {@code true} if the current location's selection intersects with another
     * location.
     *
     * @param other The location of another node.
     * @return {@code true} if <tt>this</tt> location intersects with another location.
     */
    public static boolean intersects(Location base, Location other){
        final Position otherStart   = other.getStart();
        final Position otherEnd     = other.getEnd();

        final int otherStartLine    = otherStart.getLine();
        final int otherEndLine      = otherEnd.getLine();

        final Position start        = base.getStart();
        final Position end          = base.getEnd();
        final int startLine         = start.getLine();
        final int exclusiveEndLine  = end.getLine() + 1;

        final boolean before     = otherEndLine <= startLine;
        final boolean inside     = covers(base, other);
        final boolean after      = exclusiveEndLine <= otherStartLine;

        return !before || !inside || !after;
    }
}
