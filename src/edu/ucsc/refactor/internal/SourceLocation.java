package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Position;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.util.ToStringBuilder;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class SourceLocation implements Location {

    private final Source code;
    private final Position start;
    private final Position  end;

    /**
     * (Private constructor, use one of the factory methods
     * {@link SourceLocation#createLocation(Source)},
     * {@link SourceLocation#createLocation(Source, Position, Position)}, or
     * {@link SourceLocation#createLocation(Source, String, int, int)}.
     * <p>
     * Constructs a new location range for the given file, from start to end. If
     * the length of the range is not known, end may be null.
     *
     * @param code the associated code snippet (but see the documentation for
     *            {@link #getSource()} for more information on what the code snippet
     *            represents)
     * @param start the starting position, or null
     * @param end the ending position, or null
     */
    protected SourceLocation(Source code, Position start, Position end){
        super();
        this.code   = code;
        this.start  = start;
        this.end    = end;
    }


    /**
     * Creates a new location for the given file and starting and ending
     * positions.
     *
     * @param code the {@link Source} object containing the positions
     * @param start the starting position
     * @param end the ending position
     * @return a new location
     */
    public static SourceLocation createLocation(
            Source code,
            Position start,
            Position end) {
        return new SourceLocation(code, start, end);
    }


    /**
     * Creates a new location for the given file, with the given contents, for
     * the given offset range.
     *
     * @param code the {@link Source} object containing the location
     * @param contents the current contents of the file
     * @param startOffset the starting offset
     * @param endOffset the ending offset
     * @return a new location
     */
    public static SourceLocation createLocation(
            Source code,
            String contents,
            int startOffset,
            int endOffset) {

        if (startOffset < 0 || endOffset < startOffset) {
            throw new IllegalArgumentException("Invalid offsets");
        }

        if (contents == null) {
            return new SourceLocation(code,
                    new SourcePosition(-1, -1, startOffset),
                    new SourcePosition(-1, -1, endOffset)
            );
        }

        int size    = contents.length();
        endOffset   = Math.min(endOffset, size);
        startOffset = Math.min(startOffset, endOffset);

        Position start = null;

        int line        = 0;
        int lineOffset  = 0;
        char prev       = 0;

        for (int offset = 0; offset <= size; offset++) {
            if (offset == startOffset) {
                start = new SourcePosition(line, offset - lineOffset, offset);
            }

            if (offset == endOffset) {
                Position end = new SourcePosition(line, offset - lineOffset, offset);
                return new SourceLocation(code, start, end);
            }

            char c = contents.charAt(offset);

            if (c == '\n') {
                lineOffset = offset + 1;
                if (prev != '\r') {
                    line++;
                }
            } else if (c == '\r') {
                line++;
                lineOffset = offset + 1;
            }

            prev = c;
        }

        return createLocation(code);
    }


    /**
     * Creates a new location for the given file
     *
     * @param code the the {@link Source} object to create a location for
     * @return a new location
     */
    public static SourceLocation createLocation(Source code) {
        return new SourceLocation(
                code,
                null /*start*/,
                null /*end*/
        );
    }


    @Override public int compareTo(Location location) {
        final int startLine         = getStart().getLine();
        final int endLine           = getEnd().getLine();
        final int otherStartLine    = location.getStart().getLine();
        final int otherEndLine      = location.getEnd().getLine();

        final int lineDiff = startLine - otherStartLine;

        if (lineDiff != 0) {
            return lineDiff;
        }

        return endLine - otherEndLine;
    }


    @Override public Source getSource()  { return code;  }

    @Override public Position getStart() { return start; }

    @Override public Position getEnd()   { return end;   }

    @Override public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(getSource().getName());
        builder.add("start", getStart());
        builder.add("end", getEnd());
        return builder.toString();
    }
}
