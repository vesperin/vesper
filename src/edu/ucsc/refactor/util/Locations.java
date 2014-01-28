package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Position;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceLocation;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return Locations.locate(src, node);
    }

    /**
     * Locates a ASTNode in the {@code Source}.
     *
     * @param src The Source being inspected.
     * @param node The ASTNode to be located.
     * @return A {@code Location} in the {@code Source} where a ASTNode is found.
     */
    public static Location locate(Source src, ASTNode node){
        return SourceLocation.createLocation(
                src,
                src.getContents(),
                node.getStartPosition(),
                node.getStartPosition() + node.getLength()
        );
    }

    /**
     * Locates a word in the {@code Source}
     *
     * @param code The {@code Source} to be inspected.
     * @param word The word to be located
     *
     * @return The location of the word in the {@code Source}
     */
    public static List<Location> locateWord(Source code, String word){
        final List<Location> locations = new ArrayList<Location>();

        final String  REGEX     = "\\b" + word + "\\b";
        final Pattern pattern   = Pattern.compile(REGEX);
        final Matcher matcher   = pattern.matcher(code.getContents());

        while(matcher.find()) {
            final int start = matcher.start();
            final int end   = matcher.end();

            final Location location = SourceLocation.createLocation(
                    code,
                    code.getContents(),
                    start,
                    end
            );

            locations.add(location);
        }

        return locations;
    }

    public static boolean isBeforeBaseLocation(Location base, Location other){
        final Position otherEnd     = other.getEnd();
        final int nodeEnd           = otherEnd.getOffset();
        final Position start        = base.getStart();

        return (nodeEnd <= start.getOffset());
    }


    public static boolean isAfterBaseLocation(Location base, Location other){
        final Position otherStart   = other.getStart();
        final Position end          = base.getEnd();

        final int nodeStart     = otherStart.getOffset();
        final int exclusiveEnd  = end.getOffset() + 1;


        return (exclusiveEnd <= nodeStart);
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

        final Position otherStart = other.getStart();
        final Position otherEnd   = other.getEnd();

        final int exclusiveEndOffset = end.getOffset() + 1;

        return start.getOffset() <= otherStart.getOffset()
                && (otherEnd.getOffset()) <= exclusiveEndOffset;
    }


    /**
     * Returns {@code true} if one node's location (the other) is inside another node's location (base).
     *
     * @param base The base location.
     * @param other The location of another node.
     * @return {@code true} if a code location of a node is inside the location of another node.
     */
    public static boolean inside(Location base, Location other){
        final Position start = base.getStart();
        final Position end   = base.getEnd();

        final Position otherStart = other.getStart();
        final Position otherEnd   = other.getEnd();

        return start.getOffset() < otherStart.getOffset()
                && (otherEnd.getOffset()) < end.getOffset();
    }



    /**
     * Returns {@code true} if this node's location covers a node at given start position.
     *
     * @param base The base location.
     * @param position The other node's start position.
     * @return {@code true} if this node's location (base) covers a node at given start position.
     */
    public static boolean begins(Location base, Position position/*start position*/){
        final Position start    = base.getStart();

        return start.getOffset() <= position.getOffset();

    }


    /**
     * Returns {@code true} if this node's location covers a node at given start position.
     *
     * @param base The base location.
     * @param position The other node's start position.
     * @return {@code true} if this node's location (base) covers a node at given start position.
     */
    public static boolean ends(Location base, Position position/*end position*/){
        final Position end      = base.getEnd();

        return end.getOffset() <= position.getOffset();

    }


    /**
     * Returns {@code true} if one node's location (base) is covered by another node's location.
     *
     * @param other The location of another node.
     * @return {@code true} if a code location of a node is covered by the location of another
     * node.
     */
    public static boolean coveredBy(Location base, Location other){
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


        final int nodeStart     = otherStart.getOffset();
        final int exclusiveEnd  = end.getOffset() + 1;

        return nodeStart < exclusiveEnd
                && exclusiveEnd < otherEnd.getOffset();
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

        final int nodeStart = otherStart.getOffset();
        final int nodeEnd   = otherEnd.getOffset();

        final int exclusiveEnd = end.getOffset() + 1;

        final boolean nodeBeforeBase = nodeEnd < start.getOffset();
        final boolean baseBeforeNode = exclusiveEnd < nodeStart;

        return nodeBeforeBase || baseBeforeNode;
    }


    /**
     * Returns {@code true} if the current location's selection intersects with another
     * location.
     *
     * @param other The location of another node.
     * @return {@code true} if <tt>this</tt> location intersects with another location.
     */
    public static boolean intersects(Location base, Location other){
        return !Locations.isBeforeBaseLocation(base, other)     // !before
                && !(covers(base, other))                       // !within
                && !Locations.isAfterBaseLocation(base, other); // !after
    }
}
