package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Issue;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.InternalUtil;
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
public class DetectorsTest {
    private JavaParser parser;

    @Before public void setUp() throws Exception {
        parser  = new EclipseJavaParser();
    }

    @Test public void testDetectMagicNumber(){

        final Context context = new Context(InternalUtil.createSourceWithMagicNumber());
        parser.parseJava(context);

        final MagicNumber   magicNumber = new MagicNumber();
        final Set<Issue> issues      = magicNumber.detectIssues(context);
        assertThat(issues.size(), is(1));
    }

    @Test public void testUnableToDetectMagicNumber(){
        final Context context = new Context(InternalUtil.createSourceNoIssues());
        parser.parseJava(context);

        final MagicNumber   magicNumber = new MagicNumber();
        final Set<Issue>    issues      = magicNumber.detectIssues(context);
        assertThat(issues.size(), is(0));

    }


    @Test
    public void testDetectUnusedImportDirective(){

        final Context context = new Context(
                InternalUtil.createSourceWithOneUnusedImportDirective()
        );

        parser.parseJava(context);

        final UnusedImports unusedImports = new UnusedImports();
        final Set<Issue>    issues        = unusedImports.detectIssues(context);

        assertThat(issues.size(), is(1));
    }


    @Test public void testUnableToDetectUnusedImport(){
        final Context context = new Context(InternalUtil.createSourceNoIssues());
        parser.parseJava(context);

        final UnusedImports unusedImports = new UnusedImports();
        final Set<Issue>    issues        = unusedImports.detectIssues(context);

        assertThat(issues.size(), is(0));
    }


    @Test public void testDetectUnusedField(){
        final Context context = new Context(
                InternalUtil.createSourceWithUnusedField()
        );

        parser.parseJava(context);

        final UnusedFields unusedFields = new UnusedFields();
        final Set<Issue>    issues      = unusedFields.detectIssues(context);

        assertThat(issues.size(), is(1));
    }


    @Test public void testDetectUsedField(){
        final Context context = new Context(
                InternalUtil.createSourceWithUsedField()
        );

        parser.parseJava(context);

        final UnusedFields unusedFields = new UnusedFields();
        final Set<Issue>    issues      = unusedFields.detectIssues(context);

        assertThat(issues.size(), is(0));
    }


    @Test public void testDetectUnusedMethod(){
        final Context context = new Context(
                InternalUtil.createSourceWithUnusedMethodAndParameter()
        );

        parser.parseJava(context);

        final UnusedMethods unusedMethods = new UnusedMethods();
        final Set<Issue>    issues        = unusedMethods.detectIssues(context);

        assertThat(issues.size(), is(1));
    }

    @Test public void testDetectUnusedMethodParameter(){
        final Context context = new Context(
                InternalUtil.createSourceWithUnusedMethodAndParameter()
        );

        parser.parseJava(context);

        final UnusedParameters unusedParameters = new UnusedParameters();
        final Set<Issue>       issues           = unusedParameters.detectIssues(context);

        assertThat(issues.size(), is(1));
    }

    @Test public void testUnableToDetectUnusedMethod(){
        final Context context = new Context(
                InternalUtil.createSourceNoIssues()
        );

        parser.parseJava(context);

        final UnusedMethods unusedMethods = new UnusedMethods();
        final Set<Issue>    issues        = unusedMethods.detectIssues(context);

        assertThat(issues.size(), is(0));
    }

    @Test public void testUnusedClassDetectorOnClassWithUsedNestedClass(){
        final Context context = new Context(
                InternalUtil.createSourceWithOneUsedStaticNestedClass()
        );

        parser.parseJava(context);

        final UnusedTypes unusedClass = new UnusedTypes();
        final Set<Issue>  issues      = unusedClass.detectIssues(context);

        assertThat(issues.size(), is(0));
    }


    @Test public void testUnusedClassDetectorOnClassWithOneUnusedNestedClass(){
        final Context context = new Context(
                InternalUtil.createSourceWithOneUnusedStaticNestedClass()
        );

        parser.parseJava(context);

        final UnusedTypes unusedClass = new UnusedTypes();
        final Set<Issue>  issues      = unusedClass.detectIssues(context);

        assertThat(issues.size(), is(1));
    }

    @Test public void testUnableToDetectUnusedMethodParameter(){
        final Context context = new Context(
                InternalUtil.createSourceNoIssues()
        );

        parser.parseJava(context);

        final UnusedParameters unusedParameters = new UnusedParameters();
        final Set<Issue>       issues           = unusedParameters.detectIssues(context);

        assertThat(issues.size(), is(0));
    }

    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
