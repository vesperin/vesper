package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Note;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * A symbol table that deals with {@code Note}s.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Notes implements Iterable<Note> {
    private final TreeSet<Note> storage;

    /**
     * Constructs an empty {@code Notes} or symbol table.
     */
    public Notes(){
        this(null);
    }

    /**
     * Instantiates a new {@link Notes}.
     * @param first The first note (a.k.a., general note). A general note
     *              provides a description of the entire source code.
     */
    public Notes(Note first){
        this.storage  = new TreeSet<Note>();
        if(first != null){
            add(first);
        }
    }


    /**
     * Adds a note to the notes if it is not already present.
     * @param note the note to add
     * @return {@code true} if note was added.
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     */
    public boolean add(Note note) {
        if (note == null) {
            throw new NullPointerException(
                    "called add() with a null note"
            );
        }

        return storage.add(note);
    }


    /**
     * Does this symbol table contain the given note?
     * @param note the note to be checked
     * @return <tt>true</tt> if this symbol table contains <tt>note</tt> and
     *     <tt>false</tt> otherwise
     * @throws NullPointerException if <tt>note</tt> is <tt>null</tt>
     */
    public boolean contains(Note note) {
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
     * Removes the note from the set if the note is present.
     * @param note the note to be deleted.
     * @return {@code true} if note was deleted.
     * @throws NullPointerException if <tt>note</tt> is <tt>null</tt>
     */
    public boolean delete(Note note) {
        if (note == null) {
            throw new NullPointerException(
                    "called delete() with a null note"
            );
        }

        return storage.remove(note);
    }


    /**
     * Returns the first note (a.k.a., General Note). If we are dealing with
     * one element, then {@code first()} and {@code last()} will
     * be the same.
     *
     * <strong>Important</strong>: A general note provides a description of
     * the entire source code.
     *
     * @return the first note in the Notes symbol table.
     * @throws java.util.NoSuchElementException if the symbol table is empty
     */
    public Note first() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "called first() with empty symbol table"
            );
        }

        return storage.first();
    }


    /**
     * Is this symbol table empty?
     * @return <tt>true</tt> if this symbol table is empty and <tt>false</tt> otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override public Iterator<Note> iterator() {
        return storage.iterator();
    }


    /**
     * Returns the last note in the symbol table.  If we are dealing with one element,
     * then {@code first()} and {@code last()} will be the same.
     *
     * @return the last note in the symbol table
     * @throws NoSuchElementException if the symbol table is empty
     */
    public Note last() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "called max() with empty symbol table"
            );
        }

        return storage.last();
    }

    /**
     * Returns the number of notes in this data structure.
     *
     * @return the number of notes in this data structure.
     */
    public int size() {
        return storage.size();
    }


    /**
     * Returns the union of this selection and that selection. This method does
     * not provide any guarantees of the uniqueness of elements in the resulting
     * set. The caller must provide only unique elements if uniqueness of elements is
     * required.
     *
     * @param that the other selection
     * @return the union of this set and that set
     * @throws NullPointerException if <tt>that</tt> is <tt>null</tt>
     */
    public Notes union(Notes that) {
        if (that == null) {
            throw new NullPointerException(
                    "called union() with a null argument"
            );
        }

        Notes c = new Notes();

        for (Note x : this) { c.add(x); }
        for (Note x : that) { c.add(x); }

        return c;
    }

    /**
     * Returns the difference between {@link Notes this} and {@link Note that}.
     *
     * @param that The other {@code Notes}
     * @return The difference set.
     * @throws NullPointerException if <tt>that</tt> is <tt>null</tt>
     */
    public Notes difference(Notes that){

        if (that == null) {
            throw new NullPointerException(
                    "called difference() with a null argument"
            );
        }

        final Notes diff = new Notes();

        for(Note each : this){
            diff.add(each);
        }

        for(Note other : that ){
            if(diff.contains(other)){
                diff.delete(other);
            }
        }


        return diff;
    }

    /**
     * Returns a string representation of this data structure.
     *
     * @return a string representation of this data structure, with the keys separated
     *   by single spaces
     */
    @Override public String toString() {
        StringBuilder s = new StringBuilder();
        for (Note key : this)
            s.append(key).append(" ");
        return s.toString();
    }
}
