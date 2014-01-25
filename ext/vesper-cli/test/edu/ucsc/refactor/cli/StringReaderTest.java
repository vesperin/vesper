package edu.ucsc.refactor.cli;

import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class StringReaderTest {

    @Test public void testSourceReadingSplits() throws Exception {
        final StringReader reader = new StringReader();
        final Iterable<String> tokens = reader.process("whereis param a");
        assertThat(Iterables.size(tokens), is(3));

        final Iterable<String> tokens2 = reader.process("rm param [1,2]");
        assertThat(Iterables.size(tokens2), is(3));

        final Iterable<String> tokens3 = reader.process("optimize imports");
        assertThat(Iterables.size(tokens3), is(2));

        final Iterable<String> tokens4 = reader.process("optimize (1,2)");
        assertThat(Iterables.size(tokens4), is(4));
    }
}
