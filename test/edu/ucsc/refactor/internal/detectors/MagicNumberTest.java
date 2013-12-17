package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.spi.JavaParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MagicNumberTest {
    static final String A = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(String msg){ if(msg.length() > 1) {}}\n"
            + "}";

    static final String B = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "}";

    static final Source WITH_MAGIC_NUMBER       = new Source("Name.java", A);
    static final Source WITH_OUT_MAGIC_NUMBER   = new Source("Name.java", B);

    private JavaParser parser;

    @Before public void setUp() throws Exception {
        parser  = new EclipseJavaParser();
    }

    @Test public void testDetectMagicNumber(){

        final Context context = new Context(WITH_MAGIC_NUMBER);
        parser.parseJava(context);

        final MagicNumber   magicNumber = new MagicNumber();
        final Set<Issue>    issues      = magicNumber.detectIssues(context);
        assertThat(issues.size(), is(1));
    }

    @Test public void testUnableToDetectMagicNumber(){
        final Context context = new Context(WITH_OUT_MAGIC_NUMBER);
        parser.parseJava(context);

        final MagicNumber   magicNumber = new MagicNumber();
        final Set<Issue>    issues      = magicNumber.detectIssues(context);
        assertThat(issues.size(), is(0));

    }

    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
