package edu.ucsc.refactor;

import com.google.common.collect.ImmutableList;
import edu.ucsc.refactor.internal.InternalNavigableRefactorerCreator;

/**
 * <p>
 * An extension to the Vesper API.
 * </p>
 *
 * <p>
 * The following code shows how one can start using the NavigableVesper API.
 * </p>
 *
 * <pre>
 *     import static edu.ucsc.refactor.ChangeRequest.forIssue;
 *     import static edu.ucsc.refactor.ChangeRequest.reformatSource;
 *
 *     final Source               code       = new Source(...);
 *     final NavigableRefactorer  refactorer = NavigableVesper.createNavigableRefactorer(Vesper.createRefactorer());
 *
 *     final Map<String, Parameter> userInput = ...;
 *
 *
 *     // I. Publishing locally committed changes to a remote repository (e.g., Gist.Github.com)
 *
 *     // Assuming a source have been changed many times.
 *     // Here is how to use this navigable refactorer in order to publish those changes
 *
 *     // .. changes are made to 'code'
 *
 *     final CommitPublisher publisher = refactorer.getCommitPublisher(code);
 *     // publish changes
 *     publisher.publish();
 *
 *     // II. Retrieving the SourceHistory of a Source (previously curated)
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
