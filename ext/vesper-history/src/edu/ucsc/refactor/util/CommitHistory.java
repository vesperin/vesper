package edu.ucsc.refactor.util;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.Commit;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitHistory implements Iterable<Commit> {
    private final TreeSet<Commit> storage;

    /**
     * Creates a commit history
     */
    public CommitHistory(){
        this(null);
    }

    /**
     * Creates a commit history
     * @param head ThE HEAD checkpoint.
     */
    public CommitHistory(Commit head){
        this.storage  = Sets.newTreeSet();
        if(head != null){
            add(head);
        }
    }


    /**
     * Adds a commit to the change history if it is not already present.
     * @param commit the commit to add
     * @return {@code true} if commit was added.
     * @throws NullPointerException if <tt>commit</tt> is <tt>null</tt>
     */
    public final boolean add(Commit commit) {
        if (commit == null) {
            throw new NullPointerException(
                    "called add() with a null commit"
            );
        }

        return storage.add(commit);
    }


    /**
     * Does this symbol table contain the given note?
     * @param note the note to be checked
     * @return <tt>true</tt> if this symbol table contains <tt>note</tt> and
     *     <tt>false</tt> otherwise
     * @throws NullPointerException if <tt>note</tt> is <tt>null</tt>
     */
    public boolean contains(Commit note) {
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
    public CommitHistory slice(){
        try {
            return slice(first());
        } catch (NoSuchElementException e){
            return new CommitHistory();
        }
    }

    /**
     * Slices some part of history between the {@code fromElement} and the {@code toElement}.
     *
     * @param fromElement The left end point
     * @param fromInclusive {@code true} if the left point should be included in sliced
     *        history, {@code false} otherwise.
     * @param toElement The right end point
     * @param toInclusive {@code true} if the right point should be included in sliced
     *        history, {@code false} otherwise.
     * @return a sliced history
     */
    public CommitHistory slice(final Commit fromElement, boolean fromInclusive,
                               final Commit toElement, boolean toInclusive){

        final Commit from = Preconditions.checkNotNull(fromElement);
        final Commit to   = Preconditions.checkNotNull(toElement);

        final CommitHistory sliced = new CommitHistory(from);

        if(!from.equals(to)) {
            final NavigableSet<Commit> rest = storage.subSet(
                    from,
                    fromInclusive,
                    to,
                    toInclusive
            );

            for(Commit each : rest){
                sliced.add(each);
            }

        }

        return sliced;
    }

    /**
     * Resets the change history, going back to the given checkpoint.
     * @return a new ChangeHistory containing all checkpoints until the given checkpoint.
     * @throws NullPointerException if fromElement or toElement is null and this
     *      set uses natural ordering, or its comparator does not permit null elements
     */
    public CommitHistory slice(final Commit upto/*checkpoint*/){
        return slice(first(), false, upto, true);
    }


    /**
     * Removes the commit from the set if the commit is present.
     * @param commit the commit to be deleted.
     * @return {@code true} if commit was deleted.
     * @throws NullPointerException if <tt>commit</tt> is <tt>null</tt>
     */
    public boolean delete(Commit commit) {
        if (commit == null) {
            throw new NullPointerException(
                    "called delete() with a null commit"
            );
        }

        return storage.remove(commit);
    }


    /**
     * Returns the first checkpoint (a.k.a., HEAD). If we are dealing with
     * one element, then {@code first()} and {@code last()} will
     * be the same.
     *
     * @return the first note in the Notes symbol table.
     * @throws java.util.NoSuchElementException if the symbol table is empty
     */
    public Commit first() {
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

    @Override public Iterator<Commit> iterator() {
        return storage.iterator();
    }



    /**
     * Returns the last checkpoint in the symbol table.  If we are dealing with one element,
     * then {@code first()} and {@code last()} will be the same.
     *
     * @return the last note in the symbol table
     * @throws NoSuchElementException if the symbol table is empty
     */
    public Commit last() {
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
