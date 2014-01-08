package edu.ucsc.refactor.spi;

import org.eclipse.jface.text.IDocument;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public interface Formatter {
    /**
     * Formats a piece of source code.
     * @param code Source code
     * @return formatted piece of source code.
     */
    String format(String code);

    /**
     * Formats a {@link IDocument}'s content.
     * @param document a {@link IDocument}
     * @return formatted {@link IDocument}'s content.
     */
    String format(IDocument document);
}
