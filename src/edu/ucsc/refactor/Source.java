package edu.ucsc.refactor;

import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Source {
    public static final String SOURCE_FILE_PROPERTY = "kae.source_file.source_file_property";

    private final String name;
    private final String contents;

    /**
     * construct a new {@link Source} object.
     * @param name The name of file
     * @param contents The file's contents.
     */
    public Source(String name, String contents){
        this.name     = StringUtil.extractName(name);
        this.contents = contents;
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
     * Gets the file's contents
     *
     * @return The file's contents
     */
    public String getContents() { return contents; }

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


    @Override public String toString() {
        return getName();
    }

    /**
     * Converts this {@code Source} into a jface's {@code Document}.
     * @return The {@link IDocument}
     */
    public IDocument toDocument() {
        return new Document(getContents());
    }
}
