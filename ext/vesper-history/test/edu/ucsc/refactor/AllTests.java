package edu.ucsc.refactor;

import edu.ucsc.refactor.util.CommitHistorySuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CommitHistorySuite.class,
        NavigableRefactorerTest.class,
        NavigableVesperTest.class

})
public class AllTests {
    public static Test suite() {
        return new TestSuite(AllTests.class.getName());
    }
}
