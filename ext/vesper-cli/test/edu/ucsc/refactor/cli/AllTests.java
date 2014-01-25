package edu.ucsc.refactor.cli;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        StringReaderTest.class,
        ParserTest.class
})
public class AllTests {
    public static Test suite() {
        return new TestSuite(AllTests.class.getName());
    }
}
