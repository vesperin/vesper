package edu.ucsc.refactor;

import edu.ucsc.refactor.util.Locations;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceSelectionTest {

    static final Source TEST_CLASS = new Source(
            "Test.java",
            "import java.util.List;\n class Test{\n public void test(int a){\n}\n}\n"
    );

    @Test public void testEmptySourceSelectionCreation(){
        try {
            final SourceSelection selection = new SourceSelection();
            assertNotNull(selection);
        } catch (Exception ex){
            fail("If it got here.");
        }
    }

    @Test public void testSingletonSourceSelectionCreation(){
        final SourceSelection singleton = new SourceSelection();
        final SourceSelection entry = new SourceSelection(TEST_CLASS, 37, 62);
        singleton.add(entry.toLocation());

        assertThat("Singleton is not empty", !singleton.isEmpty(), is(true));
        assertThat("Singleton has only one element", singleton.size(), is(1));
        assertThat("Singleton has a source", singleton.getSource(), equalTo(TEST_CLASS));


        final boolean sameBounds = (singleton.first().getStart() == entry.first().getStart()
                                        && singleton.last().getEnd() == entry.last().getEnd());

        assertThat("Singleton has same bounds", sameBounds, is(true));
    }

    @Test public void testSourceSelectionUnit(){
        final SourceSelection a = new SourceSelection(new SourceSelection(TEST_CLASS, 37, 62).toLocation());
        final SourceSelection b = new SourceSelection(new SourceSelection(TEST_CLASS, 0, 36).toLocation());

        final SourceSelection union = a.union(b);

        assertThat("united selection is not empty", !union.isEmpty(), is(true));
        assertThat("united selection has two elements", union.size(), is(2));
        assertThat("united selection has same source", union.getSource(), equalTo(TEST_CLASS));


        final int unionStartLine = union.first().getStart().getLine();
        final int unionEndLine   = union.last().getEnd().getLine();

        final int bStartLine     = b.first().getStart().getLine();
        final int aEndLine       = a.last().getEnd().getLine();

        final boolean combinedBounds = (unionStartLine == bStartLine
                                            && unionEndLine == aEndLine);

        assertThat("united selection has combined bounds", combinedBounds, is(true));
    }

    @Test public void testCeilingFloorSourceSelection(){
        final SourceSelection a = new SourceSelection(TEST_CLASS, 37, 62);
        final SourceSelection b = new SourceSelection(TEST_CLASS, 0, 36);
        final SourceSelection c = new SourceSelection(TEST_CLASS, 63, 70);

        final SourceSelection main = new SourceSelection(a.toLocation());
        main.add(b.toLocation());
        main.add(c.toLocation());


        assertThat(Locations.bothSame(main.ceiling(b.toLocation()), b.toLocation()), is(true));
        assertThat(Locations.bothSame(main.floor(b.toLocation()), b.toLocation()), is(true));
    }


    @Test public void testSourceSelectionDeletion(){
        final SourceSelection e = new SourceSelection(TEST_CLASS, 37, 62);
        final SourceSelection a = new SourceSelection(e.toLocation());
        assertThat("united selection is not empty", !a.isEmpty(), is(true));

        a.delete(e.toLocation());

        assertThat("united selection is not empty", a.isEmpty(), is(true));
    }


    @Test public void testSourceCodeIntersection(){
        final SourceSelection a = new SourceSelection(TEST_CLASS, 37, 62);
        final SourceSelection b = new SourceSelection(TEST_CLASS, 0, 36);
        final SourceSelection c = new SourceSelection(TEST_CLASS, 63, 70);

        final SourceSelection main = new SourceSelection(a.toLocation());
        main.add(b.toLocation());
        main.add(c.toLocation());


        final SourceSelection main2  = new SourceSelection(a.toLocation());
        final SourceSelection aPrime = main.intersects(main2);


        assertThat(Locations.bothSame(aPrime.toLocation(), a.toLocation()), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void testSourceCodeIntersectionFailing(){
        final SourceSelection main = new SourceSelection();
        main.intersects(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSourceCodeUnionFailing(){
        final SourceSelection a = new SourceSelection(TEST_CLASS, 37, 62);
        final SourceSelection main = new SourceSelection(a.toLocation());
        main.union(null);
    }


    @Test(expected = NullPointerException.class)
    public void testSourceCodeAddFailing(){
        final SourceSelection main = new SourceSelection();
        main.union(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSourceCodeContainsFailing(){
        final SourceSelection main = new SourceSelection();
        main.contains(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSourceCodeDeleteFailing(){
        final SourceSelection main = new SourceSelection();
        main.delete(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void testSourceCodeLastFailing(){
        final SourceSelection main = new SourceSelection();
        main.last();
    }


    @Test(expected = NoSuchElementException.class)
    public void testSourceCodeFirstFailing(){
        final SourceSelection main = new SourceSelection();
        main.first();
    }
}
