package edu.ucsc.refactor;

import com.google.common.collect.ImmutableList;
import edu.ucsc.refactor.internal.InternalNavigableRefactorerCreator;

/**
 * <p>
 * Vesper, in classical latin, means `evening.` However, the reason I named this
 * API vesper is because I really like Vesper martinis :-) and not because I love evenings, which
 * I do.
 * </p>
 *
 * <p>
 * The following code shows how one can start using the Vesper library.
 * </p>
 *
 * <pre>
 *     import static edu.ucsc.refactor.ChangeRequest.forIssue;
 *     import static edu.ucsc.refactor.ChangeRequest.reformatSource;
 *
 *     final Source          code       = new Source(...);
 *     // using your own configuration:
 *     // refactorer = Vesper.createRefactorer(new MyConfiguration());
 *     // using default configuration
 *     final Refactorer      refactorer = Vesper.createRefactorer();
 *
 *     final Map<String, Parameter> userInput = ...;
 *
 *     // I. Ask the refactorer to recommend changes for you
 *
 *     // print the reason for the change
 *
 *     final List<Change> changes = refactorer.recommendChanges(code);
 *     for(Change each : changes){
 *        System.out.println(each.getCause().getName());
 *     }
 *
 *     // perform a single recommended change
 *
 *     final CommitRequest applied = refactor.apply(changes.get(0));
 *     System.out.println(applied.more());
 *
 *     // or handle All recommended changes
 *
 *     List<Change> recommended = refactorer.recommendChanges(code);
 *     while(!recommended.isEmpty()){
 *         final CommitRequest applied = refactor.apply(recommended.get(0));
 *         System.out.println(applied.more());
 *         recommended = refactorer.recommendChanges(code); // get an updated list of changes
 *     }
 *
 *
 *     // II. Dealing with random edits started by the user
 *
 *     // reformat Source's content
 *
 *     Change reformat = refactorer.createChange(reformatSource(code));
 *     final CommitRequest applied = refactor.apply(reformat);
 *     System.out.println(applied.more());
 *
 *     // III. Publishing locally committed changes to a remote repository (e.g., Gist.Github.com)
 *
 *     // Let's assume that vesper has been given the right credentials to access a remote repo
 *     // (e.g., see default configuration for how to do this). Let's call this authenticated repo
 *     AuthenticatedUpstream..
 *
 *     final CommitPublisher publisher = new CommitPublisher();
 *     // publish changes
 *     final Commit published = publisher.publish(applied, new AuthenticatedUpstream());
 *     System.out.println(published.getCommitSummary().more());
 *
 *     // Assuming a source have been changed many times and we have kept a commit history
 *     // of those changes. THis mean that the user has used the NavigableRefactorer.
 *     // Here is how to use this refactorer in order to publish those changes
 *
 *     final NavigableRefactorer cf = Vesper.createNavigableRefactorer(code);
 *     // .. changes are made to 'code'
 *
 *     final CommitPublisher publisher = cf.getCommitPublisher(code);
 *     // publish changes
 *     publisher.publish();
 *
 *     // IV. Retrieving the SourceHistory of a Source (previously curated)
 *
 *     final SourceRecalling recalling = new SourceRecalling(new AuthenticatedUpstream(), "123456");
 *
 *     final SourceHistory history = recalling.recall();
 *
 *     // iterating over the history is a just a matter of a simple for-each loop
 *     Source pivot = null;
 *     for(Source eachSrc : history){
 *         if(pivot == null){
 *             pivot = eachSrc; continue;
 *         }
 *
 *         System.out.println(recalling.differences(pivot, eachSrc).toString());
 *     }
 *
 *     // V. More?
 *
 *     // see {@code Refactorer}'s API for more details.
 *
 * </pre>
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class NavigableVesper {
    private NavigableVesper(){}

    /**
     * Creates a navigable refactorer for the given array of sources
     *
     * @param sources The array of sources.
     *
     * @return a new Refactorer
     */
    public static NavigableRefactorer createNavigableRefactorer(Source... sources){
        return NavigableVesper.createNavigableRefactorer(Vesper.createRefactorer(), sources);
    }

    /**
     * Creates a navigable refactorer for the given array of sources
     *
     * @param refactorer The plain refactorer
     * @param sources The array of sources.
     *
     * @return a new Refactorer
     */
    public static NavigableRefactorer createNavigableRefactorer(Refactorer refactorer, Source... sources){
        final ImmutableList<Source> seed = ImmutableList.copyOf(sources);

        if(seed.contains(null)) {
            throw new CreationException(
                    ImmutableList.of(new Throwable("createRefactorer() has been given a null configuration."))
            );
        }

        return NavigableVesper.createNavigableRefactorer(refactorer, seed);
    }

    /**
     * Creates a navigable refactorer from a plain refactorer and for the given array of sources
     *
     * @param refactorer The plain refactorer
     * @param sources The array of sources.
     *
     * @return a new Refactorer
     */
    private static NavigableRefactorer createNavigableRefactorer(Refactorer refactorer, Iterable<Source> sources){
        return new InternalNavigableRefactorerCreator(refactorer)
                .addSources(sources)
                .build();
    }
}
