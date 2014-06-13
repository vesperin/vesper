package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.Upstream;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitPublisher;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class NavigableVesperTest extends VesperTest {
    @Test public void testUpdateExistingChange(){
        final Source src = new Source("Name.java", UPDATED);
        src.setId(String.valueOf(new Random().nextLong()));


        final Refactorer refactorer = Vesper.createRefactorer(
                new ShallowConfiguration(),
                new SemiShallowHost()
        );

        final Set<Issue> issues = refactorer.detectIssues(CODE);
        assertThat(!issues.isEmpty(), is(true));

        final List<Change> suggestedChanges = refactorer.recommendChanges(src, issues);
        assertThat(suggestedChanges.isEmpty(), is(false));

        final Change first = suggestedChanges.get(0);
        final Commit applied = refactorer.apply(first);
        assertNotNull(applied);

        final CommitPublisher publisher = new CommitPublisher();

        final Commit published = publisher.publish(
                applied,
                new Upstream(
                        new Credential("lala", "lala"),
                        new LocalGistService()
                )
        );

        assertEquals(published.getCommitSummary(), applied.getCommitSummary());

        final Commit anotherPublished = publisher.publish(
                applied,
                new Upstream(
                        new Credential("lala", "lala"),
                        new LocalGistService()
                )
        );

        assertNotNull(anotherPublished);
        System.out.println(anotherPublished.getCommitSummary().more());

    }
}
