package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.InternalUtil;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.util.Locations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SelectedASTNodeVisitorTest {

    private JavaParser parser;

    @Before public void setUp() throws Exception {
        parser  = new EclipseJavaParser();
    }

    @Test public void testSelectMethodNode(){
        final Source    code    = InternalUtil.createSourceWithMagicNumber();
        final Context   context = new Context(code);

        parser.parseJava(context);

        final Location boom = InternalUtil.locateWord(code, "boom");
        context.setScope(new SourceSelection(boom));

        final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(boom);

        context.accept(visitor);

        assertNotNull(visitor.getMatchedNode());
        assertThat(Locations.bothSame(boom, visitor.locate(visitor.getMatchedNode())), is(true));

    }

    @Test public void testSelectParameterNode(){
        final Source    code    = InternalUtil.createSourceWithMagicNumber();
        final Context   context = new Context(code);

        parser.parseJava(context);

        final Location msg = InternalUtil.locateWord(code, "msg");
        context.setScope(new SourceSelection(msg));

        final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(msg);

        context.accept(visitor);

        assertNotNull(visitor.getMatchedNode());
        assertThat(Locations.bothSame(msg, visitor.locate(visitor.getMatchedNode())), is(true));
    }


    @Test public void testSelectClassNode(){
        final Source    code    = InternalUtil.createSourceWithMagicNumber();
        final Context   context = new Context(code);

        parser.parseJava(context);

        final Location name = InternalUtil.locateWord(code, "Name");
        context.setScope(new SourceSelection(name));

        final SelectedASTNodeVisitor visitor = new SelectedASTNodeVisitor(name);

        context.accept(visitor);

        assertNotNull(visitor.getMatchedNode());
        assertThat(Locations.bothSame(name, visitor.locate(visitor.getMatchedNode())), is(true));
    }


    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
