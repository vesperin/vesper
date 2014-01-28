package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceLocation;
import edu.ucsc.refactor.internal.SourcePosition;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class LocationsTest {
    static final String NAME    = "Name.java";
    static final String CONTENT = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "}";

    static final Source SOURCE = new Source(NAME, CONTENT);

    @Test public void testLocationsBothSame(){
        final Location a = SourceLocation.createLocation(SOURCE, CONTENT, 0, CONTENT.length());
        final Location b = SourceLocation.createLocation(SOURCE, CONTENT, 0, CONTENT.length());

        assertThat(Locations.bothSame(a, b), is(true));


    }

    @Test public void testLocationsOneCoversOther(){
        final Location a = SourceLocation.createLocation(SOURCE, CONTENT, 0, CONTENT.length());
        final Location b = SourceLocation.createLocation(SOURCE, CONTENT, 12, 20);

        assertThat(Locations.covers(a, b), is(true));

    }

    @Test public void testLocationsOneIsCoveredByOther(){
        final Location a = SourceLocation.createLocation(SOURCE, CONTENT, 0, CONTENT.length());
        final Location b = SourceLocation.createLocation(SOURCE, CONTENT, 12, 20);

        assertThat(Locations.coveredBy(b, a), is(true));
    }

    @Test public void testLocationsOneIntersectsTheOther(){
        final Location a = SourceLocation.createLocation(SOURCE, CONTENT, 0, 30);
        final Location b = SourceLocation.createLocation(SOURCE, CONTENT, 22, 67);

        assertThat(Locations.intersects(a, b), is(true));

        final Location c = SourceLocation.createLocation(SOURCE, CONTENT, 0, 30);
        final Location d = SourceLocation.createLocation(SOURCE, CONTENT, 0, 5);

        assertThat(Locations.intersects(d, c), is(true));
        assertThat(Locations.begins(c, d.getStart()), is(true));
        assertThat(Locations.ends(c, d.getEnd()), is(false));
    }

    @Test public void testLocationsOneEndsInTheOther(){
        final Location a = SourceLocation.createLocation(SOURCE, CONTENT, 0, 30);
        final Location b = SourceLocation.createLocation(SOURCE, CONTENT, 30, 67);

        assertThat(Locations.endsIn(a, b), is(true));
    }


    @Test public void testLocationsLiesOutside(){
        final Location a = SourceLocation.createLocation(SOURCE, CONTENT, 30, 35);
        final Location b = SourceLocation.createLocation(
                SOURCE, new SourcePosition(0, 0, 0), new SourcePosition(0, 0, 0));
        final Location c = SourceLocation.createLocation(SOURCE, CONTENT, 40, 67);

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);

        assertThat(Locations.liesOutside(b, a), is(true));
        assertThat(Locations.liesOutside(c, a), is(true));
    }


}
