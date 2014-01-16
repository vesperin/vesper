package edu.ucsc.refactor.util;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.Name;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Checkpoint implements Comparable <Checkpoint> {
    private final Name   name;
    private final Source before;
    private final Source after;

    /**
     * Constructs a new Checkpoint.
     *
     * @param name THe name of the source change.
     * @param before THe source code before the change
     * @param after  THe source code after the change
     */
    Checkpoint(Name name, Source before, Source after){
        this.name   = Preconditions.checkNotNull(name);
        this.before = Preconditions.checkNotNull(before);
        this.after  = Preconditions.checkNotNull(after);
    }

    /**
     * Creates a new checkpoint.
     *
     * @param name THe name of the source change.
     * @param before THe source code before the change
     * @param after  THe source code after the change
     * @return the new Checkpoint object
     */
    public static Checkpoint createCheckpoint(Name name, Source before, Source after){
        return new Checkpoint(name, before, after);
    }

    /**
     * @param that is a non-null Checkpoint.
     *
     * @throws NullPointerException if that is null.
     */
    @Override public int compareTo(Checkpoint that) {
        Preconditions.checkNotNull(that);

        final int EQUAL  = 0;

        //this optimization is usually worthwhile, and can
        //always be added
        if (this == that) return EQUAL;


        //objects, including type-safe enums, follow this form
        //note that null objects will throw an exception here
        int comparison = this.getSourceBeforeChange().getContents().compareTo(that.getSourceBeforeChange().getContents());
        if (comparison != EQUAL) return comparison;

        comparison = this.getSourceAfterChange().getContents().compareTo(that.getSourceAfterChange().getContents());
        if (comparison != EQUAL) return comparison;

        comparison = this.getNameOfChange().getKey().compareTo(that.getNameOfChange().getKey());
        if (comparison != EQUAL) return comparison;

        //all comparisons have yielded equality
        //verify that compareTo is consistent with equals (optional)
        assert this.equals(that) : "compareTo inconsistent with equals.";

        return EQUAL;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Checkpoint)) return false;

        final Checkpoint that = (Checkpoint)o;

        return this.getNameOfChange().isSame(that.getNameOfChange())
                && this.getSourceBeforeChange().equals(that.getSourceBeforeChange())
                && this.getSourceAfterChange().equals(that.getSourceAfterChange());

    }

    /**
     * @return The name of the code change.
     */
    public Name getNameOfChange(){
        return name;
    }

    /**
     * @return The Source before the code change.
     */
    public Source getSourceBeforeChange(){
        return before;
    }

    /**
     * @return The Source after the code change.
     */
    public Source getSourceAfterChange(){
        return after;
    }

    /**
     * @return The unique (and stable) Source code signature.
     */
    public String getUniqueSignature(){
        return getSourceBeforeChange().getUniqueSignature();
    }


    @Override public int hashCode() {
        return Objects.hashCode(
                getNameOfChange(),
                getSourceBeforeChange(),
                getSourceAfterChange()
        );
    }

    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("ChangeName", getNameOfChange())
                .add("Before", getSourceBeforeChange())
                .add("After", getSourceAfterChange())
                .toString();
    }
}
