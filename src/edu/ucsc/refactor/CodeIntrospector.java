package edu.ucsc.refactor;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.internal.*;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.locators.MethodUnit;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import edu.ucsc.refactor.spi.SpaceGeneration;
import edu.ucsc.refactor.util.Commit;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.Recommender;
import edu.ucsc.refactor.spi.graph.DirectedAcyclicGraph;
import edu.ucsc.refactor.spi.graph.DirectedGraph;
import edu.ucsc.refactor.spi.graph.GraphUtils;
import edu.ucsc.refactor.spi.graph.Vertex;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

import static edu.ucsc.refactor.Context.throwCompilationErrorIfExist;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {

    private final Host      host;

    /**
     * Construct a code introspector.
     *
     * @param host {@code Vesper}'s main {@link Host}.
     */
    public CodeIntrospector(Host host){
        this.host = Preconditions.checkNotNull(host);
    }


    @Override public List<String> checkCodeSyntax(Source code) {
        return ImmutableList.copyOf(
                this.host.createContext(code).getSyntaxRelatedProblems()
        );
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


    @Override public List<Clip> multiStage(Source code) {
        final ClipSpaceGeneration spaceGeneration = new ClipSpaceGeneration(makeContext(code));
        // The clip space represents a multi stage example; an example split into chunks
        // where each chunk increases the complexity of the code example.
        final Set<Clip> clipSpace = spaceGeneration.generateSpace(code);

        return ImmutableList.copyOf(clipSpace).reverse();
    }

    @Override public Map<Clip, List<Location>> summarize(List<Clip> clipSpace) {
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

        return summarizeCodeBySolvingTreeKnapsack(visitor.graph(), 17/*lines of code*/);
    }


    private static List<Location> summarizeCodeBySolvingTreeKnapsack(DirectedGraph<Item> graph, int capacity){

        final LinkedList<Vertex<Item>> Q = Lists.newLinkedList(graph.getVertices());
        Q.addFirst(new Vertex<Item>()); // required to move the idx to 1


        final int N = Q.size();
        final int W = capacity < 0 ? 0 : capacity;

        int[][]     opt = new int[N][W + 1];
        boolean[][] sol = new boolean[N][W + 1];

        for (int i = 1; i < Q.size(); i++) {
            for (int j = 1; j < W + 1; j++) {

                final Vertex<Item> current = Q.get(i);
                final Item         item    = current.getData();

                if (j - item.weight < 0) {
                    opt[i][j] = opt[i - 1][j];
                } else {
                    final int bi = item.benefit;
                    final int wi = item.weight;

                    if(isPrecedenceConstraintMaintained(opt, i, j, graph) &&
                       opt[i - 1][j - wi] + bi > opt[i-1][j]){

                        opt[i][j] = opt[i - 1][j - wi] + bi;
                        sol[i][j] = true;

                    }
                }
            }
        }

        // determine which items to take

        boolean[] take = new boolean[N];
        for(int idx = 0, w = W; idx < N; idx++){
            if (sol[idx][w]) { take[idx] = true;  w = w - Q.get(idx).getData().weight; }
            else             { take[idx] = false;                                      }
        }

        final Set<Vertex<Item>> keep = Sets.newLinkedHashSet();

        for (int n = 1; n < N; n++) {
            if(take[n]) { keep.add(graph.getVertex(n - 1)); }
        }

        Q.removeFirst();   // remove the null item
        Q.removeAll(keep); // leave the elements that will be folded


        final List<Location> locations = Lists.newLinkedList();
        for(Vertex<Item> foldable : Q){
          locations.add(Locations.locate(foldable.getData().node));
        }

        return locations;
    }


    private static boolean isPrecedenceConstraintMaintained(int[][] opt, int i, int j, DirectedGraph<Item> graph){

        final Vertex<Item> parent = graph.getVertex(i - 1);
        final Vertex<Item> child  = graph.size() == i ? null : graph.getVertex(i);

        if(opt[i][j] != opt[i - 1][j] && parent.hasEdge(child)){
            return true;
        }

        return true;
    }

    static MethodDeclaration getMethod(String name, Context context){
        final ProgramUnitLocator    locator     = new ProgramUnitLocator(context);
        final List<NamedLocation>   locations   = locator.locate(new MethodUnit(name));
        final ProgramUnitLocation   target      = (ProgramUnitLocation)locations.get(0);
        return (MethodDeclaration)target.getNode();
    }

    static Context makeContext(Source code){
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

    static Clip transform(Clip that, ChangeRequest request, boolean isBase){
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

    static Clip cleanup(Clip that){
        return transform(that, ChangeRequest.optimizeImports(that.getSource()), that.isBaseClip());
    }

    static Clip format(Clip that){
        return transform(that, ChangeRequest.reformatSource(that.getSource()), that.isBaseClip());
    }

    static String capitalize(Iterable<String> words){
        final StringBuilder builder = new StringBuilder();
        for(String each : words){
            builder.append(capitalize(each)).append(" ");
        }

        return builder.toString().trim();
    }

    static String capitalize(String s) {
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

        final DirectedGraph<Item> G;
        final Set<ASTNode>   V;


        BlockVisitor(){
            G = new DirectedAcyclicGraph<Item>();
            V = Sets.newLinkedHashSet();
        }

        @Override public boolean visit(Block node) {
            final Vertex<Item> root  = new Vertex<Item>(node.toString(), Item.of(node));
            if(G.getRootVertex() == null){ G.addRootVertex(root); } else {
                G.addVertex(root);
            }

            buildDirectedAcyclicGraph(node, V, G);

            return false;
        }

        static void buildDirectedAcyclicGraph(ASTNode node,
               Set<ASTNode> visited, DirectedGraph<Item> graph){
            sink(null, node, visited, graph);
        }

        static void sink(Block parent, ASTNode node, Set<ASTNode> visited, DirectedGraph<Item> graph){

           final Deque<ASTNode> Q = new LinkedList<ASTNode>();
           Q.offer(node);

           while(!Q.isEmpty()){
              final ASTNode c = Q.poll();
              visited.add(c);

               for(ASTNode child : AstUtil.getChildren(c)){
                   if(!visited.contains(child)){
                       if(skipNode(child)) continue;

                       if(Block.class.isInstance(child)){
                         update(graph, parent, child);
                         sink((Block) child, child, visited, graph);
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


        private static int calculateBenefit(ASTNode/*Block*/ node, int depth){

            final CompilationUnit root = AstUtil.parent(CompilationUnit.class, node);

            int b = 0;
            for(ASTNode each : AstUtil.getChildren(node)){
                final SimpleName name = AstUtil.getSimpleName(each);
                if(name != null){
                    b += (AstUtil.findByNode(root, name).size()/depth);
                }
            }

            return b;
        }


        private static boolean isInnerBlock(ASTNode thisBlock, ASTNode thatBlock){
            return Locations.inside(Locations.locate(thisBlock), Locations.locate(thatBlock));
        }


        private static boolean skipNode(ASTNode node){
            return (SimpleName.class.isInstance(node) ||
                    PrimitiveType.class.isInstance(node));

        }


        private static void update(DirectedGraph<Item> graph, ASTNode parent, ASTNode child){
            final Vertex<Item> n = graph.getVertex(parent.toString());

            final Block  b = (Block) child;
            Vertex<Item> c = graph.getVertex(b.toString());
            if(c == null){
                c = new Vertex<Item>(b.toString(), Item.of(b));
            }

            graph.addVertex(n);
            graph.addVertex(c);

            if(!DirectedAcyclicGraph.isDescendantOf(n, c)) {
                graph.addEdge(n, c);

                updateItemValue(n, c, graph);
            }

        }


        private static void updateItemValue(Vertex<Item> from, Vertex<Item> to, DirectedGraph<Item> graph){
            // update benefit of the `to` node

            final List<Vertex<Item>> nodesAtDepth = ImmutableList.of(graph.getRootVertex());
            final int                depth        = GraphUtils.depth(0, to, nodesAtDepth);

            to.getData().benefit = to.getData().benefit + calculateBenefit(to.getData().node, depth);


            // update weight of the `from` node
            if(isInnerBlock(from.getData().node, to.getData().node)){
                from.getData().weight = from.getData().weight - to.getData().weight;
            }
        }

        DirectedGraph<Item> graph() {
            return G;
        }
    }

    static class Item {

        final ASTNode   node;

        int       benefit;
        int       weight;

        Item(ASTNode node, int benefit){
            this.node       = node;
            this.benefit    = benefit;
            this.weight     = this.node == null ? 1 : calculateNumberOfLines(this.node);
        }

        Item(ASTNode node){
            this(node, 1);
        }


        static Item of(ASTNode node){
            return new Item(node);
        }

        private static int calculateNumberOfLines(ASTNode node){
            final Location location = Locations.locate(node);
            return Math.abs(location.getEnd().getLine() - location.getStart().getLine());
        }

        @Override public int hashCode() {
            return node.hashCode();
        }

        @Override public boolean equals(Object o) {
            return Item.class.isInstance(o) && node.equals(((Item) o).node);
        }

        @Override public String toString() {
            return "Block(benefit:" + benefit + ", weight:" + weight + ")";
        }

    }

}
