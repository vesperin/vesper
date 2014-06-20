package edu.ucsc.refactor.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        CommitHistoryTest.class
})
public class CommitHistorySuite {
    public static Test suite() {
        return new TestSuite(CommitHistorySuite.class.getName());
    }
}
