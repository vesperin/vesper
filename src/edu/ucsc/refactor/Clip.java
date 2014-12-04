package edu.ucsc.refactor;

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Clip {
    private final Source        content;
    private final AtomicInteger support;
    private final String        label;

    /**
     * Represents a method and its dependencies, followed by a label and its
     * support value. The latter parameter will be used for ranking purposes.
     *
     * @param label The name of core method
     * @param content The code example.
     */
    Clip(String label, Source content){
        this.label      = Preconditions.checkNotNull(label);
        this.content    = Preconditions.checkNotNull(content);
        this.support    = new AtomicInteger(1);
    }

    /**
     * Factory method that creates a clip object.
     *
     * @param label The name of the core method.
     * @param content The code example.
     * @return a new Clip object.
     */
    public static Clip makeClip(String label, Source content){
        return new Clip(label, content);
    }

    /**
     * @return the label of the clip object.
     */
    public String getLabel(){
        return this.label;
    }

    /**
     * @return the code example.
     */
    public Source getSource() {
        return this.content;
    }

    /**
     * @return the support amount.
     */
    public int getSupport() {
        return this.support.get();
    }

    /**
     * increment the support to this clip by some value.
     *
     * @param val the value to be added.
     */
    public void increment(int val){
        this.support.set(this.support.get() + val);
    }

    /**
     * decrement the support to this clip by some value.
     *
     * @param val the value to be subtracted.
     */
    public void decrement(int val){
        this.support.set(this.support.get() - val);
    }
}
