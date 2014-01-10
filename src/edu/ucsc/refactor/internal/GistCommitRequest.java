package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Note;
import edu.ucsc.refactor.Source;
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
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class GistCommitRequest extends AbstractCommitRequest {
    private static final Logger LOGGER      = Logger.getLogger(GistCommitRequest.class.getName());


    /**
     * Instantiates a new {@link GistCommitRequest}
     * @param change The change to be applied and transmitted.
     */
    public GistCommitRequest(Change change){
        super(change);
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

            final String noteId         = String.valueOf(eachComment.getId());
            final String user           = eachComment.getUser().getName();
            final String noteContent    = eachComment.getBody();

            final Note note = new Note(
                    noteId,
                    user,
                    noteContent
            );

            updatedSource.addNote(note);
        }

        return updatedSource;
    }

    @Override public CommitStatus commit(Upstream to) throws RuntimeException {
        final Source            current             = getLoad().peek().getSource();
        final GistService       service             = (GistService) to.get();
        final boolean           isAboutToBeUpdated  = !getLoad().isEmpty();

        if(!isAboutToBeUpdated){ return CommitStatus.nothingStatus(); }

        final String            username            = to.getUser();
        final String            fileName            = StringUtil.extractName(current.getName());
        final ASTNode           node                = getChange().getCause().getAffectedNodes().get(0); // never null

        try {
            Gist local = new GistBuilder(service)
                    .content(squashedDeltas(fileName, getLoad(), node))
                    .file(current)
                    .user(username)
                    .build();

            updateSource(createSource(service, local));


            // fill out the `more` information
            final Name info = getChange().getCause().getName();

            final boolean updatedDate = local.getUpdatedAt() != null;
            final boolean createdDate = local.getCreatedAt() != null;

            final Date date = createdDate
                                ? (updatedDate ? local.getUpdatedAt() : local.getCreatedAt())
                                : (updatedDate ? local.getUpdatedAt() : new Date());


            updateStatus(
                    CommitStatus.succeededStatus(
                            new CommitInformation()
                                    .commit(local.getId())
                                    .author(username)
                                    .date(date)
                                    .comment(info.getKey(), info.getSummary()
                                    )
                    )
            );

            return getStatus();

        } catch (Throwable ex) {
            updateStatus(
                    CommitStatus.failedStatus(
                            new CommitInformation()
                                    .error(ex.getMessage()
                                    )
                    ));

            LOGGER.throwing("unable to commit change", "commit(Upstream)", ex);
             throw new RuntimeException(ex);
        }

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
        String username;

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
            this.username           = null;
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
