package edu.ucsc.refactor;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import difflib.DiffUtils;
import difflib.Patch;
import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.SourceHistory;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceRecalling {
    private final Repository    repository;
    private final String        sourceId;

    /**
     * Construct a new {@code SourceRecalling} object.
     *
     * @param repository The location from where the source history is retrieved.
     * @param sourceId   The id of a source.
     */
    public SourceRecalling(Repository repository, String sourceId){
        this.repository = repository;
        this.sourceId   = sourceId;
    }

    /**
     * Gets the difference between the before and after source.
     *
     * @param before The Source before some change (s)
     * @param after  The Source after some change (s)
     * @return The list of differences between the original {@code Source} and
     *      the updated {@code Source}.
     */
    public List<difflib.Delta> differences(Source before, Source after){
        Patch patch = DiffUtils.diff(
                contentToLines(before.getContents()),
                contentToLines(after.getContents())
        );

        return patch.getDeltas();
    }

    /**
     * Recall the history of some Source, which experienced many changes.
     *
     * @return The {@code SourceHistory}.
     * @throws java.lang.NullPointerException if the sourceId is null.
     */
    public SourceHistory recall(){
        return replay(repository, sourceId);
    }

    /**
     * Recall the history of some Source matching some identifier (i.e., id)
     *
     * @param fromRepository The location from where the source history of a
     *                       source will be retrieved.
     * @param sourceId The id of a source.
     * @return The {@code SourceHistory}.
     * @throws java.lang.NullPointerException if either the sourceId or the repository are null.
     */
    public SourceHistory replay(Repository fromRepository, String sourceId){
        return fromRepository.pull(sourceId);
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
