package edu.ucsc.refactor.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ToStringBuilderTest {
    @Test public void testToStringBuilderBasic(){
        final ToStringBuilder builder = new ToStringBuilder("Test");
        builder.add("greet", "Hi!");
        final String output = "Test[greet=Hi!]";

        assertEquals(builder.toString(), output);

    }
}
