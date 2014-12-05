package edu.ucsc.refactor;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import difflib.*;
import edu.ucsc.refactor.util.SourceFormatter;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Diff {
    private final Source original;
    private final Source revised;

    /**
     * Construct a new Diff object.
     *
     * @param original the original source
     * @param revised  the revised source
     */
    Diff(Source original, Source revised) {
        this.original = original;
        this.revised = revised;
    }

    /**
     * @return the list of code portions that have changed.
     */
    public List<Chunk> getChangesFromOriginal() {
        return getChunksByType(/*difflib.Delta*/Delta.TYPE.CHANGE);
    }

    /**
     * @return the list of code portions that have been inserted.
     */
    public List<Chunk> getInsertsFromOriginal() {
        return getChunksByType(/*difflib.Delta*/Delta.TYPE.INSERT);
    }

    /**
     * @return the list of code portions that have been deleted.
     */
    public List<Chunk> getDeletesFromOriginal() {
        return getChunksByType(/*difflib.Delta*/Delta.TYPE.DELETE);
    }

    /**
     * Gets the list of code chunks matching the delta type.
     *
     * @param type the type of chunk to be extracted: insertion, deletion, or change.
     * @return the list of code chunks.
     */
    private List<Chunk> getChunksByType(Delta.TYPE type) {
        final List<Chunk> listOfChanges = Lists.newArrayList();
        final List<Delta> deltas = getDeltas();

        for (Delta each : deltas) {
            if (each.getType() == type) {
                listOfChanges.add(each.getRevised());
            }
        }

        return listOfChanges;
    }

    private Patch getPatch() {
        return DiffUtils.diff(
                contentToLines(original.getContents()),
                contentToLines(revised.getContents())
        );
    }

    /**
     * @return the list of deltas (differences) between the original and revised sources.
     */
    private List<Delta> getDeltas() {
        final Patch patch = getPatch();
        return patch.getDeltas();
    }

    /**
     * resolve differences and return null if unable to resolve these differences.
     */
    public Source resolve() {
        final Patch patch = getPatch();
        try {
            final List<?> applied = patch.applyTo(contentToLines(original.getContents()));
            if (!applied.equals(contentToLines(revised.getContents()))) return null;

            final StringBuilder stringList = new StringBuilder();
            for (int i = 0; i < applied.size(); i++) {
                String s = (String) applied.get(i);
                if (i != applied.size() - 1)
                    stringList.append(s).append("\n");
                else
                    stringList.append(s);
            }

            return Source.from(revised, new SourceFormatter().format(stringList.toString()));

        } catch (PatchFailedException e) {
            throw new RuntimeException("Unable to resolved differences between the sources!");
        }
    }


    /**
     * Splits the content of a file into separate lines.
     *
     * @param content The content to split.
     * @return a List of all lines in the content string.
     */
    private static List<String> contentToLines(String content) {
        return Lists.newLinkedList(
                Splitter.on(
                        System.getProperty("line.separator")
                ).split(content)
        );
    }


}
