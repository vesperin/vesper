package edu.ucsc.refactor.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.ucsc.refactor.Credential;
import edu.ucsc.refactor.Note;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.spi.CommitSummary;
import edu.ucsc.refactor.spi.Name;
import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.Notes;
import edu.ucsc.refactor.util.SourceHistory;
import edu.ucsc.refactor.util.StringUtil;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.GistRevision;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Upstream implements Repository {
    private static final Logger LOGGER = Logger.getLogger(Upstream.class.getName());

    private final Credential    credential;
    private final GistService   service;

    /**
     * Construct a new {@code Credential} object with the
     * remote {@code repository}'s credential.
     *
     * @param credential Authentication information.
     */
    public Upstream(Credential credential){
        this(credential, new GistService());
    }

    /**
     * Construct a new Upstream
     *
     * @param credential The storage service key
     * @param service The GistService object
     */
    public Upstream(Credential credential, GistService service){

        if(credential != null && "None".equals(credential.getUsername())){
            service.getClient().setCredentials(credential.getUsername(), credential.getPassword());
        }

        this.credential = Preconditions.checkNotNull(credential);
        this.service    = Preconditions.checkNotNull(service);
    }


    @Override public Commit push(Commit commit) {
        Preconditions.checkArgument(!credential.isNoneCredential(), "incorrect credentials; this refactorer is not setup yet for remote publishing!");
        Preconditions.checkNotNull(commit, "push() received a null commit");
        try {
            final Source after = commit.getSourceAfterChange();

            final Gist gist = new GistBuilder(service)
                    .content(after.getContents())
                    .file(after)
                    .build();

            // fill out the `more` information
            final Name   info       = commit.getNameOfChange();

            sync(after, gist);

            final String username   = credential.getUsername();

            commit.amendSummary(
                    CommitSummary.forSuccessfulCommit(
                            gist.getId(),
                            username,
                            gist.getCreatedAt(),
                            gist.getUrl(),
                            info.getSummary()
                    )
            );


        } catch (Throwable ex){
            commit.amendSummary(
                    CommitSummary.forFailedCommit(ex.getMessage())
            );
        }

        return commit;
    }

    @Override public SourceHistory pull(String historyForId) {
        try {
            Preconditions.checkArgument(!StringUtil.isStringEmpty(historyForId), "invalid source id");

            final Gist gist = service.getGist(historyForId);
            final List<GistRevision> revisions = gist.getHistory();

            final SourceHistory history = new SourceHistory();

            for(GistRevision revision : revisions){
                addSourceFromUrl(history, historyForId, revision.getVersion(), revision.getUrl());
            }


            return history;

        } catch (Throwable ex){
            LOGGER.throwing("Unable to pull history", "pull()", ex);
            return new SourceHistory();
        }
    }


    // Read from a URL and return the content in a String
    public static void addSourceFromUrl(SourceHistory history, String sourceId, String version, String urlString) throws IOException {
        String jsonContent = readUrlContent(urlString);

        final JsonElement jsonElement           = new JsonParser().parse(jsonContent);
        final JsonObject  elementAsJsonObject   = jsonElement.getAsJsonObject();
        final JsonObject  fileObject            = elementAsJsonObject.get("files").getAsJsonObject();

        for(Map.Entry<String, JsonElement> each : fileObject.entrySet()){

            final String     fileName   = each.getKey();
            final JsonObject eachFile   = fileObject.get(fileName).getAsJsonObject();

            if(eachFile.has("content")){
                // todo(Huascar) there must be some process that will finish the updating of
                // these retrieved sources (e.g., adding their comments.
                final Source src = new Source(fileName, eachFile.get("content").getAsString());
                src.setId(sourceId);
                src.setVersion(version);
                history.add(src);
            }
        }
    }


    private static String readUrlContent(String urlString) throws IOException {
        final URL     url  = new URL(urlString);
        final Scanner scan = new Scanner(url.openStream());

        String content = "";

        while (scan.hasNext()){
            content += scan.nextLine();
        }

        scan.close();

        return content;
    }

    private static void sync(Source src, Gist gist) throws IOException {
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
    }


    @Override public String toString() {
        return "Upstream";
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

                    this.code.setVersion(Iterables.get(result.getHistory(), 0).getVersion());

                } else {
                    if(isDirty(this, remote)){

                        // anything below is about creating revisions to file

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

                        this.code.setVersion(Iterables.getLast(result.getHistory()).getVersion());
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
