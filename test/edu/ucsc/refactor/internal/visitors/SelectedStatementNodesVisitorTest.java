package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.*;
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

        final Location invalid = SourceLocation.createLocation(name.getSource(), name.getSource().getContents(), 6, 89);

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

        final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(name, true);
        context.accept(statements);
        statements.checkIfSelectionCoversValidStatements();

        assertThat(statements.isSelectionCoveringValidStatements(), is(true));
    }


    @Test public void testValidWholeMethodSelection(){
        final Source code    = InternalUtil.createSourceWithMagicNumber();
        final Context context = new Context(code);

        parser.parseJava(context);


        final ProgramUnitLocator locator = new ProgramUnitLocator(context);
        final NamedLocation namedLocation = locator.locate(new MethodUnit("boom")).get(0);

        final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(namedLocation, true);
        context.accept(statements);
        statements.checkIfSelectionCoversValidStatements();

        assertThat(statements.isSelectionCoveringValidStatements(), is(true));

    }


    @Test public void testInvalidWholeMethodSelection(){
        final Source code    = InternalUtil.createSourceWithMagicNumber();
        final Context context = new Context(code);

        parser.parseJava(context);

        final ProgramUnitLocator locator = new ProgramUnitLocator(context);
        final NamedLocation boomLocation = locator.locate(new MethodUnit("boom")).get(0);


        final Location invalid = SourceLocation.createLocation(
                boomLocation.getSource(),
                boomLocation.getSource().getContents(),
                6, /*starts at N*/
                boomLocation.getEnd().getOffset()
        );

        final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(invalid, true);
        context.accept(statements);
        statements.checkIfSelectionCoversValidStatements();

        assertThat(statements.isSelectionCoveringValidStatements(), is(false));

    }


    @Test public void testExpansiveSelection(){
        final Source code    = InternalUtil.createGeneralSource();
        final Context context = new Context(code);

        parser.parseJava(context);


        final Location userSelection = SourceLocation.createLocation(code, code.getContents(), 88, 238);

        final SelectedStatementNodesVisitor statements = new SelectedStatementNodesVisitor(userSelection, true);
        context.accept(statements);
        statements.checkIfSelectionCoversValidStatements();

        assertThat(statements.getSelectedNodes().isEmpty(), is(false));
        assertThat(statements.isSelectionCoveringValidStatements(), is(true));
    }


    @After public void tearDown() throws Exception {
        parser  = null;
    }
}
