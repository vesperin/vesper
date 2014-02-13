package edu.ucsc.refactor.util;

import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitSummary;
import edu.ucsc.refactor.spi.Refactoring;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CommitHistoryTest {
    static final String NAME    = "Name.java";

    static final String CONTENT = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "}";

    static final String CONTENT_PRIME = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(){}\n"
            + "}";

    static final String CONTENT_PRIME_PRIME =
            "class Name {\n"
            + "\tvoid boom(){}\n"
            + "}";

    static final String CONTENT_PRIME_PRIME_PRIME =
            "class Name {\n"
            + "\tvoid baam(){}\n"
            + "}";

    static final String CONTENT_PRIME_PRIME_PRIME_PRIME =
            "class Name {\n"
            + "}";



    static final Source BEFORE              = new Source(NAME, CONTENT);
    static final Source AFTER               = new Source(NAME, CONTENT_PRIME);
    static final Source WAY_AFTER           = new Source(NAME, CONTENT_PRIME_PRIME);
    static final Source WAY_WAY_AFTER       = new Source(NAME, CONTENT_PRIME_PRIME_PRIME);
    static final Source WAY_WAY_WAY_AFTER   = new Source(NAME, CONTENT_PRIME_PRIME_PRIME_PRIME);

    @Before public void setUp() throws Exception {
        BEFORE.generateUniqueSignature();
        AFTER.setSignature(BEFORE.getUniqueSignature());
        WAY_AFTER.setSignature(BEFORE.getUniqueSignature());
        WAY_WAY_AFTER.setSignature(BEFORE.getUniqueSignature());
        WAY_WAY_WAY_AFTER.setSignature(BEFORE.getUniqueSignature());
    }

    @Test public void testCreateHistory() throws Exception {

        final Checkpoint checkpoint = createCheckpoint(
                Refactoring.DELETE_PARAMETER,
                BEFORE,
                AFTER
        );

        final CommitHistory history = new CommitHistory(checkpoint);

        assertThat(history.first() != null, is(true));
        assertEquals(history.first(), history.last());
        assertThat(history.size(), is(1));

        assertEquals(
                history.first().getSourceBeforeChange().getUniqueSignature(),
                history.first().getSourceAfterChange().getUniqueSignature()
        );
    }

    @Test public void testResetHistory() throws Exception {
        final Checkpoint c1 = createCheckpoint(Refactoring.DELETE_PARAMETER, BEFORE, AFTER);
        final Checkpoint c2 = createCheckpoint(Refactoring.DELETE_UNUSED_IMPORTS, AFTER, WAY_AFTER);
        final Checkpoint c3 = createCheckpoint(Refactoring.RENAME_METHOD, WAY_AFTER, WAY_WAY_AFTER);
        final Checkpoint c4 = createCheckpoint(Refactoring.DELETE_METHOD, WAY_WAY_AFTER, WAY_WAY_WAY_AFTER);


        final CommitHistory history = new CommitHistory(c1);
        history.add(c2);
        history.add(c3);
        history.add(c4);

        final CommitHistory v1 = history.slice();
        assertThat(v1.size(), is(1));

        final CommitHistory v2 = history.slice(c2);
        assertThat(v2.size(), is(2));

        assertEquals(c1, v2.first());
        assertEquals(c2, v2.last());

        history.clear();
        assertThat(history.isEmpty(), is(true));

    }


    private static Checkpoint createCheckpoint(Refactoring refactoring, Source before, Source after){
       return new Checkpoint(
               refactoring,
               before,
               after,
               System.nanoTime(),
               CommitSummary.forPendingCommit());
    }
}
