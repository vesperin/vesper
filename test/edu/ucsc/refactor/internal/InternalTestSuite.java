package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.internal.changers.ChangersTest;
import edu.ucsc.refactor.internal.detectors.DetectorsTest;
import edu.ucsc.refactor.internal.util.ASTUtilTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DetectorsTest.class,
        ChangersTest.class,
        ASTUtilTest.class
})
public class InternalTestSuite {
    public static Test suite() {
        return new TestSuite(InternalTestSuite.class.getName());
    }
}

