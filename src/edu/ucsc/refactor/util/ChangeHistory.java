package edu.ucsc.refactor.util;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ChangeHistory implements Iterable<Checkpoint> {
    private final List<Checkpoint> storage;

    /**
     * Creates a change history
     */
    public ChangeHistory(){
        this(null);
    }

    /**
     * Creates a change history
     * @param head ThE HEAD checkpoint.
     */
    public ChangeHistory(Checkpoint head){
        this.storage  = Lists.newArrayList();
        if(head != null){
            add(head);
        }
    }


    /**
     * Adds a checkpoint to the change history if it is not already present.
     * @param checkpoint the checkpoint to add
     * @return {@code true} if checkpoint was added.
     * @throws NullPointerException if <tt>checkpoint</tt> is <tt>null</tt>
     */
    public final boolean add(Checkpoint checkpoint) {
        if (checkpoint == null) {
            throw new NullPointerException(
                    "called add() with a null checkpoint"
            );
        }

        return storage.add(checkpoint);
    }


    /**
     * Does this symbol table contain the given note?
     * @param note the note to be checked
     * @return <tt>true</tt> if this symbol table contains <tt>note</tt> and
     *     <tt>false</tt> otherwise
     * @throws NullPointerException if <tt>note</tt> is <tt>null</tt>
     */
    public boolean contains(Checkpoint note) {
        if (note == null) {
            throw new NullPointerException(
                    "called contains() with a null note"
            );
        }

        return storage.contains(note);
    }

    /**
     * Clears the symbol table.
     */
    public void clear(){
        this.storage.clear();
    }

    /**
     * Slice the change history, going back ALL THE WAY to the HEAD checkpoint.
     *
     * @return a new ChangeHistory having ONLY the HEAD checkpoint
     */
    public ChangeHistory slice(){
        try {
            return slice(first());
        } catch (NoSuchElementException e){
            return new ChangeHistory();
        }
    }

    /**
     * Resets the change history, going back to the given checkpoint.
     * @return a new ChangeHistory containing all checkpoints until the given checkpoint.
     * @throws NullPointerException if fromElement or toElement is null and this
     *      set uses natural ordering, or its comparator does not permit null elements
     */
    public ChangeHistory slice(final Checkpoint upto/*checkpoint*/){
        // 1, 2, 3, 4
        // reset(0) => 1
        // reset(2) => 1, 2
        final Checkpoint from = Preconditions.checkNotNull(first());
        final Checkpoint to   = Preconditions.checkNotNull(upto);

        final ChangeHistory sliced = new ChangeHistory(from);

        if(!from.equals(to)) {
            //
            // 0, 1, 2, 3, 4, 5
            // 1, 2, 3, 4, 5, 6
            int fromIndex = index(from, this) + 1;
            int toIndex   = index(to, this);

            final ImmutableList<Checkpoint> rest = FluentIterable
                    .from(this)
                    .skip(fromIndex)
                    .limit((toIndex - fromIndex) + 1)
                    .toList();

            for(Checkpoint each : rest){
                sliced.add(each);
            }

        }

        return sliced;
    }

    public static int index(final Checkpoint point, Iterable<Checkpoint> iterable){
        final Predicate<Checkpoint> match = new Predicate<Checkpoint>() {
            @Override public boolean apply(Checkpoint that) {
                return point.equals(that);
            }
        };

        return Iterables.indexOf(iterable, match);
    }


    /**
     * Removes the checkpoint from the set if the checkpoint is present.
     * @param checkpoint the checkpoint to be deleted.
     * @return {@code true} if checkpoint was deleted.
     * @throws NullPointerException if <tt>checkpoint</tt> is <tt>null</tt>
     */
    public boolean delete(Checkpoint checkpoint) {
        if (checkpoint == null) {
            throw new NullPointerException(
                    "called delete() with a null checkpoint"
            );
        }

        return storage.remove(checkpoint);
    }


    /**
     * Returns the first checkpoint (a.k.a., HEAD). If we are dealing with
     * one element, then {@code first()} and {@code last()} will
     * be the same.
     *
     * @return the first note in the Notes symbol table.
     * @throws java.util.NoSuchElementException if the symbol table is empty
     */
    public Checkpoint first() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "called first() with empty symbol table"
            );
        }

        return Iterables.get(this, 0);
    }

    /**
     * Is this symbol table empty?
     * @return <tt>true</tt> if this symbol table is empty and <tt>false</tt> otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override public Iterator<Checkpoint> iterator() {
        return storage.iterator();
    }



    /**
     * Returns the last checkpoint in the symbol table.  If we are dealing with one element,
     * then {@code first()} and {@code last()} will be the same.
     *
     * @return the last note in the symbol table
     * @throws NoSuchElementException if the symbol table is empty
     */
    public Checkpoint last() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "called max() with empty symbol table"
            );
        }

        return Iterables.getLast(this);
    }

    /**
     * Returns the number of notes in this data structure.
     *
     * @return the number of notes in this data structure.
     */
    public int size() {
        return storage.size();
    }

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("data", storage)
                .toString();
    }
}
