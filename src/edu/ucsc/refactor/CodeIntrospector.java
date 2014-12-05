package edu.ucsc.refactor;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.internal.EclipseJavaSnippetParser;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import edu.ucsc.refactor.spi.SpaceGeneration;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Recommender;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.ucsc.refactor.Context.throwCompilationErrorIfExist;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {

    private final Host      host;
    private final Context   seedContext;

    /**
     * Construct a code introspector.
     *
     * @param host {@code Vesper}'s main {@link Host}.
     * @param seedContext cached {@code Context}
     */
    public CodeIntrospector(Host host, Context seedContext){
        this.host           = Preconditions.checkNotNull(host);
        this.seedContext = seedContext;
    }

    /**
     * Construct a code introspector.
     *
     * @param host {@code Vesper}'s main {@link Host}.
     */
    public CodeIntrospector(Host host){
        this(host, null);
    }

    @Override public Set<Issue> detectIssues() {
        return detectIssues(seedContext);
    }

    @Override public Set<Issue> detectIssues(Source code) {
        final Context context = this.host.createContext(code);
        return detectIssues(context);
    }

    @Override public Set<Issue> detectIssues(IssueDetector detector, Context parsedCode) {
        final IssueDetector nonNullDetector = Preconditions.checkNotNull(detector);
        final Context       nonNullContext  = Preconditions.checkNotNull(parsedCode);
        return nonNullDetector.detectIssues(nonNullContext);
    }

    @Override public Set<Issue> detectIssues(Context context, SourceSelection selection) {
        if(context == null || selection == null) {
            throw new IllegalArgumentException(
                    "detectIssues() received a null context or a null source selection"
            );
        }

        // syntax related problem are different than code issues; therefore \
        // we should fail fast when encountering them
        throwCompilationErrorIfExist(context);

        context.setScope(selection);

        Set<Issue> issues = new HashSet<Issue>();

        for (IssueDetector detector : this.host.getIssueDetectors()) {
            issues.addAll(detectIssues(detector, context));
        }

        return issues;
    }

    @Override public Set<Issue> detectIssues(Context context) {
        return detectIssues(
                context,
                new SourceSelection(
                        context.getSource(),
                        0,
                        context.getSource().getLength()
                ) // scan whole source code
        );
    }

    @Override public Set<String> detectMissingImports(Source code) {
        return Recommender.recommendImports(code);
    }

    @Override public Diff differences(Source thisSource, Source thatSource) {
        final Source original;
        final Source revised;

        if(thisSource.getContents().length() < thatSource.getContents().length()){
            original = thisSource;
            revised  = thatSource;
        } else {
            original = thatSource;
            revised  = thisSource;
        }

        return new Diff(original, revised);
    }


    @Override public List<Clip> generateClipSpace(Source code) {
        final ClipSpaceGeneration spaceGeneration = new ClipSpaceGeneration();
        // The clip space represents a multi stage example; an example split into chunks
        // where each chunk increases the complexity of the code example.
        final Set<Clip> clipSpace = spaceGeneration.generateSpace(code);

        return ImmutableList.copyOf(clipSpace).reverse();
    }

    @Override public List<String> verifySource(Source code) {
        return ImmutableList.copyOf(
                this.host.createContext(code).getSyntaxRelatedProblems()
        );
    }

    private static Clip transform(Clip that, ChangeRequest request){
        final Refactorer    refactorer  = Vesper.createRefactorer();
        final Change        change      = refactorer.createChange(request);
        final Commit        commit      = refactorer.apply(change);

        if(commit != null && commit.isValidCommit()){
            return Clip.makeClip(that.getLabel(), commit.getSourceAfterChange());
        } else {
            return that;
        }
    }

    private static Clip cleanup(Clip that){
        return transform(that, ChangeRequest.optimizeImports(that.getSource()));
    }

    private static Clip format(Clip that){
        return transform(that, ChangeRequest.reformatSource(that.getSource()));
    }

    private static String capitalize(Iterable<String> words){
        final StringBuilder builder = new StringBuilder();
        for(String each : words){
            builder.append(capitalize(each)).append(" ");
        }

        return builder.toString().trim();
    }

    private static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    static class ClipSpaceGeneration implements SpaceGeneration {
        @Override public Set<Clip> generateSpace(Source ofCode) {
            final JavaSnippetParser parser  = new EclipseJavaSnippetParser();
            final Context           context = new Context(ofCode);
            final ResultPackage     parsed  = parser.offer(context);

            final ASTNode node = parsed.getParsedNode();
            if(node == null){
                throw new IllegalStateException("Unable to parse source file");
            } else {
                context.setCompilationUnit(AstUtil.getCompilationUnit(node));
            }

            final MethodDeclarationVisitor visitor  = new MethodDeclarationVisitor();
            final CompilationUnit unit     = context.getCompilationUnit();

            unit.accept(visitor);

            final List<MethodDeclaration> methods = visitor.getMethodDeclarations();

            final Set<Clip> space = Sets.newLinkedHashSet();
            for(MethodDeclaration eachMethod : methods){
                final Refactorer         refactorer  = Vesper.createRefactorer();
                final Location           loc         = Locations.locate(eachMethod);
                final int                startOffset = loc.getStart().getOffset();
                final int                endOffset   = loc.getEnd().getOffset();

                final SourceSelection    selection = new SourceSelection(
                        context.getSource(),
                        startOffset,
                        endOffset
                );


                final ChangeRequest request = ChangeRequest.clipSelection(selection);
                final Change        change  = refactorer.createChange(request);
                final Commit commit  = refactorer.apply(change);

                if(commit != null && commit.isValidCommit()){

                    final String label = Joiner.on(" ").join(
                            Splitter.onPattern(
                                    // thanks to http://stackoverflow
                                    // .com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
                                    "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)" + "(?=[A-Z][a-z])")
                                    .split(
                                            eachMethod.getName().getIdentifier()
                                    )
                    );

                    final String capitalized = capitalize(Splitter.on(' ').split(label));

                    final Clip clip = format(cleanup(
                            Clip.makeClip(capitalized, commit.getSourceAfterChange())
                    ));

                    space.add(clip);
                }
            }

            return space;
        }
    }
}
