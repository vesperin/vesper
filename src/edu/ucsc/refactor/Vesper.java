package edu.ucsc.refactor;

import com.google.common.collect.ImmutableList;
import edu.ucsc.refactor.internal.HostImpl;
import edu.ucsc.refactor.internal.InternalCheckpointedRefactorerCreator;
import edu.ucsc.refactor.internal.InternalRefactorerCreator;

import java.util.ArrayList;
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
 *     // IV. Retrieving the SourceHistory of a Source (previously curated)
 *
 *     final SourceRecalling recalling = new SourceRecalling(new Upstream(...), "123456");
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
     * Creates a refactorer for the given set of sources.
     * @return a new Refactorer
     */
    public static Refactorer createRefactorer(){
        return createRefactorer(DEFAULT_CONFIG);
    }


    /**
     * Creates a refactorer for the given set of sources using
     * {@link Vesper}'s main configuration object.
     *
     * @param configuration The current configuration
     *
     * @return a new Refactorer
     */
    public static Refactorer createRefactorer(Configuration configuration) {
        return createRefactorer(
                configuration,
                new HostImpl()
        );
    }


    /**
     * Creates a refactorer for the given set of sources using
     * {@link Vesper}'s main configuration object.
     *
     * @param configuration The current configuration
     * @param host          The current host.
     *
     * @return a new Refactorer
     */
    public static Refactorer createRefactorer(
            Configuration configuration,
            Host host
    ){

        Vesper.nonNull(configuration, host);


        // installs a configuration to Vesper's host.
        host.install(configuration);

        return new InternalRefactorerCreator(host)
                .build();
    }

    /**
     * Creates a checkpointed refactorer for the given array of sources
     *
     * @param sources The array of sources.
     *
     * @return a new Refactorer
     */
    public static CheckpointedRefactorer createCheckpointedRefactorer(Source... sources){
        return Vesper.createCheckpointedRefactorer(Vesper.createRefactorer(), sources);
    }

    /**
     * Creates a checkpointed refactorer for the given array of sources
     *
     * @param refactorer The plain refactorer
     * @param sources The array of sources.
     *
     * @return a new Refactorer
     */
    public static CheckpointedRefactorer createCheckpointedRefactorer(Refactorer refactorer, Source... sources){
        final ImmutableList<Source> seed = ImmutableList.copyOf(sources);

        if(seed.contains(null)) {
            throw new CreationException(
                    ImmutableList.of(new Throwable("createRefactorer() has been given a null configuration."))
            );
        }

        return Vesper.createCheckpointedRefactorer(refactorer, seed);
    }

    /**
     * Creates a checkpointed refactorer from a plain refactorer and for the given array of sources
     *
     * @param refactorer The plain refactorer
     * @param sources The array of sources.
     *
     * @return a new Refactorer
     */
    private static CheckpointedRefactorer createCheckpointedRefactorer(Refactorer refactorer, Iterable<Source> sources){
        return new InternalCheckpointedRefactorerCreator(refactorer)
                .addSources(sources)
                .build();
    }

    /**
     * Default {@code Vesper}'s configuration
     */
    static class DefaultConfiguration extends AbstractConfiguration {
        @Override protected void configure() {
            installDefaultSettings();
        }
    }


    static void nonNull(Configuration configuration, Host host) throws
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

        if(!throwables.isEmpty()){
            throw new CreationException(throwables);
        }

    }
}
