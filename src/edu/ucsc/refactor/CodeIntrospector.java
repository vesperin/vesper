package edu.ucsc.refactor;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.internal.*;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import edu.ucsc.refactor.spi.SpaceGeneration;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Recommender;
import edu.ucsc.refactor.util.graph.Graph;
import edu.ucsc.refactor.util.graph.Vertex;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

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

    @Override public Diff differences(Source original, Source revised) {
        return new Diff(original, revised);
    }


    @Override public List<Clip> generateClipSpace(Source code) {
        final ClipSpaceGeneration spaceGeneration = new ClipSpaceGeneration(makeContext(code));
        // The clip space represents a multi stage example; an example split into chunks
        // where each chunk increases the complexity of the code example.
        final Set<Clip> clipSpace = spaceGeneration.generateSpace(code);

        return ImmutableList.copyOf(clipSpace).reverse();
    }

    @Override public Map<Clip, List<Location>> summarizeAllPossibleClips(Source code) {
        final ClipSpaceGeneration spaceGeneration = new ClipSpaceGeneration(makeContext(code));
        final Set<Clip> clipSpace = spaceGeneration.generateSpace(code);

        Map<Clip, List<Location>> result = Maps.newLinkedHashMap();

        for(Clip each : clipSpace){ /// starts from smallest to larger code example

            result.put(each, summarize(each.getMethodName(), each.getSource()));

        }


        return result;
    }

    @Override public List<Location> summarize(String startingMethod, Source code) {

        final Context           context = makeContext(code);
        final MethodDeclaration method  = getMethod(startingMethod, context);

        final BlockVisitor visitor = new BlockVisitor();
        method.accept(visitor);

        System.out.println();
        System.out.println("BEGIN:" + startingMethod);
        System.out.println();
        final Graph<ASTNode> graph = visitor.graph();
        System.out.println(graph);
        System.out.println();
        System.out.println("END");

        return ImmutableList.of();
    }

    private static MethodDeclaration getMethod(String name, Context context){
        final ProgramUnitLocator    locator     = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations   = locator.locate(new MethodUnit(name));
        final ProgramUnitLocation   target      = (ProgramUnitLocation)locations.get(0);
        return (MethodDeclaration)target.getNode();
    }

    @Override public List<String> verifySource(Source code) {
        return ImmutableList.copyOf(
                this.host.createContext(code).getSyntaxRelatedProblems()
        );
    }


    private static Context makeContext(Source code){
        final JavaSnippetParser parser  = new EclipseJavaSnippetParser();
        final Context           context = new Context(code);
        final ResultPackage     parsed  = parser.offer(context);

        final ASTNode node = parsed.getParsedNode();
        if(node == null){
            throw new IllegalStateException("Unable to parse source file");
        } else {
            context.setCompilationUnit(AstUtil.getCompilationUnit(node));
        }

        return context;
    }

    private static Clip transform(Clip that, ChangeRequest request, boolean isBase){
        final Refactorer    refactorer  = Vesper.createRefactorer();
        final Change        change      = refactorer.createChange(request);
        final Commit        commit      = refactorer.apply(change);

        if(commit != null && commit.isValidCommit()){
            return Clip.makeClip(
                    that.getMethodName(),
                    that.getLabel(),
                    commit.getSourceAfterChange(),
                    isBase
            );
        } else {
            return that;
        }
    }

    private static Clip cleanup(Clip that){
        return transform(that, ChangeRequest.optimizeImports(that.getSource()), that.isBaseClip());
    }

    private static Clip format(Clip that){
        return transform(that, ChangeRequest.reformatSource(that.getSource()), that.isBaseClip());
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
        private final Context context;

        ClipSpaceGeneration(Context context){
            this.context = context;
        }

        @Override public Set<Clip> generateSpace(Source ofCode) {
            final MethodDeclarationVisitor visitor  = new MethodDeclarationVisitor();
            final CompilationUnit unit     = context.getCompilationUnit();

            unit.accept(visitor);

            final List<MethodDeclaration> methods = visitor.getMethodDeclarations();

            final Set<Clip>                     space   = Sets.newLinkedHashSet();
            final Iterator<MethodDeclaration>   itr     = methods.iterator();

            while(itr.hasNext()) {
                final MethodDeclaration eachMethod = itr.next();
                final Refactorer refactorer = Vesper.createRefactorer();
                final Location loc = Locations.locate(eachMethod);
                final int startOffset = loc.getStart().getOffset();
                final int endOffset = loc.getEnd().getOffset();

                final SourceSelection selection = new SourceSelection(
                        context.getSource(),
                        startOffset,
                        endOffset
                );


                final ChangeRequest request = ChangeRequest.clipSelection(selection);
                final Change change = refactorer.createChange(request);
                final Commit commit = refactorer.apply(change);

                if (commit != null && commit.isValidCommit()) {

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
                            Clip.makeClip(
                                    eachMethod.getName().getIdentifier(),
                                    capitalized,
                                    commit.getSourceAfterChange(),
                                    !itr.hasNext()
                            )
                    ));

                    space.add(clip);
                }
            }

            return space;
        }
    }


    static class BlockVisitor extends SourceVisitor {

        final Graph<ASTNode>    G;
        final Set<ASTNode>      V;


        BlockVisitor(){
            G = new Graph<ASTNode>();
            V = Sets.newLinkedHashSet();
        }

        @Override public boolean visit(Block node) {
            final Vertex<ASTNode> root  = new Vertex<ASTNode>(node.toString(), node);
            if(G.getRootVertex() == null){ G.addRootVertex(root); } else {
                G.addVertex(root);
            }

            sink(null, node, V, G);

            return false;
        }

        static void sink(Block parent, ASTNode node, Set<ASTNode> visited,
                         Graph<ASTNode> graph){

           final Deque<ASTNode> Q = new LinkedList<ASTNode>();
           Q.offer(node);

           while(!Q.isEmpty()){
              final ASTNode c = Q.poll();
              visited.add(c);

               for(ASTNode child : AstUtil.getChildren(c)){
                   if(!visited.contains(child)){
                       if(skip(child)) continue;

                       if(Block.class.isInstance(child)){
                         update(graph, parent, child);
                         sink((Block)child, child, visited, graph);
                         Q.offer(child);
                       } else {
                         parent = parent == null ? (Block) node : parent;
                         if(MethodInvocation.class.isInstance(child)){
                           final MethodInvocation invoke = (MethodInvocation) child;
                           final ASTNode method = AstUtil.findDeclaration(
                                   invoke.resolveMethodBinding(),
                                   AstUtil.parent(CompilationUnit.class, invoke)
                           );

                           sink(parent, method, visited, graph);
                           Q.offer(method);
                         } else {
                           sink(parent, child, visited, graph);
                           Q.offer(child);
                         }

                       }
                   }

               }

           }

        }


        private static boolean skip(ASTNode node){
            return (SimpleName.class.isInstance(node) ||
                    PrimitiveType.class.isInstance(node));

        }


        private static void update(Graph<ASTNode> graph, ASTNode parent, ASTNode child){
            final Vertex<ASTNode> n = graph.findVertexByName(parent.toString());

            final Block  b = (Block) child;
            Vertex<ASTNode> c = graph.findVertexByName(b.toString());
            if(c == null){
                c = new Vertex<ASTNode>(b.toString(), b);
            }

            graph.addVertex(n);
            graph.addVertex(c);

            if(!graph.isDescendantOf(n, c)) {
                graph.addEdge(n, c, 0);
            }

        }

        Graph<ASTNode> graph() {
            return G;
        }
    }

}
