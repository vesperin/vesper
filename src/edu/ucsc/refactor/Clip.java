package edu.ucsc.refactor;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Clip {
    private final Source        content;
    private final String        label;
    private final boolean       isBase;
    private final String        methodName;

    /**
     * Represents a method and its dependencies, followed by a label and its
     * support value. The latter parameter will be used for ranking purposes.
     *
     * @param methodName The name of method that is the root of this clip
     * @param label The name of core method
     * @param content The code example.
     * @param isBase Indicates this clip is the base clip.
     */
    Clip(String methodName, String label, Source content, boolean isBase){
        this.methodName = methodName;
        this.isBase     = isBase;
        this.label      = Preconditions.checkNotNull(label);
        this.content    = Preconditions.checkNotNull(content);
    }

    /**
     * Factory method that creates a clip object.
     *
     * @param methodName The name of method that is the root of this clip
     * @param label The name of the core method.
     * @param content The code example.
     * @return a new Clip object.
     */
    public static Clip makeClip(String methodName, String label, Source content){
        return makeClip(methodName, label, content, false);
    }

    /**
     * Factory method that creates a clip object.
     *
     * @param methodName The name of method that is the root of this clip
     * @param label The name of the core method.
     * @param content The code example.
     * @param isBase Indicates this clip is the base clip.
     * @return a new Clip object.
     */
    public static Clip makeClip(String methodName, String label, Source content, boolean isBase){
        return new Clip(methodName, label, content, isBase);
    }

    /**
     * Finds the clip matching a code key in a sorted clip space.
     * @param code the search key
     * @param clipSpace the list of clips, must be sorted in ascending order
     * @return the clip matching the code key; null if not present
     */
    public static Clip find(Source code, List<Clip> clipSpace){
       final int rank = rank(code, clipSpace);
       return rank == -1 ? null : clipSpace.get(rank);
    }

    /**
     * Searches for the code key in the sorted list `a`.
     * @param key the search key
     * @param a the list of clips, must be sorted in ascending order
     * @return index of key in list a if present; -1 if not present
     */
    private static int rank(Source key, List<Clip> a) {
        int lo = 0;
        int hi = a.size() - 1;
        while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            int mid = lo + (hi - lo) / 2;
            final int option  = key.getContents().length();
            final int option2 = a.get(mid).getSource().getContents().length();

            if      (option < option2) hi = mid - 1;
            else if (option > option2) lo = mid + 1;
            else {
                // this is true cause multi staged code examples go from
                // less code to mode code
                return mid;
            }
        }

        return -1;
    }



    /**
     * @return the label of the clip object.
     */
    public String getLabel(){
        return this.label;
    }

    /**
     * @return the name of the method, which is the root of the clip.
     */
    public String getMethodName(){
        return this.methodName;
    }

    /**
     * @return the code example.
     */
    public Source getSource() {
        return this.content;
    }

    /**
     * @return true if this is a base clip; false otherwise.
     */
    public boolean isBaseClip(){
        return this.isBase;
    }


    @Override  public String toString() {
        return "Clip[root=" + getMethodName() + ", label=" + getLabel() + "]";
    }
}
