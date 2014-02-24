package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.SourceHistoryFetcher;
import edu.ucsc.refactor.spi.Repository;
import edu.ucsc.refactor.util.SourceHistory;
import edu.ucsc.refactor.util.StopWatch;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class InternalCommitHistoryFetcherCreator {

    private final Repository repository;

    private String sourceId;

    private final StopWatch stopwatch = new StopWatch();

    /**
     * Instantiates a new {@link InternalCommitHistoryFetcherCreator}.
     *
     * @param repository The remote repository.
     */
    public InternalCommitHistoryFetcherCreator(Repository repository){
        this.repository = repository;
        this.sourceId   = null;
    }


    /**
     * Adds a source Id matching the Source from where a commit history will be fetched.
     *
     * @param sourceId The {@code Source}'s id.
     * @return self
     */
    public InternalCommitHistoryFetcherCreator addSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public SourceHistoryFetcher build() {
        final SourceHistoryFetcherImpl fetcher = new SourceHistoryFetcherImpl(repository, sourceId);
        stopwatch.resetAndLog("SourceHistoryFetcher construction");
        return fetcher;
    }


    static class SourceHistoryFetcherImpl implements SourceHistoryFetcher {
        final Repository repository;
        final String     sourceId;

        SourceHistoryFetcherImpl(Repository repository, String sourceId){
            this.repository = repository;
            this.sourceId   = sourceId;
        }

        @Override public SourceHistory fetchSourceHistory() {
            return fetchSourceHistory(repository, sourceId);
        }

        @Override public SourceHistory fetchSourceHistory(Repository fromRepository, String sourceId) {
            return fromRepository.pull(sourceId);
        }
    }
}
