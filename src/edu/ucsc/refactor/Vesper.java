package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.HostImpl;
import edu.ucsc.refactor.internal.InternalRefactorerCreator;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Vesper, in classical latin, means `evening.` However, the reason I named this
 * API like this is because I really like Vesper martinis :-)
 * </p>
 *
 * <p>
 * The following code shows how one can start using the Vesper library.
 * </p>
 *
 * <pre>
 *     import static edu.ucsc.refactor.ChangeRequest.forIssue;
 *     import static edu.ucsc.refactor.ChangeRequest.forEdit;
 *
 *     final Source          code       = new Source(...);
 *     // using your own configuration:
 *     // refactorer = Vesper.createRefactorer(new MyConfiguration(), code);
 *     // using default configuration
 *     final Refactorer      refactorer = Vesper.createRefactorer(code);
 *
 *     final Map<String, Parameter> userInput = ...;
 *
 *     // I. Dealing with issues detected by the Refactorer
 *
 *     // basic usage
 *     if(refactorer.hasIssues(code){
 *        final List<Issue> issues = refactorer.getIssues(code);
 *        for(Issue issue : issues){
 *           final Change  change  = refactorer.createChange(forIssue(issue, userInput));
 *           final CommitRequest applied = refactorer.apply(change);
 *           System.out.println(applied.more()); // prints CommitRequest's data; e.g., successful..
 *        }
 *     }
 *
 *     // a simpler usage
 *
 *     final List<Change> changes = refactorer.recommendChanges(code);
 *     for(Change each : changes){
 *        final CommitRequest applied = refactorer.apply(each);
 *        System.out.println(applied.more());
 *     }
 *
 *     // a much simpler usage
 *
 *     final List<Change> failedOnes = refactorer.apply(changes);
 *     for(Change failed : failedOnes){
 *        System.out.println(failed.more()); // displays the reason why it failed
 *     }
 *
 *     // select a method
 *     final SourceSelection    selection = new SourceSelection(code, 37, 62);
 *
 *     // II. Dealing with random edits from a user (in order)
 *     final List<Change> randomChanges = Arrays.asList(
 *         refactorer.createChange(forEdit(SingleEdit.reformatCode(code))),
 *         refactorer.createChange(forEdit(SingleEdit.renameMethod(selection), userInput)),
 *         refactorer.createChange(forEdit(SingleEdit.renameParameter(selection), userInput)),
 *         refactorer.createChange(forEdit(SingleEdit.deleteMethod(selection)))
 *     );
 *
 *     final List<Change> failedOnes2 = refactorer.apply(randomChanges);
 *     for(Change failed : failedOnes2){
 *        System.out.println(failed.more()); // displays the reason why it failed
 *     }
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
    public static Refactorer createRefactorer(Source... sources){
        return createRefactorer(
                DEFAULT_CONFIG,
                Arrays.asList(sources)
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
     * Default {@code Vesper}'s configuration
     */
    static class DefaultConfiguration extends AbstractConfiguration {
        @Override protected void configure() {
            installDefaultSettings();
        }
    }

    // basic test for Vesper
    public static void main(String[] args) {
        final String content = "import java.util.List; \n"
                + "class Name {\n"
                + "\tvoid boom(String msg){ if(msg.length() > 1) {}}\n"
                + "}";

        final Source        code        = new Source("Name.java", content);
        final Refactorer    refactorer  = Vesper.createRefactorer(code);

        System.out.println("\nfindings...");
        if(refactorer.hasIssues(code)){

            final List<Change> suggestedChanges = refactorer.recommendChanges(code);
            for(Change change : suggestedChanges){
                System.out.println(change);
            }
        }

        System.out.println("...");
    }
}
