package edu.ucsc.refactor;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.util.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Source {
    public static final String SOURCE_FILE_PROPERTY = "vesper.source_file.source_file_property";

    private static final String END = "}";
    private static final String REGEX = "class[^;=\\n]*\\s[\\S\\s]*?";

    private final String        contents;
    private final String        description;
    private final Notes         notes;

    private final AtomicReference<String> name;
    private final AtomicReference<String> id;

    private final AtomicReference<String> version;
    private final AtomicReference<String> signature;

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
        Preconditions.checkArgument(
                StringUtil.size(
                        StringUtil.extractFileName(Preconditions.checkNotNull(name))
                ) > 2, "name of class is too short"
        );

        this.name        = new AtomicReference<String>(name);
        this.contents    = contents;
        this.description = description;
        this.id          = new AtomicReference<String>();
        this.notes       = new Notes();
        this.signature   = new AtomicReference<String>();
        this.version     = new AtomicReference<String>();
    }

    /**
     * Creates a copy of {@code Source} from a seed {@code Source}
     * and some new content.
     *
     * @param seed The seed {@code Source}
     * @param newContent The new {@code Source}'s content.
     * @return The {@code Source}
     */
    public static Source from(Source seed, String newContent){
        final Pattern pattern = Pattern.compile(REGEX);
        final Matcher matcher   = pattern.matcher(newContent);

        final String tentativeName      = matcher.find() ? StringUtil.extractClassName
                (newContent) : StringUtil.extractFileName(seed.getName());
        final String nameWithoutJavaExt = StringUtil.extractFileName(seed.getName());

        final String name          = StringUtil.equals(nameWithoutJavaExt, tentativeName) ? nameWithoutJavaExt : tentativeName;

        final Source  copy  = new Source(
                name + ".java",
                newContent,
                seed.getDescription().replace(nameWithoutJavaExt, name)
        );

        copy.setId(seed.getId());
        copy.setSignature(seed.getUniqueSignature());

        for(Note each : seed.getNotes()){
            copy.addNote(each);
        }


        return copy;
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
     * Performs a quick patching, for refactoring purposes.
     *
     * @param incomplete the incomplete code example to patch.
     * @param withName the name of the complete code
     * @param withContent the content to be prepended
     *
     * @return the patched code example.
     */
    public static Source wrap(Source incomplete, String withName, String withContent){

        final String content = withContent + incomplete.getContents() + END;

        final Source revised = from(incomplete, content);
        if(!revised.getName().equals(withName)){
            revised.setName(withName + ".java");
        }

        return revised;
    }


    /**
     * Crops a complete code example (e.g., class A {...}) by removing its class definition header
     * (class ..) and its ending curly brace (e.g., `}`).
     *
     * This method abstracts out the parts of the code example, which for bookkeeping, produced by
     * {@link Source#wrap} when trying to transform incomplete code examples.
     *
     * This method should be used only for cases when trying to refactor an incomplete code
     * example. Having written that, Violette should send information on whether this method
     * can be invoked or not.
     *
     * @param a the Source to be cropped.
     * @param addon class definition header string
     * @return the cropped Source.
     */
    public static Source unwrap(Source a, String addon){

        final String currentContent = a.getContents();
        final String updatedContent = StringUtil.trim(
                StringUtil.removeEnd(
                        StringUtil.removeStart(currentContent, addon),
                        END
                )
        );

        final Source cropped = Source.from(a, updatedContent);
        cropped.setName("Scratched.java");
        return cropped;
    }

    /**
     * Crops a complete code example (e.g., class A {...}) by removing its class definition header
     * (class .. {) and its ending curly brace (e.g., `}`).
     *
     * This method abstracts out the parts of the code example, which for bookkeeping, produced by
     * {@link Source#wrap} when trying to transform incomplete code examples.
     *
     * This method should be used only for cases when trying to refactor an incomplete code
     * example. Having written that, Violette should send information on whether this method
     * can be invoked or not.
     *
     * @param toAdjust the Source to be cropped.
     * @return the cropped Source.
     */
    public static Source unwrap(Source toAdjust){
        final String header = Source.currentHeader(toAdjust, StringUtil
                .extractFileName(toAdjust.getName()));

        return unwrap(toAdjust, header);
    }

    public static String missingHeader(Introspector introspector, Source a, String name){
        return header(introspector, a, name, false);
    }

    public static String currentHeader(Source a, String name){
        return header(null, a, name, true);
    }

    static String header(Introspector introspector, Source a, String name, boolean parsed){

        if(parsed){
            final Context context = CodeIntrospector.makeContext(a);

            final String withName = StringUtil.extractFileName(a.getName());
            final List<String> withImports = CodeIntrospector.findImports(
                    CodeIntrospector.findImports(context)
            );

            return StringUtil.concat(withName, false, withImports);
        } else {
            final List<String> directives = Lists.newLinkedList(introspector.detectMissingImports(a));
            return StringUtil.concat(name, true, directives);
        }
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


    @Override public boolean equals(Object o) {
        if(!(o instanceof Source)){
            return false;
        }

        final Source that      = (Source)o;
        final boolean sameName = that.getName().equals(getName());
        final boolean sameCont = that.getContents().equals(getContents());

        return sameName && sameCont;
    }

    private static String generateDescription(String fromName){
        return "Java: " + StringUtil.splitCamelCase(StringUtil.extractFileName(fromName));
    }

    /**
     * Generate a random and unique identifier.
     *
     * @return The random and unique signature
     */
    public String generateUniqueSignature(){
        final String uuid = UniqueIdentifierGenerator.generateUniqueIdentifier();
        setSignature(uuid);
        return uuid;
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
    public String getName()     { return name.get(); }

    /**
     * Gets the content's length.
     *
     * @return The content's length.
     */
    public int getLength()      { return getContents().length(); }

    /**
     * @return A unique identifier (will never change across multiple versions of this source).
     */
    public String getUniqueSignature(){ return this.signature.get(); }

    /**
     * @return The Source version.
     */
    public String getVersion(){
        return version.get();
    }


    @Override public int hashCode() {
        return Objects.hashCode(getName(), getContents());
    }

    /**
     * Removes a note from symbol table.
     *
     * @return {@code true} if the note was removed.
     */
    public boolean removeNote(Note note){
        return this.notes.delete(note);
    }

    /**
     * Sets the name of the {@code Source}, which is different than the one
     * initially given.
     *
     * @param name The {@code Source}'s name.
     */
    public void setName(String name){
        this.name.compareAndSet(getName(), name);
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

    /**
     * Sets the local (and unique) signature of the {@code Source}. A non-null signature means this
     * {@code Source} has been persisted.
     *
     * @param signature The {@code Source}'s unique signature.
     * @return {@code true} if the signature was set.
     */
    public boolean setSignature(String signature){
        return this.signature.compareAndSet(this.signature.get(), signature);
    }

    /**
     * Sets a version in text form.
     *
     * @param version version string
     * @return {@code true} if version was set, {@code false} otherwise.
     */
    public boolean setVersion(String version){
        return this.version.compareAndSet(this.version.get(), version);
    }

    @Override public String toString() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        if(getId() != null){
            builder.add("id", getId());
        }

        builder.add("name", StringUtil.extractFileName(getName()));
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
