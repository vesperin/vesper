package edu.ucsc.refactor.internal.changers;

import com.google.common.collect.Maps;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.*;
import edu.ucsc.refactor.internal.detectors.UnusedTypes;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ChangersTest {

    private JavaParser parser;

    @Before public void setUp() throws Exception {
        parser  = new EclipseJavaParser();
    }

    @Test public void testChangerForUnusedTypeDeclaration() throws Exception {

        final Context context = new Context(
                InternalUtil.createSourceWithOneUnusedStaticNestedClass()
        );

        parser.parseJava(context);

        final UnusedTypes unusedClass = new UnusedTypes();
        final Set<Issue> issues      = unusedClass.detectIssues(context);

        assertThat(issues.size(), is(1));

        final RemoveUnusedTypes remove = new RemoveUnusedTypes();
        for(Issue each : issues){
            Change change = remove.createChange(each, Maps.<String, Parameter>newHashMap());
            assertNotNull(change);
        }

    }


    @Test public void testChangerForUsedTypeDeclaration() throws Exception {

        final Context context = new Context(
                InternalUtil.createSourceWithOneUsedStaticNestedClass()
        );

        parser.parseJava(context);


        final ProgramUnitLocator locator    = new ProgramUnitLocator(context);
        final List<Location>     locations  = locator.locate(new ClassUnit("B"));

        assertThat(locations.isEmpty(), is(false));

        for(Location each : locations){
            ProgramUnitLocation     pul         = (ProgramUnitLocation) each;
            final TypeDeclaration   declaration = (TypeDeclaration) pul.getNode();

            final Location          loc         = Locations.locate(declaration);
            final RemoveUnusedTypes remove      = new RemoveUnusedTypes();
            final SingleEdit        edit        = SingleEdit.deleteClass(new SourceSelection(loc));
            edit.addNode(declaration);
            final Change            change      = remove.createChange(edit, Maps.<String, Parameter>newHashMap());

            assertThat(change.isValid(), is(false));
        }
    }

    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
