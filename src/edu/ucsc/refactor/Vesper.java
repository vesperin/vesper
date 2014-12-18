package edu.ucsc.refactor;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.internal.HostImpl;
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
 *     final Set<Issue> issues = Vesper.createIntrospector().detectIssues(code);
 *     final List<Change> changes = Recommender.recommendChanges(refactorer, code, issues);
 *     for(Change each : changes){
 *        System.out.println(each.getCause().getName());
 *     }
 *
 *     // perform a single recommended change
 *
 *     final Commit applied = refactor.apply(changes.get(0));
 *     System.out.println(applied.more());
 *
 *     // or handle All recommended changes
 *
 *     List<Change> recommended = Recommender.recommendChanges(refactorer, code, issues);
 *     while(!recommended.isEmpty()){
 *         final Commit applied = refactor.apply(recommended.get(0));
 *         System.out.println(applied.more());
 *         final Set<Issue> badStuff = Vesper.createIntrospector().detectIssues(code);
 *         recommended = Recommender.recommendChanges(refactorer, code, badStuff); // get an updated list of changes
 *     }
 *
 *
 *     // II. Dealing with random edits started by the user
 *
 *     // reformat Source's content
 *
 *     Change reformat = refactorer.createChange(reformatSource(code));
 *     final Commit applied = refactor.apply(reformat);
 *     System.out.println(applied.more());
 *
 *
 *     // III. More?
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
     * @return a new Introspector
     */
    public static Introspector createIntrospector(){
        final Host host     = new HostImpl();
        host.install(DEFAULT_CONFIG);
        return new CodeIntrospector(host);
    }

    /**
     * Returns a locator of any structural unit of a base {@code Source} (class, member, ...).
     * Units exclude the code in a text form. Units include nodes in an AST.
     *
     * @param code The current {@code Source}
     * @return The locator created for this {@code Source}
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    public static UnitLocator createUnitLocator(Source code){
        final Host host     = new HostImpl();
        host.install(DEFAULT_CONFIG);
        final Source src    = Preconditions.checkNotNull(code);
        final Context context = host.createContext(src);
        return createUnitLocator(context);
    }

    /**
     * Returns a locator of any parsed {@code Source} (the context).
     * Units exclude the code in a text form. Units include nodes in an AST.
     *
     * @param context The parsed {@code Source}
     * @return The locator created for this {@code Source}
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    public static UnitLocator createUnitLocator(Context context){
        Preconditions.checkNotNull(context);
        Preconditions.checkArgument(!context.isMalformedContext());
        return new ProgramUnitLocator(context);
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
