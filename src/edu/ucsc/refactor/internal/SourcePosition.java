package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import edu.ucsc.refactor.Position;

import java.util.Arrays;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class SourcePosition implements Position {
    /**
     * The line number (0-based where the first line is line 0)
     */
    private final int line;

    /**
     * The column number (where the first character on the line is 0), or -1 if
     * unknown
     */
    private final int column;

    /**
     * The character offset
     */
    private final int offset;

    /**
     * Creates a new {@link SourcePosition}
     *
     * @param line   the 0-based line number, or -1 if unknown
     * @param column the 0-based column number, or -1 if unknown
     * @param offset the offset, or -1 if unknown
     */
    public SourcePosition(int line, int column, int offset) {
        this.line   = line;
        this.column = column;
        this.offset = offset;
    }


    @Override public int hashCode() {
        final int[] values = {getLine(), getColumn(), getOffset()};
        return Arrays.hashCode(values);
    }

    @Override public boolean equals(Object o) {
        if(!(o instanceof Position)) return false;

        final Position other = (Position)o;
        final boolean sameLines = getLine()   == other.getLine();
        final boolean sameCols  = getColumn() == other.getColumn();
        final boolean sameOffs  = getOffset() == other.getOffset();

        return sameLines && sameCols && sameOffs;
    }

    @Override public int getLine()   { return line;   }

    @Override public int getOffset() { return offset; }

    @Override public int getColumn() { return column; }

    @Override public String toString() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        builder.add("offset", getOffset());
        builder.add("line", getLine() + 1);
        builder.add("column", getColumn() + 1);
        return builder.toString();
    }
}
