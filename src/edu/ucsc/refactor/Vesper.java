package edu.ucsc.refactor;

import edu.ucsc.refactor.internal.HostImpl;
import edu.ucsc.refactor.internal.InternalRefactorerCreator;
import edu.ucsc.refactor.util.Locations;

import java.util.ArrayList;
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
 *        break; // the rest of changes in list of changes are outdated; please re-run recommendChanges
 *     }
 *
 *
 *     // select a method
 *     final SourceSelection    selection = new SourceSelection(code, 37, 62);
 *
 *     // II. Dealing with random edits from a user
 *
 *     Change reformat = refactorer.createChange(forEdit(SingleEdit.reformatCode(code)));
 *     System.out.println(refactorer.apply(reformat).more());
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

    // basic test for Vesper
    public static void main(String[] args) {
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = \"Hi!\";\n"
                + "\tString boom(String msg){ if(null != msg) { return boom(null);} "
                + "return \"Hi!\";}\n"
                + "\t/** {@link Name#boom(String)}**/String baam(String msg){ this.msg = msg "
                + "+ (msg+this.msg); return boom(this.msg); }\n"
                + "}";

        final Source        code        = new Source("Name.java", content);
        final Refactorer    refactorer  = Vesper.createRefactorer(code);


        System.out.println("\nfindings...");

        final SourceSelection   selection   = new SourceSelection(Locations.locateWord(code, "boom"));
        final Change            renamed     = refactorer.createChange(
                ChangeRequest.renameMethod(selection, "print")
        );

        System.out.println(renamed.more());


        final Change            renamedParam    = refactorer.createChange(
                ChangeRequest.renameParameter(new SourceSelection(code, 218, 221)/*msg*/, "ooo")
        );

        System.out.println(renamedParam.more());

        final Change            renameField    = refactorer.createChange(
                ChangeRequest.renameField(new SourceSelection(code, 74, 77)/*msg*/, "ooo")
        );

        System.out.println(renameField.more());


        final Change reformat = refactorer.createChange(ChangeRequest.reformatSource(code));
        System.out.println(reformat.more());

        System.out.println("...");
    }
}
