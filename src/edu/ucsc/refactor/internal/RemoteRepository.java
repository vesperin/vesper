package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.Note;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.CommitStatus;
import edu.ucsc.refactor.spi.Name;
import edu.ucsc.refactor.spi.Upstream;
import edu.ucsc.refactor.util.CommitInformation;
import edu.ucsc.refactor.util.Notes;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoteRepository implements Upstream {
    private final GistService service;
    private final Credential  credential;

    /**
     * Construct a new RemoteRepository
     *
     * @param key The storage service key
     */
    public RemoteRepository(Credential key){
        this(key, new GistService());
    }

    /**
     * Construct a new RemoteRepository
     *
     * @param key The storage service key
     * @param service The GistService object
     */
    public RemoteRepository(Credential key, GistService service){
        if(key != null && "None".equals(key.getUsername())){
            service.getClient().setCredentials(key.getUsername(), key.getPassword());
        }
        this.service    = service;
        this.credential = key;
    }

    @Override public CommitStatus publish(CommitRequest request) {
        if(!request.isValid()) return request.getStatus();

        final Source updatedSource = request.getUpdatedSource();
        Gist gist = new GistBuilder(service)
                .content(updatedSource.getContents())
                .file(updatedSource)
                .build();


        try {
            ((AbstractCommitRequest)request).updateSource(sync(updatedSource, gist));

            // fill out the `more` information
            final Name info = ((AbstractCommitRequest)request).getChange().getCause().getName();

            ((AbstractCommitRequest)request).updateStatus(
                    CommitStatus.succeededStatus(
                            new CommitInformation()
                                    .commit(gist.getId())
                                    .author(getUser())
                                    .date(gist.getCreatedAt())
                                    .comment(info.getKey(), info.getSummary())
                    )
            );

            return request.getStatus();
        } catch (Throwable ex){
            ((AbstractCommitRequest)request).updateStatus(
                    CommitStatus.failedStatus(
                            new CommitInformation()
                                    .error(ex.getMessage())
                    ));

            return request.getStatus();
        }

    }

    private static Source sync(Source src, Gist gist) throws IOException {
        final String        id           = gist.getId();
        final String        description  = gist.getDescription();

        String name     = null;
        String content  = null;

        for(String eachKey : gist.getFiles().keySet()){ // it should be only one file
            name    = gist.getFiles().get(eachKey).getFilename();
            content = gist.getFiles().get(eachKey).getContent();
        }

        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(content);
        Preconditions.checkNotNull(description);
        Preconditions.checkArgument(src.getName().equals(name), "name mismatch");
        Preconditions.checkArgument(description.equals(src.getDescription()), "description mismatch");

        assert name     != null;
        assert content  != null;

        src.setId(id);

        return src;
    }

    Credential getCredential(){
        return credential;
    }

    public String getUser() {
        return getCredential().getUsername();
    }


    /**
     * Helper class to create or update a {@code Gist}.
     */
    static class GistBuilder {
        final GistService service;

        Notes notes;

        // optional values
        String description;
        String content;
        Source code;

        /**
         * Construct a builder with {@code GistService} and whether
         * we will be updating a gist file as values.
         */
        GistBuilder(GistService service){
            this.service            = service;
            this.notes              = new Notes();

            this.description        = null;
            this.content            = null;
            this.code               = null;
        }

        GistBuilder description(String description) {
            this.description = description;
            return this;
        }

        GistBuilder notes(Notes comments){
            this.notes.clear();
            this.notes = this.notes.union(comments);
            return this;
        }

        GistBuilder content(String content){
            this.content = content;
            return this;
        }

        GistBuilder file(Source code){
            this.code = code;
            description(this.code.getDescription()); // one and only description
            notes(this.code.getNotes());
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

                    result = service.createGist(brandNew);

                    // add comments to an already created gist
                    for(Note eachNote : notes){
                        service.createComment(result.getId(), eachNote.getContent());
                    }

                } else {
                    if(isDirty(this, remote)){

                        for(String each : remote.getFiles().keySet()){ // ONLY one file
                            remote.getFiles().get(each).setContent(content);
                            remote.getFiles().get(each).setFilename(code.getName());
                            remote.setDescription(description);
                        }

                        final Notes newNotes = difference(
                                code.getNotes(),
                                service.getComments(remote.getId())
                        );

                        result = service.updateGist(remote);

                        // add only new comments
                        for(Note eachNote : newNotes){
                            service.createComment(result.getId(), eachNote.getContent());
                        }
                    }
                }
            } catch (Throwable ex){
                throw new RuntimeException(ex);
            }

            return result;
        }

        static Notes difference(Notes input, List<Comment> comments){
            Notes that = new Notes();
            // todo(Huascar) How can we store the location or source selection?
            // as of now, these locations are null.
            for (Comment x : comments) {
                final Note each = new Note(x.getBody());
                each.setId(String.valueOf(x.getId()));
                each.setUser(x.getUser().getName());
                that.add(each);
            }

            return input.difference(that);
        }


        static boolean isDirty(GistBuilder builder, Gist remote) throws RuntimeException {
            // determine what we are updating...
            final boolean updateDescription = !(StringUtil.isStringEmpty(
                    builder.description));
            final boolean updateNotes       = !builder.notes.isEmpty();
            final boolean updateName        = !sameName(
                    builder.code.getName(), remote.getFiles()
            );

            final boolean updateContent   = !sameContent(builder.content, remote.getFiles());

            return updateDescription || updateNotes || updateName || updateContent;
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
