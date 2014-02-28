package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.spi.UnitLocator;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CheckpointedJavaRefactorer implements CheckpointedRefactorer {
    private static final Logger LOGGER = Logger.getLogger(CheckpointedJavaRefactorer.class.getName());

    private final JavaRefactorer                refactorer;
    private final Map<String, CommitHistory>    timeline;

    /**
     * Instantiates a new {@link CheckpointedJavaRefactorer} object.
     * @param refactorer The refactorer without checkpointing.
     */
    CheckpointedJavaRefactorer(Refactorer refactorer){
        this.refactorer     = (JavaRefactorer) refactorer;
        this.timeline       = Maps.newHashMap();
    }

    @Override public Source advance(Source current) {
        final CommitHistory history = getCommitHistory(current);

        for(Commit each : history){
            if(each.getSourceBeforeChange().equals(current)){
                return each.getSourceAfterChange();
            }
        }

        // otherwise, there is nothing to advance (i.e., current is the latest version)
        return current;
    }

    @Override public Commit apply(Change change) {
        final Commit committed = refactorer.apply(change);

        if(committed != null){
            detectIssues(checkpoint(committed));
        }

        return committed;
    }

    @Override public Change createChange(ChangeRequest request) {
        return refactorer.createChange(request);
    }

    @Override public Source checkpoint(Commit commit) {
        createCommitHistory(commit);
        populateCommitHistory(commit);
        return commit.getSourceAfterChange();
    }


    private void createCommitHistory(Commit commit){
        final Source before = commit.getSourceBeforeChange();
        if(!timeline.containsKey(before.getUniqueSignature())){  // the signature should have been generated during compilation.
            this.timeline.put(before.getUniqueSignature(), new CommitHistory());
        }
    }


    @Override public void detectIssues(Source code) {
        refactorer.detectIssues(code);
    }


    @Override public List<Issue> getIssues(Source key) {
        return refactorer.getIssues(key);
    }

    @Override public CommitHistory getCommitHistory(Source src) {
        final CommitHistory result = timeline.get(Preconditions.checkNotNull(src).getUniqueSignature());
        if(result == null){
            return new CommitHistory();
        }

        return result;
    }


    @Override public CommitPublisher getCommitPublisher(Source src) {
        return new CommitHistoryPublisher(
                getCommitHistory(src),
                refactorer.getRefactoringHost().getStorageKey(),
                refactorer.getRefactoringHost().isRemoteUpstreamEnabled()
        );
    }


    @Override public UnitLocator getLocator(Source src) {
        return refactorer.getLocator(src);
    }

    @Override public boolean hasIssues(Source code) {
        return refactorer.hasIssues(code);
    }

    @Override public List<Source> getTrackedSources() {
        return new ArrayList<Source>(refactorer.getValidContexts().keySet());
    }


    private void populateCommitHistory(Commit commit){
        // clear current issue registry for source
        refactorer.getIssueRegistry().remove(commit.getSourceBeforeChange());
        refactorer.getValidContexts().remove(commit.getSourceBeforeChange());

        final String key = commit.getUniqueSignature();

        Preconditions.checkState(
                timeline.containsKey(key),
                "At this point the Source unique signature should have been recorded."
        );

        timeline.get(key).add(commit);
    }


    @Override public List<Change> recommendChanges(Source code) {
        return refactorer.recommendChanges(code);
    }

    @Override public Source rewriteHistory(Source source) {

        final Source        from   = Preconditions.checkNotNull(source);
        final CommitHistory entire = getCommitHistory(from);

        if(entire.isEmpty()) { return source; }

        final Commit first  = entire.first();
        final Commit last   = entire.last();


        if(first.getSourceBeforeChange().equals(from)) { // base case
            return rewritingHistory(from, entire.slice());
        }

        if(last.getSourceAfterChange().equals(from)) {  // base case
            return from;
        }


        final CommitHistory sandwiched = entire.slice(first, false, last, false);

        for(Commit each : sandwiched){

            if(each.getSourceAfterChange().equals(from)){
                final CommitHistory sliced = entire.slice(each);

                Preconditions.checkArgument(
                        from.getUniqueSignature().equals(sliced.last().getUniqueSignature()),
                        "rewriteHistory() is dealing with sources that are not part "
                                + "of the same change history"
                );

                return rewritingHistory(from, sliced);

            }
        }

        throw new NoSuchElementException("rewriteHistory() was unable to find " + from);
    }


    @Override public Source regress(Source current) {
        final CommitHistory history = getCommitHistory(current);

        for(Commit each : history){
            if(each.getSourceAfterChange().equals(current)){
                return each.getSourceBeforeChange();
            }
        }

        LOGGER.fine("nothing to rollback!");
        return current; // nothing to roll back

    }


    private Source rewritingHistory(Source from, CommitHistory sliced){
        final String signature = from.getUniqueSignature();

        if(timeline.containsKey(signature)){
            refactorer.getIssueRegistry().remove(from);

            timeline.remove(signature);
            timeline.put(signature, sliced);

            detectIssues(from);

        }

        return from;
    }


    @Override public String toString() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        builder.add("known files", getTrackedSources());
        return builder.toString();
    }

    /**
     * Helper class used to publish all the commits that belong to a given
     * {@code CommitHistory}
     */
    static class CommitHistoryPublisher implements CommitPublisher {
        final CommitHistory history;
        final Credential    credential;
        final boolean       isRemotePublishingEnabled;

        CommitHistoryPublisher(CommitHistory history, Credential credential, boolean isRemotePublishingEnabled){
            this.history                    = history;
            this.credential                 = credential;
            this.isRemotePublishingEnabled  = isRemotePublishingEnabled;
        }

        @Override public List<Commit> publish() {
            return publish(new Upstream(credential));
        }

        @Override public List<Commit> publish(Repository to) {

            Preconditions.checkState(
                    isRemotePublishingEnabled,
                    "this refactorer is not setup yet for remote publishing"
            );

            final List<Commit> commitsToDelete = Lists.newArrayList();
            for(Commit eachCommit : history){ // in order

                final Commit pushed = publish(eachCommit, to);

                if(pushed.isValidCommit()){
                    commitsToDelete.add(pushed);
                }
            }

            return commitsToDelete;
        }


        @Override public Commit publish(Commit request, Repository upstream) {
            return Preconditions.checkNotNull(upstream).push(
                    Preconditions.checkNotNull(request)
            );
        }
    }
}
