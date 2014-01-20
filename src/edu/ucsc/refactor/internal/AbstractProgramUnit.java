package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import edu.ucsc.refactor.spi.ProgramUnit;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class AbstractProgramUnit implements ProgramUnit {
    private final String    name;

    /**
     * Construct a new {@code AbstractProgramUnit}.
     *
     * @param name The name of unit
     */
    protected AbstractProgramUnit(String name){
        this.name   = Preconditions.checkNotNull(name);
    }

    @Override public String getName() {
        return name;
    }

    @Override public String toString() {
        return Objects.toStringHelper(getName())
                .toString();
    }
}
