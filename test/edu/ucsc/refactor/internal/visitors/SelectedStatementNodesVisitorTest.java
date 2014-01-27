package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.SourceSelection;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.InternalUtil;
import edu.ucsc.refactor.internal.SourceLocation;
import edu.ucsc.refactor.spi.JavaParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SelectedStatementNodesVisitorTest {
    private JavaParser parser;

    @Before public void setUp() throws Exception {
        parser  = new EclipseJavaParser();
    }

    @Test public void testInvalidStatementsSelection(){
        final Source    code    = InternalUtil.createSourceWithMagicNumber();
        final Context   context = new Context(code);

        parser.parseJava(context);

        final Location name = InternalUtil.locateWord(code, "Name");
        context.setScope(new SourceSelection(name));

        final Location invalid = SourceLocation.createLocation(name.getSource(), name.getSource().getContents(), 6, 12);

        final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(invalid, true);
        context.accept(statements);
        statements.checkIfSelectionCoversValidStatements();

        assertThat(statements.isSelectionCoveringValidStatements(), is(false));
    }

    @Test public void testValidStatementsSelection(){
        final Source code    = InternalUtil.createSourceWithMagicNumber();
        final Context context = new Context(code);

        parser.parseJava(context);

        final Location name = InternalUtil.locateWord(code, "Name");
        context.setScope(new SourceSelection(name));

        final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(name, true);
        context.accept(statements);
        statements.checkIfSelectionCoversValidStatements();

        assertThat(statements.isSelectionCoveringValidStatements(), is(true));
    }

    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
