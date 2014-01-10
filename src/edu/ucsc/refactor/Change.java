package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.spi.CommitRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class Change {
    private final List<Delta>               deltas;
    private final List<String>              errors;
    private final Map<String, Parameter>    parameters;

    /**
     * Construct a new {@link Change}.
     * @param parameters The parameters that were used in the creation of this change.
     */
    protected Change(Map<String, Parameter> parameters){
        this(
                parameters,
                new ArrayList<Delta>(),
                new ArrayList<String>()
        );
    }

    /**
     * Construct a new {@link Change}.
     *
     * @param parameters The parameters that were used in the creation of this change.
     * @param deltas The list of deltas.
     * @param errors The list of errors.
     */
    Change(Map<String, Parameter> parameters, List<Delta> deltas, List<String> errors){
        this.parameters = parameters;
        this.deltas     = deltas;
        this.errors     = errors;
    }

    /**
     * Adds a new {@code Delta}.
     *
     * @param delta The added delta.
     */
    public void addDelta(Delta delta){
        if(delta == null){
            throw new IllegalArgumentException("addDelta() was given a null delta.");
        }

        this.deltas.add(delta);
    }


    /**
     * Convenience method that instantiates a new {@link Delta} and associates it with this
     * solution.
     *
     * @param source The {@link Source} the {@link Delta} applies to.
     * @return The newly created {@link Delta}.
     */
    public Delta createDelta(Source source) {
        Delta delta = new Delta(source);
        addDelta(delta);
        return delta;
    }


    /**
     * Get the cause that trigger this change.
     *
     * @return The cause this change addresses.
     */
    public abstract CauseOfChange getCause();


    /**
     * Get the delta's that make up the solution.
     *
     * @return The solution's deltas.
     */
    public List<Delta> getDeltas() {
        return deltas;
    }

    /**
     * Gets the list of errors caught when creating this change.
     *
     * @return The list of errors.
     */
    public List<String> getErrors(){
        return this.errors;
    }

    /**
     * Returns the parameters that were used in the creation of this change.
     *
     * @return the parameters that were used in the creation of this change.
     */
    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    /**
     * @return The original source (i.e., the one before any changes were made).
     */
    public  Source getSource(){
        return Source.from(getCause().getAffectedNodes().get(0));
    }

    /**
     * Verifies that this change object is still valid and can be executed by calling
     * <code>perform</code>.
     *
     * @return {@code true} if this change is valid.
     */
    public boolean isValid(){
        return getErrors().isEmpty();
    }

    /**
     * Describes the current status of this {@code Change}.
     *
     * @return Human-readable description of {@code Change}'s status.
     */
    public abstract String more();

    /**
     * Performs this change by creating a commit request,
     * which will be committed to an external service.
     * @return The commit request.
     */
    public abstract CommitRequest perform();

    /**
     * Removes a {@code Delta}.
     *
     * @param delta The {@code Delta} to be removed.
     * @return {@code true} if {@code Delta} was removed. {@code false} otherwise.
     */
    public boolean removeDelta(Delta delta){
        return delta != null
                && this.deltas.contains(delta)
                && this.deltas.remove(delta);
    }

    @Override public String toString() {
        return more();
    }
}
