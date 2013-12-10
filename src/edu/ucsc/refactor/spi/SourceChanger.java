package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.*;
import edu.ucsc.refactor.Location;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class SourceChanger implements Changer {
    private static final Logger LOGGER  = Logger.getLogger(SourceChanger.class.getName());

    /**
     * Construct a new {@link SourceChanger}.
     */
    protected SourceChanger(){}

    /**
     * Applies a {@link Change}.
     *
     * @param change The {@link Change} object.
     */
    public CommitRequest applyChange(Change change){
        LOGGER.fine("Applying change " + change);
        final CommitRequest request = commitChange(change);
        LOGGER.fine("Commit request created: " + request.more());
        return request;
    }

    @Override public Change createChange(CauseOfChange cause, Map<String, Parameter> parameters){
        if (!canHandle(cause) ) {
            throw new IllegalArgumentException(
                    "Cannot handle cause. This changer does not know " +
                            "how to handle this kind of cause."
            );
        }

        if (null == parameters) {
            parameters = new HashMap<String, Parameter>();
        }

        merge(parameters, defaultParameters());

        return initChanger(cause, parameters);
    }


    @Override public CommitRequest commitChange(Change change) {
        return new GistCommitRequest(change);
    }

    /**
     * Sets up the solver and returns its solution.
     *
     * @param cause The cause to be handled.
     * @param parameters Supporting data for the solver.
     * @return a new {@link Change solution}.
     */
    protected abstract Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters);


    /**
     * Tracks the changes made to a {@link Source file} by storing them
     * in the {@link edu.ucsc.refactor.internal.Delta} object.
     *
     * @param node The ASTNode object.
     * @param rewrite The ASTRewrite object.
     * @return a new {@link edu.ucsc.refactor.internal.Delta} object.
     */
    protected Delta createDelta(ASTNode node, ASTRewrite rewrite) {
        final Source    source      = Source.from(node);
        final IDocument document    = source.toDocument();

        Delta delta = new Delta(source);
        delta.setBefore(document.get());

        TextEdit textEdit = rewrite.rewriteAST(document, JavaCore.getOptions());
        try {
            textEdit.apply(document);
        } catch (MalformedTreeException e) {
            LOGGER.throwing("Could not rewrite the AST tree.", "createDelta", e);
        } catch (BadLocationException e) {
            LOGGER.throwing("Could not rewrite the AST tree.", "createDelta", e);
        }

        delta.setAfter(format(document));
        return delta;
    }


    public static String format(IDocument document){
        return new SourceFormatter().format(document);
    }


    /**
     * Default supporting data.
     * @return The default configuration for solver's supporting data.
     */
    protected Map<String, Parameter> defaultParameters() {
        return new HashMap<String, Parameter>();
    }

    @Override public List<Location> locate(CauseOfChange cause) {
        final List<Location> locations = new ArrayList<Location>();

        for(ASTNode each : cause.getAffectedNodes()){
            locations.add(locate(each));
        }

        return locations;
    }


    /**
     * Merges two maps.
     *
     * @param current The default map
     * @param other   The other map to be merged into the default map.
     */
    public static void merge(Map<String, Parameter> current, Map<String, Parameter> other) {
        for (Map.Entry<String, Parameter> entry : current.entrySet()) {
            if (!other.containsKey(entry.getKey())) {
                other.put(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * Locates a ASTNode in the {@code Source}.
     *
     * @param node The ASTNode to be located.
     * @return A {@code Location} in the {@code Source} where a ASTNode is found.
     */
    protected Location locate(ASTNode node) {
        final Source src = Source.from(node);
        return SourceLocation.createLocation(
                src,
                src.getContents(),
                node.getStartPosition(),
                node.getStartPosition() + node.getLength()
        );
    }



}
