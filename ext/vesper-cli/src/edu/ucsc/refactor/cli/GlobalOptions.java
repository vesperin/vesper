package edu.ucsc.refactor.cli;

import com.google.common.base.Objects;
import io.airlift.airline.Option;

import static io.airlift.airline.OptionType.GLOBAL;

/**
 * {@code Vesper CLI}'s global options
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class GlobalOptions {
    @Option(type = GLOBAL, name = {"-v", "--verbose"}, description = "Verbose mode")
    public boolean verbose = false;

    @Override public String toString() {
        return Objects.toStringHelper("GlobalOptions")
                .add("verbose", verbose)
                .toString();
    }
}
