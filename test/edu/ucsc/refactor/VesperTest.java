package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.HostImpl;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.Upstream;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class VesperTest {
    static final String UPDATED = "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "}";

    static final String CONTENT = "import java.util.List; \n"
            + "class Name {\n"
            + "\tvoid boom(String msg){}\n"
            + "}";

    static final Source CODE = new Source("Name.java", CONTENT);

    @Test public void testCommitBrandNewChange() throws Exception {
        final List<Source>  folio   = new ArrayList<Source>();

        folio.add(CODE);

        final Refactorer refactorer = Vesper.createRefactorer(
                new ShallowConfiguration(),
                new ShallowHost(),
                folio
        );

        assertThat(refactorer.hasIssues(CODE), is(true));

        final List<Change> suggestedChanges = refactorer.recommendChanges(CODE);
        assertThat(suggestedChanges.isEmpty(), is(false));

        final Change first = suggestedChanges.get(0);

        final CommitRequest applied = refactorer.apply(first);
        assertNotNull(applied);

    }

    @Test public void testUpdateExistingChange(){
        final Source src = new Source("Name.java", UPDATED);
        src.setId(String.valueOf(new Random().nextLong()));

        final List<Source>  folio   = new ArrayList<Source>();
        folio.add(src);

        final Refactorer refactorer = Vesper.createRefactorer(
                new ShallowConfiguration(),
                new SemiShallowHost(),
                folio
        );

        assertThat(refactorer.hasIssues(src), is(true));

        final List<Change> suggestedChanges = refactorer.recommendChanges(src);
        assertThat(suggestedChanges.isEmpty(), is(false));

        final Change first = suggestedChanges.get(0);
        final CommitRequest applied = refactorer.apply(first);
        assertNotNull(applied);
    }


    static class ShallowHost extends HostImpl {
        @Override public Upstream getUpstream() {
            return new ShallowUpstream();
        }
    }

    static class SemiShallowHost extends HostImpl {
        @Override public Upstream getUpstream() {
            return new SemiShallowUpstream();
        }
    }

    static class ShallowConfiguration extends AbstractConfiguration {
        @Override protected void configure() {
            installDefaultSettings();
        }
    }


    static class LocalGistService extends GistService {
        static Comment comment = new Comment();

        @Override
        public Gist createGist(Gist gist) throws IOException {
            final GistFile file = new GistFile();
            file.setContent(UPDATED);
            file.setFilename("Name.java");

            gist.setId(null);
            gist.setId(String.valueOf(new Random().nextLong()));
            gist.setFiles(Collections.singletonMap("Name.java", file));
            gist.setCreatedAt(new Date());
            return gist;
        }

        @Override
        public Comment createComment(String gistId, String comment) throws IOException {
            LocalGistService.comment.setId(0L);
            LocalGistService.comment.setBody(null);
            return LocalGistService.comment.setBody(comment).setId(new Random().nextLong());
        }

        @Override
        public List<Comment> getComments(String gistId) throws IOException {
            return Collections.emptyList();
        }

        @Override
        public Gist updateGist(Gist gist) throws IOException {
            return gist;
        }
    }

    static class SemiShallowUpstream implements Upstream {

        @Override public String getUser() {
            return "srcfolio";
        }

        @Override public GistService get() {
            return new LocalGistService(){

                @Override
                public Gist getGist(String id) throws IOException {
                    final Gist gist = new Gist();
                    final GistFile file = new GistFile();
                    file.setContent(CONTENT);
                    file.setFilename("Name.java");

                    gist.setId(id);
                    gist.setFiles(Collections.singletonMap("Name.java", file));
                    gist.setCreatedAt(new Date());
                    gist.setUpdatedAt(new Date());

                    return gist;
                }
            };
        }
    }

    static class ShallowUpstream implements Upstream {
        @Override public String getUser() {
            return "srcfolio";
        }

        @Override public GistService get() {
            return new LocalGistService(){

                @Override
                public Gist getGist(String id) throws IOException {
                    return null;
                }
            };
        }
    }
}