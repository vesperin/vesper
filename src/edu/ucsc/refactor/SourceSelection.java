package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.SourceLocation;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * <strong>Important Note:</strong>
 * <p>
 * `One` source selection takes multiple locations or points in a file
 * e.g., in the case of drafted where the user can make multiple
 * selections on the same file, those multiple selections represent locations
 * to be added to the overall source selection object (`One`).
 * </p>
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceSelection implements Iterable<Location> {
    private final TreeSet<Location> selections;

    private Context context;

    /**
     * Instantiates a new {@link SourceSelection}.
     *
     * @param code The {@link Source}
     * @param startOffset The selection's starting offset
     * @param endOffset The selection's ending offset
     */
    public SourceSelection(Source code, int startOffset, int endOffset){
        this(SourceLocation.createLocation(
                code,
                code.getContents(),
                startOffset,
                endOffset
        ));
    }

    /**
     * Instantiates a new {@link SourceSelection}.
     * @param location The first selection or location.
     */
    public SourceSelection(Location location){
        this.selections  = new TreeSet<Location>();
        if(location != null){
            add(location);
        }
    }

    /**
     * Internal Constructor.
     */
    SourceSelection(){
        this(null);
    }

    /**
     * Adds a location to the source selection if it is not already present.
     * @param location the location to add
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     */
    public void add(Location location) {
        if (location == null) {
            throw new NullPointerException(
                    "called add() with a null location"
            );
        }

        selections.add(location);
    }


    /**
     * Does this symbol table contain the given location?
     * @param location the location to be checked
     * @return <tt>true</tt> if this symbol table contains <tt>location</tt> and
     *     <tt>false</tt> otherwise
     * @throws NullPointerException if <tt>location</tt> is <tt>null</tt>
     */
    public boolean contains(Location location) {
        if (location == null) {
            throw new NullPointerException(
                    "called contains() with a null location"
            );
        }

        return selections.contains(location);
    }


    /**
     * Returns the smallest location in the selection greater than or equal to <tt>location</tt>.
     * @return the smallest location in the set greater than or equal to <tt>location</tt>
     * @param other the other location
     * @throws NoSuchElementException if there is no such location
     * @throws NullPointerException if <tt>location</tt> is <tt>null</tt>
     */
    public Location ceiling(Location other) {
        if (other == null) {
            throw new NullPointerException(
                    "called ceiling() with a null location"
            );
        }

        Location k = selections.ceiling(other);
        if (k == null) {
            throw new NoSuchElementException(
                    "all locations are less than " + other
            );
        }

        return k;
    }


    /**
     * Removes the location from the set if the location is present.
     * @param location the location to be deleted.
     * @throws NullPointerException if <tt>location</tt> is <tt>null</tt>
     */
    public void delete(Location location) {
        if (location == null) {
            throw new NullPointerException(
                    "called delete() with a null location"
            );
        }

        selections.remove(location);
    }


    /**
     * Returns the first location in the source selection. If this selection
     * consists of one element, then {@code first()} and {@code last()} will
     * be the same.
     *
     * @return the first location in the source selection
     * @throws NoSuchElementException if the selection is empty
     */
    public Location first() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "called min() with empty selection"
            );
        }

        return selections.first();
    }


    /**
     * Returns the largest location in the selection less than or equal to <tt>location</tt>.
     * @return the largest location in the selection less than or equal to <tt>location</tt>
     * @param location the other location
     * @throws NoSuchElementException if there is no such location
     * @throws NullPointerException if <tt>location</tt> is <tt>null</tt>
     */
    public Location floor(Location location) {
        if (location == null) {
            throw new NullPointerException(
                    "called floor() with a null location"
            );
        }

        Location k = selections.floor(location);
        if (k == null) {
            throw new NoSuchElementException(
                    "all keys are greater than " + location
            );
        }

        return k;
    }


    /**
     * Returns the {@link Context} this selection uses to locate the node
     * this selection corresponds to.
     *
     * @return the {@link Context}
     */
    public Context getContext(){
        return context;
    }

    /**
     * @return The {@code Source} where a user is selecting a block of code.
     */
    public Source getSource(){
        final Context context = getContext();
        Source src;

        if(context == null){ src = first().getSource(); } else {
            src = context.getSource();
        }

        return src;
    }


    /**
     * Returns the intersection of this selection and that selection.
     * @param that the other selection
     * @return the intersection of this selection and that selection
     * @throws NullPointerException if <tt>that</tt> is <tt>null</tt>
     */
    public SourceSelection intersects(SourceSelection that) {
        if (that == null) {
            throw new NullPointerException(
                    "called intersects() with a null argument"
            );
        }

        SourceSelection c = new SourceSelection();

        if (this.size() < that.size()) {
            for (Location x : this) {
                if (that.contains(x)) c.add(x);
            }
        } else {
            for (Location x : that) {
                if (this.contains(x)) c.add(x);
            }
        }
        return c;
    }


    /**
     * Is this source selection empty?
     * @return <tt>true</tt> if this source selection is empty and <tt>false</tt> otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }


    /**
     * Returns all of the locations part of this source selection as an iterator.
     * To iterate over all of the locations in a set named <tt>selection</tt>,
     * use the foreach notation: <tt>for (Location location : selection)</tt>.
     *
     * @return an iterator to all of the locations in the selection
     */
    @Override public Iterator<Location> iterator() {
        return selections.iterator();
    }


    /**
     * Returns the last location in the source selection.  If this selection
     * consists of one element, then {@code first()} and {@code last()} will
     * be the same.
     *
     * @return the last location in the source selection
     * @throws NoSuchElementException if the selection is empty
     */
    public Location last() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "called max() with empty selection"
            );
        }

        return selections.last();
    }


    /**
     * Returns the number of selections in this source selection.
     *
     * @return the number of selections in this source selection.
     */
    public int size() {
        return selections.size();
    }


    /**
     * Sets the context for this selection.
     * @param context The context of source, which this selection is part of.
     */
    public void setContext(Context context) {
        this.context = context;
    }


    /**
     * Returns the union of this selection and that selection. This applies only for
     * non intersecting selections.
     *
     * @param that the other selection
     * @return the union of this set and that set
     * @throws NullPointerException if <tt>that</tt> is <tt>null</tt>
     */
    public SourceSelection union(SourceSelection that) {
        if (that == null) {
            throw new NullPointerException(
                    "called union() with a null argument"
            );
        }

        SourceSelection c = new SourceSelection();

        // Only non intersecting selections can be joined together into
        // this selection.
        if(!intersects(that).isEmpty()){
            c.add(first());
            return c;
        }

        for (Location x : this) { c.add(x); }
        for (Location x : that) { c.add(x); }

        return c;
    }

    /**
     * Returns the location that represents this {@code SourceSelection}.
     *
     * @return The {@code Location} representing this {@code SourceSelection}.
     */
    public Location toLocation(){
        final Context context = getContext();
        final Location start  = first();
        final Location end    = last();

        final Source code = context != null ? context.getSource() : start.getSource();
        return SourceLocation.createLocation(code, start.getStart(), end.getEnd());
    }


    /**
     * Returns a string representation of this set.
     * @return a string representation of this set, with the keys separated
     *   by single spaces
     */
    @Override public String toString() {
        StringBuilder s = new StringBuilder();
        for (Location key : this)
            s.append(key).append(" ");
        return s.toString();
    }
}
