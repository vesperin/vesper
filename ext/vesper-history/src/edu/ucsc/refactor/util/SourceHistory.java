package edu.ucsc.refactor.util;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.Source;

import java.util.Iterator;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceHistory implements Iterable<Source> {
    private final Set<Source> sources;

    /**
     * Creates a new and empty SourceHistory.
     */
    public SourceHistory(){
        this(Sets.<Source>newLinkedHashSet()/*in order*/);
    }

    /**
     * Creates a new SourceHistory from set of sources.
     * @param sources The sources to be included in history.
     */
    SourceHistory(Set<Source> sources){
        this.sources = sources;
    }

    /**
     * Adds a new Source to the history.
     * @param source The source to be added.
     */
    public void add(Source source){
       if(!sources.contains(source)){
           sources.add(source);
       }
    }

    /**
     * Clears the history.
     */
    public void clear(){
        sources.clear();
    }

    /**
     * Removes an existing Source from the history.
     * @param source The source to be deleted.
     */
    public void delete(Source source){
        if(sources.contains(source)){
            sources.remove(source);
        }
    }

    /**
     * Returns whether the history is empty or not.
     */
    public boolean isEmpty(){
        return sources.isEmpty();
    }

    @Override public Iterator<Source> iterator() {
        return sources.iterator();
    }

    public int size(){
        return sources.size();
    }

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("data", sources)
                .toString();
    }

}
