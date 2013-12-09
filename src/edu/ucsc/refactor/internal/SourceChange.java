package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.CommitRequest;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.spi.Changer;
import edu.ucsc.refactor.spi.SourceChanger;

import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceChange extends Change {

    private final CauseOfChange cause;
    private final SourceChanger changer;

    /**
     * Creates a {@code Change} with an editing strategy and additional parameters.
     *
     * @param cause The cause of this change
     * @param changer The source changer that created this {@code Change} or {@code Refactoring}.
     * @param parameters  The parameters that the changer needs to apply the {@code Refactoring}.
     */
    public SourceChange(CauseOfChange cause, Changer changer, Map<String, Parameter> parameters){
        super(parameters);
        this.cause   = cause;
        this.changer = (SourceChanger) changer;
    }

    @Override public CauseOfChange getCause() {
        return cause;
    }


    /**
     * Returns the {@link SourceChanger} that created this change.
     *
     * @return The creating {@link SourceChanger} of this change.
     */
    public SourceChanger getSourceChanger() {
        return changer;
    }

    @Override public String more() {
        final StringBuilder builder = new StringBuilder();
        final String name = getSource().getName();
        builder.append("Change for ").append(getCause().getName().getKey()).append(" ");

        if(!isValid()){
            builder.append("cannot be performed to ");
            builder.append("class ").append(name).append(".java ");
            builder.append("Change contains ").append(getErrors().size());
            builder.append(" errors.");
            return builder.toString();
        }

        builder.append("can be performed to ");
        builder.append("class ").append(name).append(".java ");
        return builder.toString();
    }

    @Override public CommitRequest perform() {
        final SourceChanger currentChanger = getSourceChanger();
        if(null == currentChanger){
            getErrors().add("No suitable changer available.");
            return null;
        }

        return currentChanger.applyChange(this);
    }
}
