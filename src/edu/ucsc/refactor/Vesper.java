package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.HostImpl;
import edu.ucsc.refactor.internal.InternalCommitHistoryFetcherCreator;
import edu.ucsc.refactor.internal.InternalRefactorerCreator;
import edu.ucsc.refactor.spi.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 *     // refactorer = Vesper.createRefactorer(new MyConfiguration(), code);
 *     // using default configuration
 *     final Refactorer      refactorer = Vesper.createRefactorer(code);
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
 *     // (e.g., see default configuration for how to do this).
 *
 *     // publish changes
 *     final CommitRequest published = refactorer.publish(applied);
 *     System.out.println(published.more());
 *
 *     // IV. Retrieving the CommitHistory of a Source (previously curated)
 *
 *     final SourceHistoryFetcher fetcher = Vesper.createSourceHistoryFetcher(new Upstream(...), "123456");
 *
 *     final SourceHistory history = fetcher.fetchSourceHistory();
 *
 *     // iterating over the history is a just a matter of a simple for-each loop
 *     for(Source eachSrc : history){
 *         System.out.println(eachSrc.getName());
 *     }
 *
 *     // V. More?
 *
 *     // see {@code Refactorer}'s API for more details.
 *
 * </pre>
 *
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public final class Vesper {
    private final static Configuration DEFAULT_CONFIG = new DefaultConfiguration();

    /**
     * Private constructor to prevent instantiation.
     */
    private Vesper(){}

    /**
     * Creates a commit history fetcher for the given repository and source id.
     *
     * @param repository The remote repository.
     * @param sourceId   The {@code Source}'s id.
     * @return a new SourceHistoryFetcher.
     */
    public static SourceHistoryFetcher createSourceHistoryFetcher(Repository repository, String sourceId){
        return new InternalCommitHistoryFetcherCreator(repository)
                .addSourceId(sourceId)
                .build();
    }

    /**
     * Creates a refactorer for the given set of sources.
     * @return a new Refactorer
     */
    public static Refactorer createRefactorer(Source... sources){
        return createRefactorer(
                DEFAULT_CONFIG,
                sources
        );
    }


    /**
     * Creates a refactorer for the given set of sources using
     * {@link Vesper}'s main configuration object.
     *
     * @param configuration The current configuration
     * @param host          The current host.
     * @param sources The list of sources.
     *
     * @return a new Refactorer
     */
    public static Refactorer createRefactorer(
            Configuration configuration,
            Host host, Iterable<Source> sources
    ){

        Vesper.nonNull(configuration, host, sources);


        // installs a configuration to Vesper's host.
        host.install(configuration);

        return new InternalRefactorerCreator(host)
                .addSources(sources)
                .build();
    }


    /**
     * Creates a refactorer for the given set of sources using
     * {@link Vesper}'s main configuration object.
     *
     * @param configuration The current configuration
     * @param sources The list of sources.
     *
     * @return a new Refactorer
     */
    public static Refactorer createRefactorer(
            Configuration configuration,
            Iterable<Source> sources
    ){

        return createRefactorer(
                configuration,
                new HostImpl(),
                sources
        );
    }


    /**
     * Creates a refactorer for the given set of sources using
     * {@link Vesper}'s main configuration object.
     *
     * @param configuration The current configuration
     * @param sources The array of sources.
     *
     * @return a new Refactorer
     */
    public static Refactorer createRefactorer(
            Configuration configuration,
            Source... sources
    ){

        return createRefactorer(
                configuration,
                Arrays.asList(sources)
        );
    }

    /**
     * Default {@code Vesper}'s configuration
     */
    static class DefaultConfiguration extends AbstractConfiguration {
        @Override protected void configure() {
            installDefaultSettings();
        }
    }


    static void nonNull(Configuration configuration, Host host, Iterable<Source> sources) throws
            CreationException {

        final List<Throwable> throwables = new ArrayList<Throwable>();
        if(configuration == null){
            throwables.add(
                    new Throwable("createRefactorer() has been given a null configuration.")
            );
        }

        if(host == null){
            throwables.add(
                    new Throwable("createRefactorer() has been given a null host.")
            );
        }

        if(sources == null){
            throwables.add(
                    new Throwable("createRefactorer() has been given no sources to inspect.")
            );
        }

        if(!throwables.isEmpty()){
            throw new CreationException(throwables);
        }

    }
}
