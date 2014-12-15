package edu.ucsc.refactor;

import com.google.common.base.Preconditions;

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
