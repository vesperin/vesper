package edu.ucsc.refactor.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class StringsTest {
    @Test public void testExtractJavaFileName(){
        final String nameWithExtension = "Name.java";
        assertEquals(StringUtil.extractName(nameWithExtension), "Name");
    }
}
