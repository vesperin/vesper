package edu.ucsc.refactor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import edu.ucsc.refactor.internal.EclipseJavaSnippetParser;
import edu.ucsc.refactor.internal.ProgramUnitLocation;
import edu.ucsc.refactor.internal.SourceVisitor;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.locators.MethodUnit;
import edu.ucsc.refactor.spi.IssueDetector;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import edu.ucsc.refactor.spi.SpaceGeneration;
import edu.ucsc.refactor.spi.graph.DirectedAcyclicGraph;
import edu.ucsc.refactor.spi.graph.DirectedGraph;
import edu.ucsc.refactor.spi.graph.Vertex;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

import static edu.ucsc.refactor.Context.throwCompilationErrorIfExist;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {

  private static final Function<Clip, Integer> LINES_OF_CODE = new Function<Clip, Integer>() {
    @Override public Integer apply(Clip obj) {
      return obj.getSource().getContents().split(System.getProperty("line.separator")).length;
    }
  };

  private static final String WHOLE_CODE = "wholecode";

  private final Host host;

  /**
   * Construct a code introspector.
   *
   * @param host {@code Vesper}'s main {@link Host}.
   */
  public CodeIntrospector(Host host) {
    this.host = Preconditions.checkNotNull(host);
  }


  @Override public Set<Issue> detectIssues(Source code) {
    final Context context = this.host.createContext(code);
    return detectIssues(context);
  }

  @Override public Set<Issue> detectIssues(IssueDetector detector, Context parsedCode) {
    final IssueDetector nonNullDetector = Preconditions.checkNotNull(detector);
    final Context nonNullContext = Preconditions.checkNotNull(parsedCode);
    return nonNullDetector.detectIssues(nonNullContext);
  }

  @Override public Set<Issue> detectIssues(Context context, SourceSelection selection) {
    if (context == null || selection == null) {
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

  @Override public List<Change> detectImprovements(Source code) {
    final Source nonNull = Preconditions.checkNotNull(code);
    return detectImprovements(detectIssues(nonNull));
  }

  @Override public List<Change> detectImprovements(Set<Issue> issues) {
    final List<Change> recommendations = new ArrayList<Change>();
    final Refactorer refactorer = Vesper.createRefactorer();

    for (Issue issue : issues) {
      recommendations.add(refactorer.createChange(ChangeRequest.forIssue(issue)));
    }

    return recommendations;
  }

  @Override public List<String> detectSyntaxErrors(Source code) {
    return ImmutableList.copyOf(
          this.host.createContext(code).getSyntaxRelatedProblems()
    );
  }

  @Override public boolean detectPartialSnippet(Source code) {
    final JavaSnippetParser parser = new EclipseJavaSnippetParser();
    final Context context = new Context(code);
    final ResultPackage pkg = parser.offer(context);
    return pkg.isSnippet()
          && EclipseJavaSnippetParser.isMissingTypeDeclarationUnit(pkg.getParsedNode()
    );
  }

  @Override public Diff differences(Source original, Source revised) {
    return new Diff(original, revised);
  }


  @Override public List<Clip> multiStage(Source code) {
    final ClipSpaceGeneration spaceGeneration = new ClipSpaceGeneration(makeContext(code));
    // The clip space represents a multi stage example; an example split into chunks
    // where each chunk increases the complexity of the code example.
    final Set<Clip> clipSpace = spaceGeneration.generateSpace(code);

    Ordering<Clip> byLinesOfCode =
          Ordering.natural()
                .onResultOf(LINES_OF_CODE);

    return byLinesOfCode.sortedCopy(ImmutableList.copyOf(clipSpace));
  }


  @Override public Map<Clip, List<Location>> summarize(List<Clip> clipSpace, int bound) {
    Map<Clip, List<Location>> result = Maps.newLinkedHashMap();

    for (Clip each : clipSpace) { /// starts from smallest to larger code example

      result.put(each, summarize(each, bound));

    }

    return result;
  }

  @Override public List<Location> summarize(Clip clip, int bound) {
    return summarize(clip.getMethodName(), clip.getSource(), bound);
  }

  @Override public List<Location> summarize(String startingMethod, Source code, int bound) {

    // Note: the null clip; i.e., the one with no starting method.
    // When there's no starting method then summarize code example considering the
    // whole source code and then return result. Otherwise, perform the code below.
    //
    // By doing this, I will be able to show that my technique is novel and interesting.
    final Context context = makeContext(code);
    if(WHOLE_CODE.equals(startingMethod)) { return summarizeWhole(context, bound); } else {

      final MethodDeclaration method = getMethod(startingMethod, context);

      if (method == null) return Lists.newLinkedList();

      final BlockVisitor visitor = new BlockVisitor();
      method.accept(visitor);

      return solveCodeSummarizationProblem(context, visitor.graph(), bound);

    }
  }

  @Override public List<Location> summarize(Source code, int bound) {
    return summarize(WHOLE_CODE, code, bound);
  }

  @Override public Set<String> typesInside(Source code) {
    final Context context = makeContext(code);
    final Set<String> localTypes   = AstUtil.getUsedTypesInCode(context.getCompilationUnit());
    final Set<String> staticTypes  = AstUtil.getUsedStaticTypesInCode(context.getCompilationUnit());
    return Sets.union(localTypes, staticTypes);
  }

  private static List<Location> summarizeWhole(Context context, int bound){
    final BlockVisitor visitor = new BlockVisitor();
    context.accept(visitor);

    return solveCodeSummarizationProblem(context, visitor.graph(), bound);
  }

  private static List<Location> solveCodeSummarizationProblem(
        Context context, DirectedGraph<Item> graph, int bound){

    final List<Location> foldableLocations = solveTreeKnapsack(
          graph,
          bound/*lines of code*/
    );

    // Imports are folded regardless of the previous computation
    final Location foldedImports = foldImportDeclaration(context);

    if (foldedImports != null) {
      foldableLocations.add(foldedImports);
    }

    return foldableLocations;
  }

  /**
   * Adjusts a clip space's shared source and the appropriate folding locations.
   *
   * @param space The summarized clip space.
   * @return adjusted summarized clip space.
   */
  public static Map<Clip, List<Location>> adjustClipspace(Map<Clip, List<Location>> space,
                                                          Source original) {

    final CodePacker packer = new JavaCodePacker();
    final Map<Clip, List<Location>> result = Maps.newLinkedHashMap();
    for (Clip each : space.keySet()) {
      final List<Location> folds = space.get(each);

      final Source adjustedSrc = packer.unpacks(each.getSource(), original);
      final List<Location> adjustedLocs = Locations.adjustLocations(
            folds,
            adjustedSrc
      );

      final Clip adjustedClip = Clip.makeClip(
            each.getMethodName(),
            each.getLabel(),
            adjustedSrc,
            each.isBaseClip()
      );

      result.put(adjustedClip, adjustedLocs);
    }

    return result;
  }


  private static Location foldImportDeclaration(Context context) {
    SourceSelection selection = new SourceSelection();
    // TODO(Huascar) maybe this method should be promoted to main util package; please
    // investigate
    final Set<ImportDeclaration> imports = findImports(context);
    for (ImportDeclaration each : imports) {
      selection.add(Locations.locate(each));
    }

    return !selection.isEmpty() ? selection.toLocation() : null;
  }

  static Set<ImportDeclaration> findImports(Context context) {
    return AstUtil.getUsedImports(context.getCompilationUnit());
  }


  static List<String> findImports(Set<ImportDeclaration> declarations) {
    final List<String> result = Lists.newArrayList();
    for (ImportDeclaration each : declarations) {
      result.add("import " + each.getName().getFullyQualifiedName() + ";");
    }

    return result;
  }

  private static List<Location> solveTreeKnapsack(DirectedGraph<Item> graph, int capacity) {
    final LinkedList<Vertex<Item>> Q = Lists.newLinkedList(graph.getVertices());

    final int N = Q.size();

    // if single Block node, then return empty list
    if(N == 1) return Lists.newArrayList();

    @SuppressWarnings("UnnecessaryLocalVariable")
    final int W = capacity;

    double[] profit = new double[N + 1];
    int[]    weight = new int[N + 1];

    // add vertices values
    for( int n = 1; n <= N; n++){
      profit[n] = graph.getVertex(n - 1).getData().benefit;
      weight[n] = graph.getVertex(n - 1).getData().weight;
    }

    double[][]  opt = new double [N + 1][W + 1];
    boolean[][] sol = new boolean[N + 1][W + 1];

    for(int n = 1; n <= N; n++){
      for(int w = 1; w <= W; w++){
        // don't take item n
        double option1 = opt[n-1][w];

        // take item n
        double option2 = Double.NEGATIVE_INFINITY;
        if (weight[n] <= w) {
          option2 = profit[n] + opt[n-1][w-weight[n]];
        }

        // select better of two options only if there is a precedence relation
        // between item n and n - 1
        opt[n][w] = Math.max(option1, option2);
        sol[n][w] = (option2 > option1)
              && isPrecedenceConstraintMaintained(opt, n, w, graph);
      }
    }

    // determine which items to take
    boolean[] take = new boolean[N+1];
    for (int n = N, w = W; n > 0; n--) {
      if (sol[n][w]) { take[n] = true;  w = w - weight[n]; }
      else           { take[n] = false;                    }
    }

    final Set<Vertex<Item>> keep = Sets.newLinkedHashSet();
    for (int n = 1; n <= N; n++) {
      if (take[n]) {
        keep.add(graph.getVertex(n - 1));
      }
    }

    Q.removeAll(keep);
    final List<Location> locations = Lists.newLinkedList();
    for (Vertex<Item> foldable : Q) {
      locations.add(Locations.locate(foldable.getData().node));
    }

    return locations;
  }


  private static boolean isPrecedenceConstraintMaintained(
        double[][] opt, int i, int j,
        DirectedGraph<Item> graph) {


    final Vertex<Item> parent = graph.getVertex(i - 1);
    final Vertex<Item> child = (graph.size() == i
          ? null
          : graph.getVertex(i)
    );

    // a graph made of a single node implies the following:
    // - the single node is the root
    // - no precedence constraints can be enforced since it has not parent and no children
    final boolean singleNode    = graph.size() == 1;
    final boolean pass          = singleNode && graph.isRootVertex(parent) && child == null;

    return  (pass) || (opt[i][j] != opt[i - 1][j] && parent.hasEdge(child));
  }

  static MethodDeclaration getMethod(String name, Context context) {
    final ProgramUnitLocator locator = new ProgramUnitLocator(context);
    final List<NamedLocation> locations = locator.locate(new MethodUnit(name));

    if (locations.isEmpty()) return null;

    final ProgramUnitLocation target = (ProgramUnitLocation) locations.get(0);
    return (MethodDeclaration) target.getNode();
  }

  static Context makeContext(Source code) {
    final JavaSnippetParser parser = new EclipseJavaSnippetParser();
    final Context context = new Context(code);
    final ResultPackage parsed = parser.offer(context);

    final ASTNode node = parsed.getParsedNode();
    if (node == null) {
      throw new IllegalStateException("Unable to parse source file");
    } else {
      context.setCompilationUnit(AstUtil.getCompilationUnit(node));
    }

    return context;
  }

  static Clip transform(Clip that, ChangeRequest request, boolean isBase) {
    final Refactorer refactorer = Vesper.createRefactorer();
    final Change change = refactorer.createChange(request);
    final Commit commit = refactorer.apply(change);

    if (commit != null && commit.isValidCommit()) {
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

  static Clip cleanup(Clip that) {
    return transform(that, ChangeRequest.optimizeImports(that.getSource()), that.isBaseClip());
  }

  static String capitalize(Iterable<String> words) {
    final StringBuilder builder = new StringBuilder();
    for (String each : words) {
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

    ClipSpaceGeneration(Context context) {
      this.context = context;
    }

    @Override public Set<Clip> generateSpace(Source ofCode) {
      final MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
      final CompilationUnit unit = context.getCompilationUnit();

      unit.accept(visitor);

      final List<MethodDeclaration> methods = visitor.getMethodDeclarations();

      final Set<Clip> space = Sets.newLinkedHashSet();
      final Iterator<MethodDeclaration> itr = methods.iterator();

      while (itr.hasNext()) {
        final MethodDeclaration eachMethod = itr.next();

        // guarantees that only main methods are considered;
        // any methods in closures or anonymous classes will be
        // ignored.
        if (eachMethod.getParent().getNodeType() != ASTNode.TYPE_DECLARATION) {
          continue;
        }

        // guarantees that methods in non-top-level classes (i.e., inner or
        // static nested class) are not crawled by the multi stager.
        if(AstUtil.isClass(eachMethod.getParent())){
          final TypeDeclaration typeDeclarationStatement = AstUtil.exactCast(
                TypeDeclaration.class,
                eachMethod.getParent()
          );
          // thx to http://stackoverflow.com/questions/15699568/extract-inner-classes-using-eclipse-jdt
          if (!typeDeclarationStatement.isPackageMemberTypeDeclaration()) {
            continue; // skip methods in non top level classes
          }
        }


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

          final Clip clip = cleanup(
                Clip.makeClip(
                      eachMethod.getName().getIdentifier(),
                      capitalized,
                      commit.getSourceAfterChange(),
                      !itr.hasNext()
                )
          );

          space.add(clip);
        }
      }

      return space;
    }
  }


  static class BlockVisitor extends SourceVisitor {

    static final Set<ASTNode> VISITED = Sets.newLinkedHashSet();

    final DirectedGraph<Item> G;


    BlockVisitor() {
      G = new DirectedAcyclicGraph<Item>();
    }

    @Override
    public boolean visit(Block node) {

      buildTree(node, G);

      return false;
    }

    static void buildTree(ASTNode node, DirectedGraph<Item> G) {
      final Vertex<Item> root = new Vertex<Item>(node.toString(), Item.of(node));
      if (G.getRootVertex() == null) {
        G.addRootVertex(root);
      } else {
        G.addVertex(root);
      }

      buildSubtree(null, node, G);
    }

    static void buildSubtree(Block parent, ASTNode node, DirectedGraph<Item> G) {
      if (node == null) return;

      final Deque<ASTNode> Q = new LinkedList<ASTNode>();
      Q.offer(node);

      while (!Q.isEmpty()) {
        final ASTNode c = Q.poll();
        VISITED.add(c);

        for (ASTNode child : AstUtil.getChildren(c)) {
          if (!VISITED.contains(child)) {
            if (skipNode(child)) continue;

            if (Block.class.isInstance(child)) {
              connect(G, parent, child);
              buildSubtree((Block) child, child, G);
              Q.offer(child);
            } else {
              parent = parent == null ? (Block) node : parent;

              if(ExpressionStatement.class.isInstance(child)){
                final ExpressionStatement statement = AstUtil.exactCast(ExpressionStatement
                      .class, child);

                final Expression expression = AstUtil.exactCast(Expression.class, statement
                      .getExpression());
                if(MethodInvocation.class.isInstance(expression)){
                  final MethodInvocation methodInvocation = AstUtil.exactCast(MethodInvocation
                        .class, expression);
                  handleMethodInvocation(parent, G, Q, methodInvocation);
                } else {
                  buildSubtree(parent, expression, G);
                  Q.offer(expression);
                }


              } else if (MethodInvocation.class.isInstance(child)) {
                handleMethodInvocation(parent, G, Q, (MethodInvocation) child);
              } else if (isTypeDeclarationStatement(child)) {
                final SimpleType type = (SimpleType) child;
                final ASTNode declaration = AstUtil.findDeclaration(
                      type.resolveBinding(),
                      type
                );

                if (VISITED.contains(declaration)) return;
                buildSubtree(parent, declaration, G);
                Q.offer(declaration);
              } else {
                buildSubtree(parent, child, G);
                Q.offer(child);
              }

            }
          }
        }
      }

    }


    static void handleMethodInvocation(Block parent, DirectedGraph<Item> G, Deque<ASTNode> Q,
                                       MethodInvocation invoke){
      final ASTNode method = AstUtil.findDeclaration(
            invoke.resolveMethodBinding(),
            AstUtil.parent(CompilationUnit.class, invoke)
      );

      if (method == null) {
        final List args = invoke.arguments();
        for (Object arg : args) {
          final ASTNode argNode = (ASTNode) arg;
          if (argNode.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
            final ClassInstanceCreation creation =
                  (ClassInstanceCreation) argNode;
            final AnonymousClassDeclaration anonymousClassDeclaration =
                  creation.getAnonymousClassDeclaration();
            if (anonymousClassDeclaration != null) {
              final List bodyDeclares = anonymousClassDeclaration
                    .bodyDeclarations();
              for (Object eachBodyDeclare : bodyDeclares) {
                final ASTNode eachBodyNode = (ASTNode) eachBodyDeclare;
                buildSubtree(parent, eachBodyNode, G);
                Q.offer(eachBodyNode);
              }

            } else {
              final ASTNode innerClass = AstUtil.findDeclaration(
                    creation.resolveTypeBinding(),
                    AstUtil.parent(CompilationUnit.class, creation)
              );

              if (VISITED.contains(innerClass)) return;
              buildSubtree(parent, innerClass, G);
              Q.offer(innerClass);
            }

          }
        }
      } else {
        if (VISITED.contains(method)) return;
        buildSubtree(parent, method, G);
        Q.offer(method);
      }
    }

    private static boolean isTypeDeclarationStatement(ASTNode node) {
      if (!SimpleType.class.isInstance(node)) return false;

      final SimpleType type = (SimpleType) node;
      final ASTNode declaration = AstUtil.findDeclaration(type
            .resolveBinding(), type);

      if (!TypeDeclaration.class.isInstance(declaration)) return false;

      final TypeDeclaration found = (TypeDeclaration) declaration;
      // todo(Huascar) test whether we handle anonymous class declarations
      return !found.isPackageMemberTypeDeclaration() || found.isMemberTypeDeclaration();
    }


    private static double calculateBenefit(ASTNode/*Block*/ node, int depth) {

      final CompilationUnit root = AstUtil.parent(CompilationUnit.class, node);

      double b = 0;
      for(ASTNode each : AstUtil.getChildren(node)){
        final ElementsVisitor visitor = new ElementsVisitor();
        each.accept(visitor);
        final Set<SimpleName> elements = visitor.nodes;
        for(SimpleName eachName : elements){
          final double size = Math.abs(
                size(AstUtil.findByNode(root, eachName)) - 1 /*declaration*/
          );

          b += size / depth;
        }
      }

      return b;
    }

    private static double size(List<SimpleName> list){
      return (double)Sets.newHashSet(list).size();
    }


    private static boolean isInnerBlock(ASTNode thisBlock, ASTNode thatBlock) {
      return Locations.inside(Locations.locate(thisBlock), Locations.locate(thatBlock));
    }


    private static boolean skipNode(ASTNode node) {
      return (SimpleName.class.isInstance(node) ||
            PrimitiveType.class.isInstance(node));

    }


    private static void connect(DirectedGraph<Item> graph,
                                ASTNode parent, ASTNode child) {

      final Vertex<Item> n = graph.getVertex(parent.toString());

      final Block b = (Block) child;
      Vertex<Item> c = graph.getVertex(b.toString());
      if (c == null) {
        c = new Vertex<Item>(b.toString(), Item.of(b));
      }

      graph.addVertex(n);
      graph.addVertex(c);

      if (!DirectedAcyclicGraph.isDescendantOf(n, c)) {
        graph.addEdge(n, c);

        updateItemValue(n, c);
      }

    }


    private static void updateItemValue(Vertex<Item> from, Vertex<Item> to) {
      // update benefit of the `to` node

      final int depth = to.getData().getDepth();
      to.getData().benefit = to.getData().benefit + calculateBenefit(to.getData().node, depth);


      // update weight of the `from` node
      if (isInnerBlock(from.getData().node, to.getData().node)) {
        final int weightChange = from.getData().weight - to.getData().weight;
        from.getData().weight  = weightChange < 0 ? 0 : weightChange;
      }
    }

    DirectedGraph<Item> graph() {
      return G;
    }
  }

  static class Item {

    final ASTNode node;

    double benefit;
    int weight;

    Item(ASTNode node, int benefit) {
      this.node = node;
      this.benefit = benefit;
      this.weight = this.node == null ? 1 : calculateNumberOfLines(this.node);
    }

    Item(ASTNode node) {
      this(node, 1);
    }

    int getDepth(){
      ASTNode parent = node;
      int depth = 0;
      do {
        parent = parent.getParent();
        if (parent != null) {
          depth ++;
        }
      } while (parent != null);

      return depth;
    }


    static Item of(ASTNode node) {
      return new Item(node);
    }

    private static int calculateNumberOfLines(ASTNode node) {
      final Location location = Locations.locate(node);

      return Math.abs(location.getEnd().getLine() - location.getStart().getLine()) + 1/*inclusive*/;
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


  static class ElementsVisitor extends SourceVisitor {
    final Set<SimpleName> nodes = Sets.newHashSet();
    final Set<ASTNode> visited = Sets.newHashSet();
    @Override public boolean visit(FieldAccess node) {
      nodes.add(node.getName());
      return false;
    }

    @Override public boolean visit(ArrayAccess node) {
      if(!visited.contains(node)){
        visited.add(node);
        node.accept(this);
      }
      return false;
    }

    @Override public boolean visit(MethodInvocation node) {
      nodes.add(node.getName());
      for(Object each : node.arguments()){
        final ASTNode arg = (ASTNode)each;
        visited.add(arg);
        arg.accept(this);
      }
      return false;
    }

    @Override public boolean visit(SuperFieldAccess node) {
      nodes.add(node.getName());
      return false;
    }

    @Override public boolean visit(SuperMethodInvocation node) {
      nodes.add(node.getName());
      return false;
    }


    @Override public boolean visit(SimpleName node) {
      nodes.add(node);
      return false;
    }

    @Override public boolean visit(LabeledStatement node) {
      nodes.add(node.getLabel());
      return false;
    }
  }
}
