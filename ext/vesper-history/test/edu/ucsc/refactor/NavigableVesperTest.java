package edu.ucsc.refactor;

import com.google.common.collect.Lists;
import edu.ucsc.refactor.internal.HostImpl;
import edu.ucsc.refactor.internal.Upstream;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitPublisher;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.GistRevision;
import org.eclipse.egit.github.core.service.GistService;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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

        final NavigableRefactorer navigableRefactorer = NavigableVesper.createNavigableRefactorer(refactorer);

        final Set<Issue> issues = navigableRefactorer.detectIssues(CODE);
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

    static class SemiShallowHost extends HostImpl {
    }

    static class ShallowConfiguration extends AbstractConfiguration {
        @Override protected void configure() {
            installDefaultSettings();
            addCredentials(new Credential("lala", "lala"));
        }
    }



    static class LocalGistService extends GistService {
        static Comment comment = new Comment();

        @Override public Gist createGist(Gist gist) throws IOException {
            final GistFile file = new GistFile();
            file.setContent(UPDATED);
            file.setFilename("Name.java");

            gist.setId(null);
            gist.setId(String.valueOf(new Random().nextLong()));
            gist.setFiles(Collections.singletonMap("Name.java", file));
            gist.setCreatedAt(new Date());
            gist.setUrl("http://gist.github.com/lala/123456");
            final List<GistRevision> history = Lists.newArrayList(new GistRevision().setVersion("19C"));
            gist.setHistory(history);
            return gist;
        }

        @Override public Gist getGist(String id) throws IOException {
            return null;
        }

        @Override public Comment createComment(String gistId, String comment) throws IOException {
            LocalGistService.comment.setId(0L);
            LocalGistService.comment.setBody(null);
            return LocalGistService.comment.setBody(comment).setId(new Random().nextLong());
        }

        @Override public List<Comment> getComments(String gistId) throws IOException {
            return Collections.emptyList();
        }

        @Override public Gist updateGist(Gist gist) throws IOException {
            return gist;
        }
    }
}
