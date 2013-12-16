package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Notes;
import edu.ucsc.refactor.util.StringUtil;
import edu.ucsc.refactor.util.ToStringBuilder;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
// todo(Huascar) investigate the need for implementing equals and hashCode
public class Source {
    public static final String SOURCE_FILE_PROPERTY = "kae.source_file.source_file_property";

    private final String        name;
    private final String        contents;
    private final String        description;
    private final Notes notes;

    private final AtomicReference<String> id;

    /**
     * construct a new {@link Source} object.
     *
     * @param name The name of file
     * @param contents The file's contents.
     */
    public Source(String name, String contents){
        this(
                name,
                contents,
                generateDescription(name)
        );
    }

    /**
     * construct a new {@link Source} object.
     *
     * @param name The name of file
     * @param contents The file's contents.
     * @param description The file's description.
     */
    public Source(String name, String contents, String description){
        this.name        = name;
        this.contents    = contents;
        this.description = description;
        this.id          = new AtomicReference<String>();
        this.notes       = new Notes();
    }

    /**
     * Retrieves the {@link Source} object for a given {@link ASTNode node}.
     *
     * @param node The {@link ASTNode} node.
     * @return the {@link Source} object.
     */
    public static Source from(ASTNode node){
       return (Source) node.getRoot().getProperty(Source.SOURCE_FILE_PROPERTY);
    }

    /**
     * Adds a note or mark describing the {@code Source}
     *
     * @param note The note to be added.
     * @return {@code true} if the note was added. {@code false} otherwise.
     */
    public boolean addNote(Note note){
        return this.notes.add(note);
    }


    private static String generateDescription(String fromName){
        return "Java: " + StringUtil.splitCamelCase(StringUtil.extractName(fromName));
    }

    /**
     * Gets a human readable description of file
     *
     * @return The file's description
     */
    public String getDescription(){ return description; }

    /**
     * Gets the file's contents
     *
     * @return The file's contents
     */
    public String getContents() { return contents; }

    /**
     * @return The general {@code Note} describing the entire {@code Source}.
     */
    public Note getGeneralNote(){ return getNotes().first(); }

    /**
     * @return All the notes describing the {@code Source}.
     */
    public Notes getNotes(){ return this.notes; }

    /**
     * Gets the file's id
     *
     * @return The file's id
     */
    public String getId(){ return id.get();  }

    /**
     * Gets the name of source file.
     *
     * @return The file's name
     */
    public String getName()     { return name; }

    /**
     * Gets the content's length.
     *
     * @return The content's length.
     */
    public int getLength()      { return getContents().length(); }

    /**
     * Removes a note from symbol table.
     *
     * @return {@code true} if the note was removed.
     */
    public boolean removeNote(Note note){
        return this.notes.delete(note);
    }

    /**
     * Sets the id of the {@code Source}. A non-null id means this
     * {@code Source} has been persisted.
     *
     * @param id The {@code Source}'s id.
     * @return {@code true} if the id was set.
     */
    public boolean setId(String id){
        final String old = getId();
        return this.id.compareAndSet(old, id);
    }

    @Override public String toString() {
        final ToStringBuilder builder = new ToStringBuilder("Source");
        if(getId() != null){
            builder.add("id", getId());
        }

        builder.add("name", StringUtil.extractName(getName()));
        return builder.toString();

    }

    /**
     * Converts this {@code Source} into a jface's {@code Document}.
     * @return The {@link IDocument}
     */
    public IDocument toDocument() {
        return new Document(getContents());
    }
}
