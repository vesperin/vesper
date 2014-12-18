package edu.ucsc.refactor.internal;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.CommitHistory;
import edu.ucsc.refactor.util.CommitPublisher;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class NavigableJavaRefactorer implements NavigableRefactorer {
    private static final Logger LOGGER = Logger.getLogger(NavigableJavaRefactorer.class.getName());

    private final JavaRefactorer                refactorer;
    private final Map<String, CommitHistory>    timeline;
    private final Map<Source, List<Issue>>      findings;

    /**
     * Instantiates a new {@link NavigableJavaRefactorer} object.
     * @param refactorer The refactorer without checkpointing.
     */
    NavigableJavaRefactorer(Refactorer refactorer){
        this.refactorer     = (JavaRefactorer) refactorer;
        this.timeline       = Maps.newHashMap();
        this.findings       = Maps.newHashMap();
    }

    @Override public Source next(Source current) {
        final CommitHistory history = getCommitHistory(current);

        for(Commit each : history){
            if(each.getSourceBeforeChange().equals(current)){
                return each.getSourceAfterChange();
            }
        }

        // otherwise, there is nothing to next (i.e., current is the latest version)
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


    @Override public Set<Issue> detectIssues(Source code) {
        final Introspector introspector = Vesper.createIntrospector();
        try {
            final Set<Issue> issues = introspector.detectIssues(code);
            for(Issue each : issues){
                registerIssue(code, each);
            }
            return issues;
        } catch (RuntimeException ex){
            refactorer.getRefactoringHost().addError(ex);
            return ImmutableSet.of();
        }
    }

    private Issue registerIssue(Source source, Issue issue) {
        if(getIssueRegistry().containsKey(source)) { this.findings.get(source).add(issue); } else {
            this.findings.put(source, new ArrayList<Issue>());
            this.findings.get(source).add(issue);
        }

        return issue;
    }

    Map<Source, List<Issue>> getIssueRegistry() {
        return findings;
    }


    @Override public List<Issue> getIssues(Source key) {
        if(getIssueRegistry().containsKey(key)){
            return getIssueRegistry().get(key);
        }

        return Collections.emptyList();
    }

    @Override public CommitHistory getCommitHistory(Source src) {
        final CommitHistory result = timeline.get(Preconditions.checkNotNull(src).getUniqueSignature());
        if(result == null){
            return new CommitHistory();
        }

        return result;
    }


    @Override public CommitPublisher getCommitPublisher(Source src) {
        return new CommitPublisher(
                getCommitHistory(src),
                refactorer.getRefactoringHost().getStorageKey()
        );
    }


    @Override public boolean hasIssues(Source code) {
        return !getIssues(Preconditions.checkNotNull(code)).isEmpty();
    }

    @Override public List<Source> getSources() {
        return new ArrayList<Source>(findings.keySet());
    }


    private void populateCommitHistory(Commit commit){
        // clear current issue registry for source
        final Source before = commit.getSourceBeforeChange();
        getIssueRegistry().remove(before);
        findings.remove(before);

        final String key = commit.getUniqueSignature();

        Preconditions.checkState(
                timeline.containsKey(key),
                "At this point the Source unique signature should have been recorded."
        );

        timeline.get(key).add(commit);
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


    @Override public Source previous(Source current) {
        final CommitHistory history = getCommitHistory(current);

        for(Commit each : history){
            if(each.getSourceAfterChange().equals(current)){
                getIssueRegistry().remove(current);
                return each.getSourceBeforeChange();
            }
        }

        LOGGER.fine("nothing to rollback!");
        return current; // nothing to roll back

    }


    private Source rewritingHistory(Source from, CommitHistory sliced){
        final String signature = from.getUniqueSignature();

        if(timeline.containsKey(signature)){

            timeline.remove(signature);
            timeline.put(signature, sliced);

            detectIssues(from);

        }

        return from;
    }

    void throwCreationErrorsIfExist(){
        refactorer.getRefactoringHost().throwCreationErrorIfErrorsExist();
    }

    @Override public String toString() {
        final Objects.ToStringHelper builder = Objects.toStringHelper(getClass());
        builder.add("known files", getSources());
        return builder.toString();
    }
}
