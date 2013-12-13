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

    @Test public void testSplitCamelCase() {
        assertEquals("lowercase", StringUtil.splitCamelCase("lowercase"));
        assertEquals("Class", StringUtil.splitCamelCase("Class"));
        assertEquals("My Class", StringUtil.splitCamelCase("MyClass"));
        assertEquals("HTML", StringUtil.splitCamelCase("HTML"));
        assertEquals("PDF Loader", StringUtil.splitCamelCase("PDFLoader"));
        assertEquals("A String", StringUtil.splitCamelCase("AString"));
        assertEquals("Simple XML Parser", StringUtil.splitCamelCase("SimpleXMLParser"));
        assertEquals("GL 11 Version", StringUtil.splitCamelCase("GL11Version"));
    }
}
