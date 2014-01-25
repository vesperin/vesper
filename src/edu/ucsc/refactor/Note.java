package edu.ucsc.refactor;

import com.google.common.base.Objects;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Note implements Comparable<Note> {

    private final AtomicReference<String>   id;
    private final AtomicReference<String>   user;
    private final AtomicReference<String>   content;
    private final AtomicReference<Location> mark;

    /**
     * Construct a {@code Note} with its content as value.
     * @param content The {@code Note}'s content.
     */
    public Note(String content){
        this(null, null, content, null);
    }

    /**
     * Constructs a {@code Note} with id and user as values.
     * @param id  The id of the note.
     * @param user The user who created the note.
     * @param content The {@code Note}'s content.
     */
    public Note(String id, String user, String content){
        this(id, user, content, null);
    }

    /**
     * Constructs a {@code Note} with id, user, content, and mark as values.
     * @param id The id of the note.
     * @param user The user who created the note.
     * @param content The content of the note.
     * @param mark The location in the {@code Source} where the note has been created.
     */
    Note(String id, String user, String content, Location mark){
        this.id         = new AtomicReference<String>(id);
        this.user       = new AtomicReference<String>(user);
        this.content    = new AtomicReference<String>(content);
        this.mark       = new AtomicReference<Location>(mark);
    }

    @Override public int compareTo(Note that) {
        final boolean isLocationEnabled = getMark() != null;
        return (isLocationEnabled
                    ? getMark().compareTo(that.getMark())
                    : getContent().compareTo(that.getContent())
        );
    }

    /**
     * @return The {@code Note}'s id.
     */
    public String getId(){  return id.get(); }

    /**
     * @return The {@code Note}'s creator.
     */
    public String getUser(){ return user.get(); }

    /**
     * @return The {@code Note}'s content.
     */
    public String getContent(){ return content.get(); }

    /**
     * @return The {@code Note}'s mark location.
     */
    public Location getMark(){ return mark.get(); }

    /**
     * Sets the {@code Note}'s content.
     *
     * @param content The {@code Note}'s content.
     * @return {@code true} if content was set.
     */
    public boolean setContent(String content){
        final String old = getContent();
        return this.content.compareAndSet(old, content);
    }

    /**
     * Sets the {@code Note}'s id.
     *
     * @param id The {@code Note}'s id.
     * @return {@code true} if id was set.
     */
    public boolean setId(String id){
        final String old = getId();
        return this.id.compareAndSet(old, id);
    }

    /**
     * Sets the {@code Note}'s location.
     *
     * @param newLocation The {@code Note}'s location.
     * @return {@code true} if location was set.
     */
    public boolean setMark(Location newLocation){
        final Location old = getMark();
        return this.mark.compareAndSet(old, newLocation);
    }

    /**
     * Sets the {@code Note}'s selected code block.
     *
     * @param selection The {@code Note}'s selected code block.
     * @return {@code true} if code selection was set.
     */
    public boolean setMark(SourceSelection selection){
        return setMark(selection.toLocation());
    }

    /**
     * Sets the {@code Note}'s creator.
     *
     * @param user The {@code Note}'s creator.
     * @return {@code true} if the name of user who created the note was set.
     */
    public boolean setUser(String user){
        final String old = getUser();
        return this.user.compareAndSet(old, user);
    }


    @Override public String toString() {
        return Objects.toStringHelper("Note")
                .add("id", getId())
                .add("user", getUser())
                .add("content", getContent())
                .add("location", getMark())
                .toString();
    }
}
