package edu.ucsc.refactor.cli;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.spi.CommitRequest;
import edu.ucsc.refactor.spi.CommitStatus;
import edu.ucsc.refactor.util.Notes;
import edu.ucsc.refactor.util.StringUtil;
import io.airlift.airline.*;
import io.airlift.airline.Cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static com.google.common.base.Objects.firstNonNull;
import static io.airlift.airline.OptionType.COMMAND;
import static io.airlift.airline.OptionType.GLOBAL;

/**
 * Vesper's very own CLI; i.e., Chilled
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Chilled {
    static final int EXIT_PERMANENT = 100;

    static Logger logger = null;

    public static void main(String[] args) throws Exception {
        final Interpreter singleCommand = new Interpreter();

        try {
            final Result result = singleCommand.eval(args);
            if(result.isError()){
                printError(result.getErrorMessage());
            } else if (result.isInfo()){
                if(!result.getInfo().isEmpty()){
                    print("= " + result.getInfo() + "\n");
                }
            } else if (result.isIssuesList()){
                final List<Issue> issues = result.getIssuesList();
                for(int i = 1; i <= issues.size(); i++){
                    print(String.valueOf(i) + ". ");
                    print(issues.get(i).getName().getKey() + ".");
                    print("\n");
                }
            } else if(result.isSource()){
                printResult(result.getSource().getContents());
            }
        } catch (Throwable e) {
            System.out.println(firstNonNull(e.getMessage(), "Unknown command line parser error"));
            System.exit(EXIT_PERMANENT);
        }
    }

    /**
     * {@code Vesper CLI}'s global options
     */
    static class GlobalOptions {
        @Option(type = GLOBAL, name = {"-v", "--verbose"}, description = "Verbose mode")
        public boolean verbose = false;

        @Override public String toString() {
            return Objects.toStringHelper("GlobalOptions")
                    .add("verbose", verbose)
                    .toString();
        }
    }


    static class Interpreter {
        final Cli<VesperCommand> parser;
        final Environment        environment;
        final StringReader       reader;

        Interpreter(){
            Cli.CliBuilder<VesperCommand> builder = Cli.<VesperCommand>builder("vesper")
                    .withDescription("the nice CLI for Vesper")
                    .withDefaultCommand(HelpCommand.class)
                    .withCommand(HelpCommand.class)
                    .withCommand(ResetCommand.class)
                    .withCommand(InspectCommand.class)
                    .withCommand(ReplCommand.class)
//                    .withCommand(ConfigCommand.class)
                    .withCommand(AddCommand.class)
//                    .withCommand(OriginShow.class)
//                    .withCommand(RemoveCommand.class)
//                    .withCommand(PublishCommand.class)
                    .withCommand(FormatCommand.class);

            builder.withGroup("rename")
                    .withDescription("Manage set of renaming commands")
                    .withDefaultCommand(RenameClass.class)
                    .withCommand(RenameClass.class)
                    .withCommand(RenameMethod.class)
                    .withCommand(RenameParameter.class)
                    .withCommand(RenameField.class);

            builder.withGroup("chomp")
                    .withDescription("munch noises (irrelevant code) from SOURCE")
                    .withDefaultCommand(ChompClass.class)
                    .withCommand(ChompClass.class)
                    .withCommand(ChompMethod.class)
                    .withCommand(ChompParam.class)
                    .withCommand(ChompField.class);

            builder.withGroup("chop")
                    .withDescription("cut specific code sections from SOURCE")
                    .withDefaultCommand(ChopClass.class)
                    .withCommand(ChopClass.class);

            builder.withGroup("notes")
                    .withDescription("Manage set of notes about SOURCE")
                    .withDefaultCommand(NotesShow.class)
                    .withCommand(NotesShow.class)
                    .withCommand(NoteAdd.class);

            reader      = new StringReader();
            parser      = builder.build();
            environment = new Environment();
        }

        Result evaluateAndReturn(String statement) throws RuntimeException {
            final Iterable<String> args = reader.process(statement);
            if(Iterables.isEmpty(args)){
                return Result.nothing();
            }

            return eval(Iterables.toArray(args, String.class));
        }

        Result eval(String... args) throws RuntimeException {
           return parser.parse(args).call(environment);
        }

        public void clears() {
            environment.clears();
        }

        Environment getEnvironment(){
            return environment;
        }
    }


    static class Environment {

        final AtomicReference<Refactorer>       refactorer;
        final AtomicReference<Source>           origin;
        final AtomicReference<Configuration>    remoteConfig;

        final Queue<CommitRequest>              checkpoints;

        Environment(){
            refactorer      = new AtomicReference<Refactorer>();
            origin          = new AtomicReference<Source>();
            remoteConfig    = new AtomicReference<Configuration>();
            checkpoints     = new LinkedList<CommitRequest>();
        }


        Result unit(){
            return Result.nothing();
        }

        boolean containsOrigin() {
            return this.origin.get() != null;
        }

        void setOrigin(Source origin) {
            updateOrigin(origin);

            if(origin != null){
                final Configuration remote      = remoteConfig.get();
                final Refactorer    refactorer  = remote == null
                        ? Vesper.createRefactorer(getOrigin())
                        : Vesper.createRefactorer(remote, getOrigin());

                this.refactorer.set(refactorer);
            } else {
                this.refactorer.set(null);
            }
        }

        Source getOrigin() {
            return origin.get();
        }

        boolean setCredential(final String username, final String password) {

            return !(Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password))
                    && setCredential(new Credential(username, password));

        }

        boolean setCredential(final Credential credential){
            remoteConfig.set(new AbstractConfiguration() {
                @Override protected void configure() {
                    installDefaultSettings();
                    addCredentials(credential);
                }
            });

            return true;
        }

        Refactorer getRefactorer() {
            return refactorer.get();
        }

        void put(CommitRequest request){
            checkpoints.add(request);
        }


        Queue<CommitRequest> getRequests(){
            return checkpoints;
        }

        void clears() {
            setCredential(null);
            setOrigin(null);
        }

        public void updateOrigin(Source updatedSource) {
            // if origin != null, then refactorer.source === origin
            this.origin.set(updatedSource);
            assert knows(getOrigin());
        }

        /**
         * Checks whether this {@code Refactorer} knows its seed (i.e., the one for which it was
         * created).
         *
         * @param code The The {@code Source}
         * @return {@code true} if it knows, {@code false} otherwise.
         */
        boolean knows(Source code) {
            for(Source each : getRefactorer().getVisibleSources()){
                if(each.equals(code)){ return true; }
            }

            return false;
        }

        public void reset() {
            reset(getOrigin().getName());
        }

        public Source reset(String name) {
            Preconditions.checkNotNull(name);

            final List<Source> all = getRefactorer().getVisibleSources();

            for(Source each : all){
                if(each.getName().equals(name)){

                    final boolean isOrigin = each.equals(getOrigin());

                    final Source to         = getRefactorer().rewind(each);
                    final Source indexed    = getRefactorer().rewrite(to);

                    final boolean isUpdateNeeded = !each.equals(indexed);

                    if(isOrigin && isUpdateNeeded){
                        updateOrigin(indexed);
                    }

                    return each;
                }
            }

            throw new NoSuchElementException("Source with the name = " + name + " was not found.");
        }
    }


    static abstract class VesperCommand {
        @Inject
        public GlobalOptions globalOptions = new GlobalOptions();

        protected AskQuestion askQuestion  = new AskQuestion();


        @VisibleForTesting
        public Result result = null;

        public boolean ask(String question, boolean defaultValue) {
            return askQuestion.ask(question, defaultValue);
        }

        public Result call(Environment environment) throws RuntimeException {
            try {
                initializeLogging(globalOptions.verbose);
                result = execute(environment);
            } catch (Throwable ex){
                if(globalOptions.verbose){
                    throw new RuntimeException(ex);
                } else {
                    return Result.failedPackage(firstNonNull(ex.getMessage(), "Unknown error"));
                }
            }

            return firstNonNull(result, Result.nothing());
        }

        protected CommitRequest commitChange(Environment environment, ChangeRequest request){
            final CommitRequest applied = environment.getRefactorer().apply(environment.getRefactorer().createChange(request));
            environment.updateOrigin(applied.getSource());
            return applied;
        }

        protected static void ensureValidState(Environment environment){
            Preconditions.checkNotNull(environment, "No environment available");
            Preconditions.checkNotNull(environment.getOrigin(), "No source code available");
            Preconditions.checkNotNull(environment.getRefactorer(), "No refactorer available");
        }


        protected Result createResultPackage(CommitRequest applied, String message){
            if(applied == null){
                return Result.failedPackage(message);
            }

            return Result.committedPackage(applied);
        }

        protected SourceSelection createSelection(Environment environment, String head){
            final Iterable<String> rangeSplit = Splitter.on(',').split(head);

            final int start = Integer.valueOf(rangeSplit.iterator().next());
            final int end   = Integer.valueOf(Iterables.getLast(rangeSplit));

            return new SourceSelection(environment.getOrigin(), start, end);
        }


        public abstract Result execute(Environment environment) throws Exception;
    }


    @Command(name = "reset", description = "Reset modified source to its original state")
    public static class ResetCommand extends VesperCommand {
        @Arguments(description = "Reset command parameters")
        public List<String> patterns;

        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            Preconditions.checkArgument((patterns == null) || (patterns.size() == 1));

            if(patterns != null && patterns.size() == 1){
                final Source indexed = environment.reset(patterns.get(0));
                return Result.sourcePackage(indexed);
            } else {
                environment.reset();
                return Result.sourcePackage(environment.getOrigin()); // show the new origin
            }
        }

        @Override public String toString() {
            return Objects.toStringHelper("ResetCommand")
                    .add("params", patterns)
                    .toString();
        }
    }


    static abstract class RenameVesperCommand extends VesperCommand {
        @Arguments(description = "Rename operation parameters")
        public List<String> patterns;

        @Override public Result execute(Environment environment) throws RuntimeException {
            Preconditions.checkNotNull(environment);
            Preconditions.checkNotNull(patterns);
            Preconditions.checkArgument(!patterns.isEmpty());
            Preconditions.checkArgument(patterns.size() == 2);


            // (1,2)-> head
            // newName-> tail
            final String head = patterns.get(0).replace("[", "").replace("]", "");
            final String tail = patterns.get(1);

            final SourceSelection selection = createSelection(environment, head);

            final ChangeRequest request   = createChangeRequest(selection, tail);
            final CommitRequest applied   = commitChange(environment, request);

            return createResultPackage(
                    applied,
                    "unable to commit '"
                            + request.getCauseOfChange().getName().getKey()
                            +  "' change."
            );
        }

        protected abstract ChangeRequest createChangeRequest(SourceSelection selection, String newName);

        @Override public String toString() {
            return Objects.toStringHelper(getClass())
                    .add("params", patterns)
                    .toString();
        }
    }


    static abstract class ChompVesperCommand extends VesperCommand {
        @Arguments(description = "Chomp operation parameters")
        public List<String> patterns;

        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            Preconditions.checkNotNull(patterns);
            Preconditions.checkArgument(!patterns.isEmpty());
            Preconditions.checkArgument(patterns.size() == 1);


            // [1,2]-> head
            final String head = patterns.get(0).replace("[", "").replace("]", "");

            final SourceSelection selection = createSelection(environment, head);

            final ChangeRequest request   = createChangeRequest(selection);
            final CommitRequest applied   = commitChange(environment, request);

            return createResultPackage(applied, "unable to commit 'chomp' change");
        }

        protected abstract ChangeRequest createChangeRequest(SourceSelection selection);

        @Override public String toString() {
            return Objects.toStringHelper(getClass())
                    .add("params", patterns)
                    .toString();
        }
    }

    @Command(name = "help", description = "Display help information about airship")
    public static class HelpCommand extends VesperCommand {
        @Inject public Help help;

        @Override public Result execute(Environment environment) throws RuntimeException {
            help.call();
            return environment.unit(); // nothing to show
        }

        @Override public String toString() {
            return Objects.toStringHelper("HelpCommand")
                    .add("help", help)
                    .toString();
        }
    }


    @Command(name = "add", description = "Add file contents to the index")
    public static class AddCommand extends VesperCommand {
        @Option(type = COMMAND, name = {"-f", "--file"}, description = "Add a file")
        public boolean file = false;

        @Arguments(description = "Patterns of files to be added")
        public List<String> patterns;

        @Override public Result execute(Environment environment) throws RuntimeException {
            Preconditions.checkNotNull(environment);
            Preconditions.checkNotNull(patterns);
            Preconditions.checkArgument(!patterns.isEmpty(), "add... was given no arguments");

            if(file){
                final String path = Preconditions.checkNotNull(Iterables.get(patterns, 0, null));
                final String name = StringUtil.extractName(path);
                final String cont = SourceFileReader.readContent(path);

                return compareAndSet(environment, name + ".java", cont);

            } else {
                final String head    = Preconditions.checkNotNull(Iterables.get(patterns, 0, null));
                final String tail    = Preconditions.checkNotNull(Iterables.get(patterns, 1, null));

                final boolean headWithExt = "java".equals(Files.getFileExtension(head));
                final boolean tailWithExt = "java".equals(Files.getFileExtension(tail));

                final String name       = headWithExt && !tailWithExt ? head : tail;
                final String content    = !headWithExt && tailWithExt ? head : tail;

                return compareAndSet(environment, name, content);
            }
        }

        private Result compareAndSet(Environment environment, String name, String content){
            if(environment.containsOrigin()){
                // ask to continue
                if (!ask("Are you sure you would like to REPLACE the existing SOURCE?", false)) {
                    return Result.nothing();
                }
            }

            environment.setOrigin(
                    new Source(
                            name,
                            content
                    )
            );


            return Result.sourcePackage(environment.getOrigin());
        }
    }

    @Command(name = "remove", description = "Remove file contents to the index")
    public static class RemoveCommand extends VesperCommand {

        @Arguments(description = "Command to execute on the existing Source")
        public String name;

        @Override public Result execute(Environment environment) throws RuntimeException {
            Preconditions.checkNotNull(environment);
            Preconditions.checkNotNull(name);

            if(environment.containsOrigin()){
                if(environment.getOrigin().getName().equals(name) || "origin".equals(name)){
                    // ask to continue
                    if (!ask("Are you sure you would like to REMOVE the existing SOURCE?", false)) {
                        return Result.nothing();
                    }

                    environment.setOrigin(null);

                    return Result.infoPackage(name + " was removed!");
                }
            }

            return Result.infoPackage("There is nothing to remove!");
        }

        @Override public String toString() {
            return Objects.toStringHelper("RemoveCommand")
                    .add("target", name)
                    .toString();
        }
    }


    @Command(name = "config", description = "Configure access to a remote repository")
    public static class ConfigCommand extends VesperCommand {

        static final int USERNAME       = 0;
        static final int USERNAME_VALUE = 1;
        static final int PASSWORD       = 2;
        static final int PASSWORD_VALUE = 3;


        @Arguments(description = "Credentials to a remote repository")
        public List<String> credentials;

        @Override public Result execute(Environment environment) throws RuntimeException {
            Preconditions.checkNotNull(environment);
            Preconditions.checkNotNull(credentials);
            Preconditions.checkState(!credentials.isEmpty());

            Preconditions.checkArgument(credentials.size() == 4);

            Preconditions.checkArgument("username".equals(credentials.get(USERNAME)));
            Preconditions.checkArgument("password".equals(credentials.get(PASSWORD)));


            final String username  = credentials.get(USERNAME_VALUE);
            final String password  = credentials.get(PASSWORD_VALUE);

            final boolean allSet  = environment.setCredential(username, password);

            if(allSet){
                if(globalOptions.verbose){
                    return Result.infoPackage("Ok, credentials have been set!\n");
                }
            }

            return Result.nothing();
        }

        @Override public String toString() {
            return Objects.toStringHelper("ConfigCommand")
                    .add("username", credentials.get(USERNAME_VALUE))
                    .add("password", credentials.get(PASSWORD_VALUE))
                    .toString();
        }
    }


    @Command(name = "repl", description = "Interactive Vesper")
    public static class ReplCommand extends VesperCommand {

        @Option(name = "-c", description = "Enter remote credentials")
        public boolean config = false;

        @Arguments(description = "Interactive Vesper parameters")
        public List<String> patterns;

        @Override public Result execute(Environment environment) throws RuntimeException {
            Preconditions.checkNotNull(environment);

            try {
                Credential credential = null;
                if(config){

                    Preconditions.checkNotNull(patterns);
                    Preconditions.checkArgument(!patterns.isEmpty());
                    Preconditions.checkArgument(patterns.size() == 4, "Unknown parameters");

                    Preconditions.checkArgument("username".equals(patterns.get(0)));
                    Preconditions.checkArgument("password".equals(patterns.get(2)));


                    final String username  = patterns.get(1);
                    final String password  = patterns.get(3);

                    credential = new Credential(username, password);

                }

                if(runRepl(credential, environment)){
                    // todo(Huascar) think of a better strategy of how to return things...
                    System.out.println("quitting ivr. Good bye!");
                    return Result.sourcePackage(environment.getOrigin());
                }

            } catch (Throwable ex){
                throw new RuntimeException(ex);
            }

            return environment.unit();
        }

        private static boolean runRepl(Credential credential, Environment global) throws IOException {
            System.out.println();
            System.out.println("Vesper v0.0.0");
            System.out.println("-----------");
            System.out.println("Type 'q' and press Enter to quit.");


            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);

            AskQuestion quitQuestion  = new AskQuestion();

            Interpreter repl = new Interpreter();

            if(credential != null){
                repl.getEnvironment().setCredential(credential);
            }


            Result result = null;

            while (true) {
                System.out.print("vesper> ");

                String line = in.readLine();

                if (line.equals("q")) {
                    // ask to continue
                    if (!quitQuestion.ask("Are you sure you would like to quit IVR?", false)) {
                        continue;
                    } else {
                        // bubble up changes done in REPL mode to the global
                        // environment (scope), and then clear the local
                        // environment.
                        global.clears();
                        global.setOrigin(repl.getEnvironment().getOrigin());
                        repl.clears();
                        return true; // exiting ivr
                    }
                }

                if (line.equals("help")) {
                    repl.eval("help");
                    continue;
                }

                if(line.equals("repl")){
                    print("Vesper v0.0.0, yeah! that's me.\n");
                    continue; // no need to call it again
                }


                if(line.equals("log")){
                    if(result != null){
                        if(result.isCommitRequest()){
                            printResult(result.getCommitRequest().more());
                        } else if(result.isSource()){
                            printResult(result.getSource().getContents());
                        }
                    }

                    continue;
                }

                try {
                    result = repl.evaluateAndReturn(line);
                } catch (ParseException ex){
                    printError("Unknown command");
                    continue;
                }

                if(result.isError()){
                    printError(result.getErrorMessage());
                } else if (result.isInfo()){
                    if(!result.getInfo().isEmpty()){
                        print("= " + result.getInfo());
                    }
                } else if (result.isIssuesList()){
                    final List<Issue> issues = result.getIssuesList();
                    for(int i = 0; i < issues.size(); i++){
                        print(String.valueOf(i + 1) + ". ");
                        print(issues.get(i).getName().getKey() + ".");
                        print("\n");
                    }
                }  else if(result.isSource()){
                    printResult(result.getSource().getContents());
                }
            }

        }



        @Override public String toString() {
            return Objects.toStringHelper("ReplCommand")
                    .add("config", config)
                    .toString();
        }
    }

    @Command(name = "inspect", description = "Shows the issues in the Source that should be fixed")
    public static class InspectCommand extends VesperCommand {

        @Arguments(description = "Source to inspect")
        public String name;

        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            final boolean inspectOrigin = Strings.isNullOrEmpty(name)
                    || "java".equals(Files.getFileExtension(name));


            List<Issue> issues = Lists.newArrayList();
            if(environment.containsOrigin() && inspectOrigin){
                issues = environment.getRefactorer().getIssues(environment.getOrigin());
                if(issues.isEmpty()){
                    return Result.infoPackage("No issues to show.\n");
                }
            }

            return Result.issuesListPackage(issues);   // maybe we should return only the text of each issue

        }

        @Override public String toString() {
            return Objects.toStringHelper("InspectCommand")
                    .toString();
        }
    }


    @Command(name = "show", description = "Shows all recorded notes")
    public static class NotesShow extends VesperCommand {
        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            final Notes         notes   = environment.getOrigin().getNotes();
            final StringBuilder text    = new StringBuilder();

            for(Note each : notes){
               text.append(each.getContent()).append("\n");
            }

            return Result.infoPackage(text.toString());
        }

        @Override public String toString() {
            return Objects.toStringHelper("NotesShow")
                    .toString();
        }
    }

    @Command(name = "add", description = "Adds a note")
    public static class NoteAdd extends VesperCommand {
        @Arguments(description = "Note to add")
        public String note;

        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            final String noteToAdd = Preconditions.checkNotNull(note);

            environment.getOrigin().addNote(
                    // todo(Huascar) add SourceRange (1, 2) or [1,2]
                    new Note(/*[1,2]*/noteToAdd)
            );

            return environment.unit();
        }
    }


    @Command(name = "class", description = "Renames a class or interface found in the SOURCE")
    public static class RenameClass extends RenameVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
            return ChangeRequest.renameClassOrInterface(selection, newName);
        }
    }


    @Command(name = "method", description = "Renames a method found in the SOURCE")
    public static class RenameMethod extends RenameVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
            return ChangeRequest.renameMethod(selection, newName);
        }
    }


    @Command(name = "param", description = "Renames a parameter found in the SOURCE's method")
    public static class RenameParameter extends RenameVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
            return ChangeRequest.renameParameter(selection, newName);
        }
    }

    @Command(name = "field", description = "Renames a field found in the SOURCE's class")
    public static class RenameField extends RenameVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection, String newName) {
            return ChangeRequest.renameField(selection, newName);
        }
    }


    @Command(name = "publish", description = "Publish all recorded commits")
    public static class PublishCommand extends VesperCommand {
        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            final Queue<CommitRequest> requests = environment.getRequests();
            final Queue<CommitRequest> skipped  = Lists.newLinkedList();

            final StringBuilder details = new StringBuilder();
            while(!requests.isEmpty()){
                final CommitRequest request = requests.remove();
                final CommitStatus  status  = environment.getRefactorer().publish(request).getStatus();

                if(status.isAborted()){
                    skipped.add(request);
                } else {
                    details.append(status.more());
                    if(!requests.isEmpty()){
                        details.append("\n");
                    }
                }
            }

            if(requests.isEmpty() && skipped.isEmpty()){
                return Result.infoPackage(
                        "\nGreat!, all commits have been published. See details:\n"
                                + details.toString()
                );
            } else {
                while(!skipped.isEmpty()){
                    environment.put(skipped.remove());
                }

               return Result.infoPackage(
                       "A total of "
                               + environment.getRequests().size()
                               + " commits were not published. Tried again later."
               );
            }
        }

        @Override public String toString() {
            return Objects.toStringHelper("PublishCommand")
                    .toString();
        }
    }

    @Command(name = "format", description = "Formats the tracked SOURCE")
    public static class FormatCommand extends VesperCommand {
        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);


            final ChangeRequest request = ChangeRequest.reformatSource(environment.getOrigin());
            final CommitRequest applied = commitChange(environment, request);

            return createResultPackage(applied, "unable to commit 'format code' change");
        }

        @Override public String toString() {
            return Objects.toStringHelper("FormatCommand")
                    .toString();
        }
    }

    public static void initializeLogging(boolean debug) throws IOException {
        if (debug) {
            logger = Logger.getLogger(Vesper.class.getPackage().getName());
        }
    }

    @Command(name = "class", description = "Chop a class from the recorded SOURCE")
    public static class ChopClass extends VesperCommand {
        @Arguments(description = "Chop class parameters")
        public List<String> patterns;

        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            Preconditions.checkNotNull(patterns);
            Preconditions.checkArgument(!patterns.isEmpty());
            Preconditions.checkArgument(patterns.size() == 2);

            return Result.infoPackage(
                    "Supported chomping strategies:\n\t\t"
                            + "1. Chop class\n\t\t"
                            + "2. Chop method\n\t\t"
                            + "3. Chop parameter\n\t\t"
                            + "4. Chop field\n"
            );
        }

        @Override public String toString() {
            return Objects.toStringHelper("ChopClass")
                    .toString();
        }
    }

    @Command(name = "class", description = "Remove unused inner class from SOURCE")
    public static class ChompClass extends ChompVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
            return ChangeRequest.deleteClass(selection);
        }
    }


    @Command(name = "method", description = "Remove unused method from SOURCE")
    public static class ChompMethod extends ChompVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
            return ChangeRequest.deleteMethod(selection);
        }
    }

    @Command(name = "param", description = "Remove unused param from SOURCE's method")
    public static class ChompParam extends ChompVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
            return ChangeRequest.deleteParameter(selection);
        }
    }


    @Command(name = "field", description = "Remove unused param from SOURCE's method")
    public static class ChompField extends ChompVesperCommand {
        @Override protected ChangeRequest createChangeRequest(SourceSelection selection) {
            return ChangeRequest.deleteField(selection);
        }
    }

    @Command(name = "show", description = "Shows the current SOURCE")
    public static class OriginShow extends VesperCommand {
        @Override public Result execute(Environment environment) throws RuntimeException {
            ensureValidState(environment);

            if(!environment.containsOrigin()){
                return Result.nothing();
            }

            return Result.sourcePackage(environment.getOrigin());
        }

        @Override public String toString() {
            return Objects.toStringHelper("OriginShow")
                    .toString();
        }
    }


    private static class AskQuestion {
        boolean ask(String question, boolean defaultValue){
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            try {
                while (true) {
                    System.out.print(question + (defaultValue ? " [Y/n] " : " [y/N] "));
                    String line = null;
                    try {
                        line = reader.readLine();
                    } catch (IOException ignored) {
                    }

                    if (line == null) {
                        throw new IllegalArgumentException("Error reading from standard in");
                    }

                    line = line.trim().toLowerCase();
                    if (line.isEmpty()) {
                        return defaultValue;
                    }
                    if ("y".equalsIgnoreCase(line) || "yes".equalsIgnoreCase(line)) {
                        return true;
                    }
                    if ("n".equalsIgnoreCase(line) || "no".equalsIgnoreCase(line)) {
                        return false;
                    }
                }
            } finally {
                System.out.println();
            }
        }
    }

    static void print(String text) {
        System.out.print(text);
    }


    static void printResult(String result) {
        System.out.print("= ");
        System.out.println(result);
    }

    static void printError(String message) {
        System.out.println("! " + message);
    }


    /**
     * Parses a string that corresponds to a Vesper command
     */
    private static class StringReader {

        static final String WHITESPACE_AND_QUOTES_DELIMITER = " \t\r\n\"[],";
        static final String QUOTES_ONLY_DELIMITER           = "\"";
        static final String DOUBLE_QUOTE                    = "\"";
        static final String NOTHING                         = "";

        private final StringBuilder parentheses = new StringBuilder();

        Iterable<String> process(String statement){
            final Set<String> result = new LinkedHashSet<String>();

            String  delimiter = WHITESPACE_AND_QUOTES_DELIMITER;

            final StringTokenizer parser = new StringTokenizer(
                    statement,
                    delimiter,
                    true/*=>return Tokens*/
            );

            while (parser.hasMoreTokens()) {

                String token = process(parser.nextToken(delimiter), delimiter, parser);

                if(!isDoubleQuote(token)){ addNonTrivialWordToResult(token, result); } else {
                    delimiter = flipDelimiters(delimiter);
                }
            }


            return result;
        }

        private String process(String token, String delimiter, StringTokenizer parser){
            String curatedToken;
            if("[".equals(token)){
                cacheContentInParen(token);
                do {
                    token = parser.nextToken(delimiter);
                    cacheContentInParen(token);
                    curatedToken = releaseContentInParen(token);
                } while(!isParenGone() && curatedToken == null);
                return curatedToken;
            } else {
                return token;
            }


        }

        private boolean isParenGone(){
            return parentheses.toString().isEmpty();
        }

        private void cacheContentInParen(String left){
            if(textHasContent(left)){
                if("[".equals(left)) {
                    parentheses.append(left);
                } else if(!isParenGone()){
                    parentheses.append(left);
                }
            }
        }

        private String releaseContentInParen(String text){
            if("]".equals(text)){
                return popContent();
            }

            return null;
        }

        private String popContent(){
            return parentheses.toString();
        }

        private String flipDelimiters(String currentDelimiter){
            return (currentDelimiter.equals(WHITESPACE_AND_QUOTES_DELIMITER)
                    ? QUOTES_ONLY_DELIMITER
                    : WHITESPACE_AND_QUOTES_DELIMITER
            );
        }

        private boolean isDoubleQuote(String token){
            return token.equals(DOUBLE_QUOTE);
        }


        private boolean textHasContent(String text){
            return (text != null) && (!text.trim().equals(NOTHING));
        }

        private void addNonTrivialWordToResult(String token, Set<String> result){
            if (textHasContent(token)) {
                result.add(token.trim());
            }
        }
    }

}