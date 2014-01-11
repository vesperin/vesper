package edu.ucsc.refactor.cli;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.spi.CommitRequest;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A very basic CLI Interpreter.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Interpreter {

    static final String WHITESPACE_AND_QUOTES_DELIMITER = " \t\r\n\"(),";
    static final String QUOTES_ONLY_DELIMITER           = "\"";
    static final String DOUBLE_QUOTE                    = "\"";
    static final String NOTHING                         = "";
    static final String VESPER                          = "vesper";


    final AtomicReference<Refactorer> core;
    final AtomicReference<Source>     origin;
    final StringParser parser;

    /**
     * s
     */
    public Interpreter(){
        core   = new AtomicReference<Refactorer>();
        origin = new AtomicReference<Source>();
        parser = new StringParser();
    }

    public Result eval(String statement){
        final Iterable<String> args = parser.parse(statement);
        final String 		   head = args.iterator().next();

        if(!VESPER.equals(head)){
            return Result.failedPackage("Unknown command given");
        }

        try {
            return eval(Iterables.skip(args, 1)/*tail*/);
        } catch (NoSuchElementException ex){
            return Result.failedPackage("Unknown command given");
        }
    }

    Result eval(Iterable<String> args){
        final String 			head = args.iterator().next();
        final Iterable<String> 	tail = Iterables.skip(args, 1);

        switch(VesperCommand.from(head)){
            // assuming we have added a Source and we have an active refactorer
            case ADD:       // add <code_snippet> <file_name_with_extension>
                return fork(tail);
            case TRASH:    // trash <origin|file_name_with_extension>
                return trash(tail);
            case RENAME:
                return rename(tail);
            case DELETE:
                return delete(tail);
            case REFORMAT:
                return reformat(tail);
            default: throw new NoSuchElementException();
        }
    }

    private Result fork(Iterable<String> args){
        ensureValidArgs(args);

        final String head = args.iterator().next();
        final String tail = Iterables.skip(args, 1).iterator().next();


        final Source src  = new Source(tail, head);

        if(origin.compareAndSet(origin.get(), src)){
            core.set(Vesper.createRefactorer(origin.get()));
            return Result.sourcePackage(origin.get());
        }

        return Result.infoPackage(tail + " is already added!");
    }

    private static void ensureValidArgs(Iterable<String> args){
        Preconditions.checkNotNull(args);
        Preconditions.checkArgument(!Iterables.isEmpty(args));
    }

    private static void ensureValidState(Source origin, Refactorer refactorer){
        Preconditions.checkNotNull(origin);
        Preconditions.checkNotNull(refactorer);
    }

    private Result trash(Iterable<String> args){
        ensureValidArgs(args);

        Preconditions.checkArgument(Iterables.size(args) == 1);

        final String head = args.iterator().next(); // origin

        final Source src  = origin.get();

        if("origin".equals(head) || src.getName().equals(head)){
            origin.set(null);
            core.set(null);
            return Result.infoPackage(head + " was removed!");
        }


        return Result.failedPackage(head + "was not found!");
    }


    private Source getOrigin(){
        return origin.get();
    }


    private Refactorer getRefactorer(){
        return core.get();
    }

    private Result rename(Iterable<String> args){
        ensureValidArgs(args);
        ensureValidState(getOrigin(), getRefactorer());

        // param(1,2)
        final String            head = args.iterator().next();
        final Iterable<String>  tail = Iterables.skip(args, 1);

        switch (Member.from(head)){
            case CLASS:
                return renameClassOrInterface(tail);
            case FIELD:
                return renameField(tail);
            case METHOD:
                return renameMethod(tail);
            case PARAMETER:
                return renameParameter(tail);
            default: throw new NoSuchElementException();
        }

    }


    private Result delete(Iterable<String> args){
        ensureValidArgs(args);
        ensureValidState(getOrigin(), getRefactorer());

        // param(1,2)
        final String            head = args.iterator().next();
        final Iterable<String>  tail = Iterables.skip(args, 1);

        switch (Member.from(head)){
            case CLASS:
                return deleteClass(tail);
            case FIELD:
                return deleteField(tail);
            case METHOD:
                return deleteMethod(tail);
            case PARAMETER:
                return deleteParameter(tail);
            default: throw new NoSuchElementException();
        }

    }


    private Result renameClassOrInterface(Iterable<String> args){
        ensureValidArgs(args);
        ensureValidState(getOrigin(), getRefactorer());

        // (1,2) Test
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.skip(args, 1).iterator().next();

        final SourceSelection selection = createSelection(head);

        final ChangeRequest   request   = ChangeRequest.renameClassOrInterface(selection, tail);
        final CommitRequest   applied   = commitChange(request);

        return createResultPackage(applied, "unable to commit rename class change");
    }


    private Result renameParameter(Iterable<String> args){
        ensureValidArgs(args);
        ensureValidState(getOrigin(), getRefactorer());

        // (1,2) newName
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.skip(args, 1).iterator().next();

        final SourceSelection selection = createSelection(head);

        final ChangeRequest   request   = ChangeRequest.renameParameter(selection, tail);
        final CommitRequest   applied   = commitChange(request);

        return createResultPackage(applied, "unable to commit rename parameter change");
    }

    private Result renameField(Iterable<String> args){
        ensureValidArgs(args);
        ensureValidState(getOrigin(), getRefactorer());

        // (1,2) newName
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.skip(args, 1).iterator().next();

        final SourceSelection selection = createSelection(head);

        final ChangeRequest   request   = ChangeRequest.renameField(selection, tail);
        final CommitRequest   applied   = commitChange(request);

        return createResultPackage(applied, "unable to commit rename class change");
    }


    private Result renameMethod(Iterable<String> args){
        ensureValidArgs(args);
        ensureValidState(getOrigin(), getRefactorer());

        // 1:2 newName
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.skip(args, 1).iterator().next();

        final SourceSelection selection = createSelection(head);

        final ChangeRequest   request   = ChangeRequest.renameMethod(selection, tail);
        final CommitRequest   applied   = commitChange(request);

        return createResultPackage(applied, "unable to commit rename method change");
    }


    private Result deleteClass(Iterable<String> args){
        // (1,2)
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.getLast(args, null);

        Preconditions.checkState(tail == null, "found: " + tail + ", expected: " + "");

        final SourceSelection selection = createSelection(head);
        final ChangeRequest   request   = ChangeRequest.deleteClass(selection);

        final CommitRequest   applied   = commitChange(request);
        return createResultPackage(applied, "unable to commit 'delete class' change");

    }


    private Result deleteMethod(Iterable<String> args){
        // (1,2)
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.getLast(args, null);

        Preconditions.checkState(tail == null, "found: " + tail + ", expected: " + "");

        final SourceSelection selection = createSelection(head);
        final ChangeRequest   request   = ChangeRequest.deleteMethod(selection);

        final CommitRequest   applied   = commitChange(request);
        return createResultPackage(applied, "unable to commit 'delete method' change");

    }


    private Result deleteParameter(Iterable<String> args){
        // (1,2)
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.getLast(args, null);

        Preconditions.checkState(tail == null, "found: " + tail + ", expected: " + "");

        final SourceSelection selection = createSelection(head);
        final ChangeRequest   request   = ChangeRequest.deleteParameter(selection);

        final CommitRequest   applied   = commitChange(request);
        return createResultPackage(applied, "unable to commit 'delete parameter' change");

    }


    private Result deleteField(Iterable<String> args){
        // (1,2)
        final String head = args.iterator().next().replace("(", "").replace(")", "");
        final String tail = Iterables.getLast(args, null);

        Preconditions.checkState(tail == null, "found: " + tail + ", expected: " + "");

        final SourceSelection selection = createSelection(head);
        final ChangeRequest   request   = ChangeRequest.deleteField(selection);

        final CommitRequest   applied   = commitChange(request);
        return createResultPackage(applied, "unable to commit 'delete field' change");

    }


    private Result reformat(Iterable<String> args){
        Preconditions.checkNotNull(args);
        ensureValidState(getOrigin(), getRefactorer());

        final int size = Iterables.size(args);
        Preconditions.checkState(size == 0, "reformat command only accepts zero param");

        final ChangeRequest request = ChangeRequest.reformatSource(getOrigin());

        final CommitRequest   applied   = commitChange(request);
        return createResultPackage(applied, "unable to commit rename method change");
    }

    private Result createResultPackage(CommitRequest applied, String message){
        if(applied == null){
            return Result.failedPackage(message);
        }

        return Result.committedPackage(applied);
    }

    private SourceSelection createSelection(String head){
        final Iterable<String> rangeSplit = Splitter.on(',').split(head);

        final int start = Integer.valueOf(rangeSplit.iterator().next());
        final int end   = Integer.valueOf(Iterables.getLast(rangeSplit));

        return new SourceSelection(getOrigin(), start, end);
    }


    private CommitRequest commitChange(ChangeRequest request){
        return getRefactorer().apply(getRefactorer().createChange(request));
    }

    /**
     * Represents a supported Vesper command
     */
    private enum VesperCommand {
        ADD("add"),
        TRASH("trash"),
        RENAME("rename"),
        DELETE("delete"),
        REFORMAT("format"),
        STATUS("status");

        private final String keyword;

        VesperCommand(String keyword){
            this.keyword = keyword;
        }

        public static VesperCommand from(String key){
            for(VesperCommand each : values()){
                if(each.keyword.equals(key)){
                    return each;
                }
            }

            throw new NoSuchElementException();
        }
    }

    private enum Member {
        CLASS("class"),
        PARAMETER("param"),
        FIELD("field"),
        METHOD("method");

        private final String keyword;

        Member(String keyword){
            this.keyword = keyword;
        }

        public static Member from(String key){
            for(Member each : values()){
                if(each.keyword.equals(key)){
                    return each;
                }
            }

            throw new NoSuchElementException();
        }
    }

    /**
     * Parses a string that corresponds to a Vesper command
     */
    private static class StringParser {

        private final StringBuilder parentheses = new StringBuilder();

        Iterable<String> parse(String statement){
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
            if("(".equals(token)){
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
                if("(".equals(left)) {
                    parentheses.append(left);
                } else if(!isParenGone()){
                    parentheses.append(left);
                }
            }
        }

        private String releaseContentInParen(String text){
            if(")".equals(text)){
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