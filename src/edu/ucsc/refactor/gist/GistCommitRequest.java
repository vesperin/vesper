package edu.ucsc.refactor.gist;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.Name;
import edu.ucsc.refactor.spi.Upstream;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.GistService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class GistCommitRequest implements CommitRequest {
    private static final Logger LOGGER      = Logger.getLogger(GistCommitRequest.class.getName());
    private static final String DOT_JAVA    = ".java";

    private final Change            change;
    private final Queue<Delta>      load;

    private final AtomicReference<Source> fileMatchingLastDelta;
    private final StringBuilder moreBuilder;


    /**
     * Instantiates a new {@link GistCommitRequest}
     * @param change The change to be applied and transmitted.
     */
    public GistCommitRequest(Change change){
        this.change     = change;
        this.load       = new LinkedList<Delta>();

        for(Delta each : change.getDeltas()){ // in order
            this.load.add(each);
        }


        this.fileMatchingLastDelta  = new AtomicReference<Source>();
        this.moreBuilder            = new StringBuilder();
    }

    @Override public boolean isValid() {
        return change.isValid();
    }

    static String squashedDeltas(String name, Queue<Delta> deltas) throws RuntimeException {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(name, DOT_JAVA);
            while (!deltas.isEmpty()){
                final Delta next = deltas.remove();
                Files.write(next.getAfter().getBytes(), tempFile);
            }

            Files.readLines(tempFile, Charset.defaultCharset());

            return Joiner.on("\n").join(
                    Files.readLines(
                            tempFile,
                            Charset.defaultCharset()
                    )
            );
        } catch (Throwable ex){
            throw new RuntimeException(ex);
        } finally {
            if(tempFile != null){
                if(tempFile.exists()){
                    final boolean deleted = tempFile.delete();
                    LOGGER.fine(name + " file was deleted? " + deleted);
                }
            }
        }
    }


    static Source createSource(GistService service, Gist gist) throws IOException {
        final String        id           = gist.getId();
        final String        description  = gist.getDescription();
        final List<Comment> comments     = service.getComments(id);

        String name     = null;
        String content  = null;

        for(String eachKey : gist.getFiles().keySet()){ // it should be only one file
            name    = gist.getFiles().get(eachKey).getFilename();
            content = gist.getFiles().get(eachKey).getContent();
        }

        assert name     != null;
        assert content  != null;

        final Source updatedSource = new Source(name, content, description);
        updatedSource.setId(id);

        for(Comment eachComment : comments){
            // todo(Huascar) in the future, we can store the user who made
            // the comment. This will be great when having multiple ppl
            // updating the file. This will be cool.
            updatedSource.addComment(eachComment.getBody());
        }

        return updatedSource;
    }

    @Override public void commit(Upstream to) throws RuntimeException {
        final Source            current             = this.load.peek().getSourceFile();
        final GistService       service             = (GistService) to.get();
        final boolean           isAboutToBeUpdated  = !this.load.isEmpty();
        final GistRepository    repository          = ((GistRepository)to);
        final String            username            = repository.getCredential().getUsername();
        final String            fileName            = StringUtil.extractName(current.getName());

        try {
            Gist local = new GistBuilder(service, isAboutToBeUpdated)
                    .content(squashedDeltas(fileName, this.load))
                    .file(current)
                    .user(username)
                    .build();

            fileMatchingLastDelta.set(createSource(service, local));


            // fill out the `more` information
            final Name info = change.getCause().getName();
            moreBuilder.append("commit ").append(local.getId()).append("\n");
            moreBuilder.append("Author:\t").append(username).append("\n");
            moreBuilder.append("Date:\t").append(local.getUpdatedAt()).append("\n\n\t\t");
            moreBuilder.append(info.getKey()).append(": ").append(info.getSummary()).append("\n");

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override public Source getUpdatedSource() {
        assert this.fileMatchingLastDelta.get() != null;

        return this.fileMatchingLastDelta.get();
    }


    @Override public String more() {
        return moreBuilder.toString();
    }


    /**
     * Helper class to create or update a {@code Gist}.
     */
    static class GistBuilder {
        final GistService service;
        final boolean     aboutTobeUpdated;
        final Set<String> comments;

        // optional values
        String description;
        String content;
        Source code;
        String username;

        /**
         * Construct a builder with {@code GistService} and whether
         * we will be updating a gist file as values.
         */
        GistBuilder(GistService service, boolean isAboutTobeUpdated){
            this.service            = service;
            this.aboutTobeUpdated   = isAboutTobeUpdated;
            this.comments           = new HashSet<String>();

            this.description        = null;
            this.content            = null;
            this.code               = null;
            this.username           = null;
        }

        GistBuilder description(String description) {
            this.description = description;
            return this;
        }

        GistBuilder comments(Set<String> comments){
            this.comments.clear();
            this.comments.addAll(comments);
            return this;
        }

        GistBuilder content(String content){
            this.content = content;
            return this;
        }

        GistBuilder file(Source code){
            this.code = code;
            description(this.code.getDescription()); // one and only description
            comments(this.code.getComments());
            return this;
        }

        GistBuilder user(String username){
            this.username = username;
            return this;
        }

        public Gist build() throws RuntimeException {
            Gist result = null;
            try {
                final Gist remote = (this.code.getId() != null
                                        ? service.getGist(this.code.getId())
                                        : null);
                if(remote == null){
                    Gist brandNew = new Gist().setDescription(this.description);

                    final String updatedContent = this.content;
                    final String fileName       = this.code.getName();

                    final GistFile file = new GistFile();
                    file.setContent(updatedContent);
                    file.setFilename(fileName);

                    brandNew.setFiles(Collections.singletonMap(fileName, file));
                    brandNew.setPublic(false);

                    // add comments
                    for(String eachCommentContent : comments){
                        final Comment each = new Comment();
                        each.setBody(eachCommentContent);
                        each.setUser(new User().setName(username));
                    }

                    result = service.createGist(brandNew);
                } else {
                    if(isDirty(this, remote) || aboutTobeUpdated){

                        for(String each : remote.getFiles().keySet()){ // ONLY one file
                            remote.getFiles().get(each).setContent(content);
                            remote.getFiles().get(each).setFilename(code.getName());
                            remote.setDescription(description);
                        }

                        final List<String> comments = complement(
                                code.getComments(), service.getComments(remote.getId())
                        );

                        // add only new comments
                        for(String eachCommentContent : comments){
                            final Comment each = new Comment();
                            each.setBody(eachCommentContent);
                            each.setUser(remote.getUser());
                        }


                        result = service.updateGist(remote);
                    }
                }
            } catch (Throwable ex){
                throw new RuntimeException(ex);
            }

            return result;
        }

        static List<String> complement(Set<String> input, List<Comment> comments){
            final List<String> result = new ArrayList<String>();
            for(Comment eachComment : comments){
                result.add(eachComment.getBody());
            }

            // removes from input all the elements that are also contained
            // in result.
            input.removeAll(result);
            return new ArrayList<String>(input);
        }


        static boolean isDirty(GistBuilder builder, Gist remote) throws RuntimeException {
            // determine what we are updating...
            final boolean updateDescription = !(StringUtil.isStringEmpty(
                    builder.description));
            final boolean updateComments    = !builder.comments.isEmpty();
            final boolean updateName        = !sameName(
                    builder.code.getName(), remote.getFiles()
            );

            final boolean updateContent   = !sameContent(builder.content, remote.getFiles());

            return updateDescription || updateComments || updateName || updateContent;
        }


        static boolean sameContent(String content, Map<String, GistFile> files){
            for(String key : files.keySet()){
                final GistFile file = files.get(key);
                if(StringUtil.equals(content, file.getContent())){
                    return true;
                }
            }
            return false;
        }


        static boolean sameName(String name, Map<String, GistFile> files){
            for(String key : files.keySet()){
                final GistFile file = files.get(key);
                if(StringUtil.equals(name, file.getFilename())){
                    return true;
                }
            }
            return false;
        }

    }
}
