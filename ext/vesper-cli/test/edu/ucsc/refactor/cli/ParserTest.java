package edu.ucsc.refactor.cli;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ParserTest {
    @Test public void testParsingSingleCommands() throws Exception {
        final Parser parser = new Parser();

        assertNotNull(parser.parse("optimize"));
        assertNotNull(parser.parse("help"));
        assertNotNull(parser.parse("log"));
        assertNotNull(parser.parse("reset"));
        assertNotNull(parser.parse("inspect"));
        assertNotNull(parser.parse("ivp --simple-prompt"));
        assertNotNull(parser.parse("show"));
        assertNotNull(parser.parse("publish"));
    }

    @Test public void testParsingGroupCommands() throws Exception {
        final Parser parser = new Parser();

        assertNotNull(parser.parse("rm class [1,2]"));
        assertNotNull(parser.parse("rm method [1,2]"));
        assertNotNull(parser.parse("rm param [1,2]"));
        assertNotNull(parser.parse("rm field [1,2]"));
        assertNotNull(parser.parse("rm -f origin"));
    }
}
