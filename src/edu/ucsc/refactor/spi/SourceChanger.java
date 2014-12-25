package edu.ucsc.refactor.spi;

import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.LocalCommitRequest;
import edu.ucsc.refactor.internal.SourceLocation;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.util.SourceFormatter;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
        LOGGER.fine("Commit request created: " + request);
        return request;
    }

    @Override public Change createChange(Cause cause, Map<String, Parameter> parameters){
        if (!canHandle(cause) ) {
            throw new IllegalArgumentException(
                    "Cannot handle cause. This changer does not know " +
                            "how to handle this kind of cause."
            );
        }

        if (null == parameters) {
            parameters = new HashMap<String, Parameter>();
        }

        merge(defaultParameters(), parameters);

        return initChanger(cause, parameters);
    }


    @Override public CommitRequest commitChange(Change change) {
        // Always commit changes locally...once we are ready to save changes, we perform
        // a remote commit...
        return new LocalCommitRequest(change);
    }

    /**
     * Sets up the solver and returns its solution.
     *
     * @param cause The cause to be handled.
     * @param parameters Supporting data for the solver.
     * @return a new {@link Change solution}.
     */
    protected abstract Change initChanger(Cause cause, Map<String, Parameter> parameters);

    /**
     * Tracks the changes made to a {@link Source file} by storing them
     * in the {@link Delta} object.
     *
     * @param source The Source object.
     * @param rewrite The ASTRewrite object.
     * @return a new {@link Delta} object.
     */
    protected Delta createDelta(Source source, ASTRewrite rewrite){
        return createDelta(source, rewrite, false);
    }


    protected Delta createDelta(Source source, ASTRewrite rewrite, boolean goformat){
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

        delta.setAfter(StringUtil.trim((goformat ? format(document) : document.get())));

        return delta;
    }

    /**
     * Tracks the changes made to a {@link Source file} by storing them
     * in the {@link Delta} object.
     *
     * @param node The ASTNode object.
     * @param rewrite The ASTRewrite object.
     * @return a new {@link Delta} object.
     */
    protected Delta createDelta(ASTNode node, ASTRewrite rewrite) {
        return createDelta(Source.from(node), rewrite);
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

    @Override public List<Location> locate(Cause cause) {
        final List<Location> locations = new ArrayList<Location>();

        for(ASTNode each : cause.getAffectedNodes()){
            locations.add(locate(each));
        }

        return locations;
    }

    protected static CompilationUnit getCompilationUnit(Cause cause){
        return AstUtil.parent(CompilationUnit.class, cause.getAffectedNodes().get(0));
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
