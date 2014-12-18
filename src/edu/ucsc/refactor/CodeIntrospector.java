package edu.ucsc.refactor;

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
import edu.ucsc.refactor.spi.graph.GraphUtils;
import edu.ucsc.refactor.spi.graph.Vertex;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

import static edu.ucsc.refactor.Context.throwCompilationErrorIfExist;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {

    private static Map<String, Set<String>> PACKAGES_OF_INTEREST;
    static {
        final Map<String, Set<String>> container = Maps.newHashMap();
        container.put("java.util.zip",                  ImmutableSet.of("Adler32", "CheckedInputStream", "CheckedOutputStream", "CRC32", "Deflater", "DeflaterInputStream", "DeflaterOutputStream", "GZIPInputStream", "GZIPOutputStream", "Inflater", "InflaterInputStream", "InflaterOutputStream", "ZipEntry", "ZipFile", "ZipInputStream", "ZipOutputStream", "DataFormatException", "ZipException", "ZipError"));
        container.put("java.util.spi",                  ImmutableSet.of("CurrencyNameProvider", "LocaleNameProvider", "LocaleServiceProvider", "TimeZoneNameProvider"));
        container.put("java.util.regex",                ImmutableSet.of("MatchResult", "Matcher", "Pattern", "PatternSyntaxException"));
        container.put("java.util.prefs",                ImmutableSet.of("NodeChangeListener", "PreferenceChangeListener", "PreferencesFactory", "AbstractPreferences", "NodeChangeEvent", "PreferenceChangeEvent", "Preferences", "BackingStoreException", "InvalidPreferencesFormatException"));
        container.put("java.util.logging",              ImmutableSet.of("Filter", "LoggingMXBean", "ConsoleHandler", "ErrorManager", "FileHandler", "Formatter", "Handler", "Level", "Logger", "LoggingPermission", "LogManager", "LogRecord", "MemoryHandler", "SimpleFormatter", "SocketHandler", "StreamHandler", "XMLFormatter"));
        container.put("java.util.jar",                  ImmutableSet.of("Pack200.Packer", "Pack200.Unpacker", "Attributes", "Attributes.Name", "JarEntry", "JarFile", "JarInputStream", "JarOutputStream", "Manifest", "Pack200", "JarException"));
        container.put("java.util.concurrent.locks",     ImmutableSet.of("Condition", "Lock", "ReadWriteLock", "AbstractOwnableSynchronizer", "AbstractQueuedLongSynchronizer", "AbstractQueuedSynchronizer", "LockSupport", "ReentrantLock", "ReentrantReadWriteLock", "ReentrantReadWriteLock.ReadLock", "ReentrantReadWriteLock.WriteLock"));
        container.put("java.util.concurrent.atomic",    ImmutableSet.of("AtomicBoolean", "AtomicInteger", "AtomicIntegerArray", "AtomicIntegerFieldUpdater", "AtomicLong", "AtomicLongArray", "AtomicLongFieldUpdater", "AtomicMarkableReference", "AtomicReference", "AtomicReferenceArray", "AtomicReferenceFieldUpdater", "AtomicStampedReference"));
        container.put("java.util.concurrent",           ImmutableSet.of("BlockingDeque", "BlockingQueue", "Callable", "CompletionService", "ConcurrentMap", "ConcurrentNavigableMap", "Delayed", "Executor", "ExecutorService", "Future", "RejectedExecutionHandler", "RunnableFuture", "RunnableScheduledFuture", "ScheduledExecutorService", "ScheduledFuture", "ThreadFactory", "AbstractExecutorService", "ArrayBlockingQueue", "ConcurrentHashMap", "ConcurrentLinkedQueue", "ConcurrentSkipListMap", "ConcurrentSkipListSet", "CopyOnWriteArrayList", "CopyOnWriteArraySet", "CountDownLatch", "CyclicBarrier", "DelayQueue", "Exchanger", "ExecutorCompletionService", "Executors", "FutureTask", "LinkedBlockingDeque", "LinkedBlockingQueue", "PriorityBlockingQueue", "ScheduledThreadPoolExecutor", "Semaphore", "SynchronousQueue", "ThreadPoolExecutor", "ThreadPoolExecutor.AbortPolicy", "ThreadPoolExecutor.CallerRunsPolicy", "ThreadPoolExecutor.DiscardOldestPolicy", "ThreadPoolExecutor.DiscardPolicy", "TimeUnit", "BrokenBarrierException", "CancellationException", "ExecutionException", "RejectedExecutionException", "TimeoutException"));
        container.put("java.util",                      ImmutableSet.of("Collection", "Comparator", "Deque", "Enumeration", "EventListener", "Formattable", "Iterator", "List", "ListIterator", "Map", "Map.Entry", "NavigableMap", "NavigableSet", "Observer", "Queue", "RandomAccess", "Set", "SortedMap", "SortedSet", "AbstractCollection", "AbstractList", "AbstractMap", "AbstractMap.SimpleEntry", "AbstractMap.SimpleImmutableEntry", "AbstractQueue", "AbstractSequentialList", "AbstractSet", "ArrayDeque", "ArrayList", "Arrays", "BitSet", "Calendar", "Collections", "Currency", "Date", "Dictionary", "EnumMap", "EnumSet", "EventListenerProxy", "EventObject", "FormattableFlags", "Formatter", "GregorianCalendar", "HashMap", "HashSet", "Hashtable", "IdentityHashMap", "LinkedHashMap", "LinkedHashSet", "LinkedList", "ListResourceBundle", "Locale", "Observable", "PriorityQueue", "Properties", "PropertyPermission", "PropertyResourceBundle", "Random", "ResourceBundle", "ResourceBundle.Control", "Scanner", "ServiceLoader", "SimpleTimeZone", "Stack", "StringTokenizer", "Timer", "TimerTask", "TimeZone", "TreeMap", "TreeSet", "UUID", "Vector", "WeakHashMap", "Formatter.BigDecimalLayoutForm", "ConcurrentModificationException", "DuplicateFormatFlagsException", "EmptyStackException", "FormatFlagsConversionMismatchException", "FormatterClosedException", "IllegalFormatCodePointException", "IllegalFormatConversionException", "IllegalFormatException", "IllegalFormatFlagsException", "IllegalFormatPrecisionException", "IllegalFormatWidthException", "InputMismatchException", "InvalidPropertiesFormatException", "MissingFormatArgumentException", "MissingFormatWidthException", "MissingResourceException", "NoSuchElementException", "TooManyListenersException", "UnknownFormatConversionException", "UnknownFormatFlagsException", "ServiceConfigurationError"));
        container.put("java.text.spi",                  ImmutableSet.of("BreakIteratorProvider", "CollatorProvider", "DateFormatProvider", "DateFormatSymbolsProvider", "DecimalFormatSymbolsProvider", "NumberFormatProvider"));
        container.put("java.text",                      ImmutableSet.of("AttributedCharacterIterator", "CharacterIterator", "Annotation","AttributedCharacterIterator.Attribute","AttributedString","Bidi","BreakIterator","ChoiceFormat","CollationElementIterator","CollationKey","Collator","DateFormat","DateFormat.Field","DateFormatSymbols","DecimalFormat","DecimalFormatSymbols","FieldPosition","Format","Format.Field","MessageFormat","MessageFormat.Field","Normalizer","NumberFormat","NumberFormat.Field","ParsePosition","RuleBasedCollator","SimpleDateFormat","StringCharacterIterator","Normalizer.Form","ParseException"));
        container.put("java.nio.charset.spi",           ImmutableSet.of("CharsetProvider"));
        container.put("java.nio.charset",               ImmutableSet.of("Charset", "CharsetDecoder", "CharsetEncoder", "CoderResult", "CodingErrorAction", "CharacterCodingException", "IllegalCharsetNameException", "MalformedInputException", "UnmappableCharacterException", "UnsupportedCharsetException", "CoderMalfunctionError"));
        container.put("java.nio.channels.spi",          ImmutableSet.of("AbstractInterruptibleChannel", "AbstractSelectableChannel", "AbstractSelectionKey", "AbstractSelector", "SelectorProvider"));
        container.put("java.nio.channels",              ImmutableSet.of("ByteChannel", "Channel", "GatheringByteChannel", "InterruptibleChannel", "ReadableByteChannel", "ScatteringByteChannel", "WritableByteChannel", "Channels", "DatagramChannel", "FileChannel", "FileChannel.MapMode", "FileLock", "Pipe", "Pipe.SinkChannel", "Pipe.SourceChannel", "SelectableChannel", "SelectionKey", "Selector", "ServerSocketChannel", "SocketChannel", "AlreadyConnectedException", "AsynchronousCloseException", "CancelledKeyException", "ClosedByInterruptException", "ClosedChannelException", "ClosedSelectorException", "ConnectionPendingException", "FileLockInterruptionException", "IllegalBlockingModeException", "IllegalSelectorException", "NoConnectionPendingException", "NonReadableChannelException", "NonWritableChannelException", "NotYetBoundException", "NotYetConnectedException", "OverlappingFileLockException", "UnresolvedAddressException", "UnsupportedAddressTypeException"));
        container.put("java.nio",                       ImmutableSet.of("Buffer", "ByteBuffer", "ByteOrder", "CharBuffer", "DoubleBuffer", "FloatBuffer", "IntBuffer", "LongBuffer", "MappedByteBuffer", "ShortBuffer", "BufferOverflowException", "BufferUnderflowException", "InvalidMarkException", "ReadOnlyBufferException"));
        container.put("java.net",                       ImmutableSet.of("ContentHandlerFactory", "CookiePolicy", "CookieStore", "DatagramSocketImplFactory", "FileNameMap", "SocketImplFactory", "SocketOptions", "URLStreamHandlerFactory", "Authenticator", "CacheRequest", "CacheResponse", "ContentHandler", "CookieHandler", "CookieManager", "DatagramPacket", "DatagramSocket", "DatagramSocketImpl", "HttpCookie", "HttpURLConnection", "IDN", "Inet4Address", "Inet6Address", "InetAddress", "InetSocketAddress", "InterfaceAddress", "JarURLConnection", "MulticastSocket", "NetPermission", "NetworkInterface", "PasswordAuthentication", "Proxy", "ProxySelector", "ResponseCache", "SecureCacheResponse", "ServerSocket", "Socket", "SocketAddress", "SocketImpl", "SocketPermission", "URI", "URL", "URLClassLoader", "URLConnection", "URLDecoder", "URLEncoder", "URLStreamHandler", "Authenticator.RequestorType", "Proxy.Type", "BindException", "ConnectException", "HttpRetryException", "MalformedURLException", "NoRouteToHostException", "PortUnreachableException", "ProtocolException", "SocketException", "SocketTimeoutException", "UnknownHostException", "UnknownServiceException", "URISyntaxException"));
        container.put("java.math",                      ImmutableSet.of("BigDecimal", "BigInteger", "MathContext", "RoundingMode"));
        container.put("java.lang.reflect",              ImmutableSet.of("AnnotatedElement", "GenericArrayType", "GenericDeclaration", "InvocationHandler", "Member", "ParameterizedType", "Type", "TypeVariable", "WildcardType", "AccessibleObject", "Array", "Constructor", "Field", "Method", "Modifier", "Proxy", "ReflectPermission", "InvocationTargetException", "MalformedParameterizedTypeException", "UndeclaredThrowableException", "GenericSignatureFormatError"));
        container.put("java.lang.ref",                  ImmutableSet.of("PhantomReference", "Reference", "ReferenceQueue", "SoftReference", "WeakReference"));
        container.put("java.lang.management",           ImmutableSet.of("ClassLoadingMXBean", "CompilationMXBean", "GarbageCollectorMXBean", "MemoryManagerMXBean", "MemoryMXBean", "MemoryPoolMXBean", "OperatingSystemMXBean", "RuntimeMXBean", "ThreadMXBean", "LockInfo", "ManagementFactory", "ManagementPermission", "MemoryNotificationInfo", "MemoryUsage", "MonitorInfo", "ThreadInfo", "MemoryType"));
        container.put("java.lang.instrument",           ImmutableSet.of("ClassFileTransformer", "Instrumentation", "ClassDefinition", "IllegalClassFormatException", "UnmodifiableClassException"));
        container.put("java.lang.annotation",           ImmutableSet.of("Annotation", "ElementType", "RetentionPolicy", "AnnotationTypeMismatchException", "IncompleteAnnotationException", "AnnotationFormatError", "Documented", "Inherited", "Retention", "Target"));
        container.put("java.io",                        ImmutableSet.of("Closeable", "DataInput", "DataOutput", "Externalizable", "FileFilter", "FilenameFilter", "Flushable", "ObjectInput", "ObjectInputValidation", "ObjectOutput", "ObjectStreamConstants", "Serializable", "BufferedInputStream", "BufferedOutputStream", "BufferedReader", "BufferedWriter", "ByteArrayInputStream", "ByteArrayOutputStream", "CharArrayReader", "CharArrayWriter", "Console", "DataInputStream", "DataOutputStream", "File", "FileDescriptor", "FileInputStream", "FileOutputStream", "FilePermission", "FileReader", "FileWriter", "FilterInputStream", "FilterOutputStream", "FilterReader", "FilterWriter", "InputStream", "InputStreamReader", "LineNumberInputStream", "LineNumberReader", "ObjectInputStream", "ObjectInputStream.GetField", "ObjectOutputStream", "ObjectOutputStream.PutField", "ObjectStreamClass", "ObjectStreamField", "OutputStream", "OutputStreamWriter", "PipedInputStream", "PipedOutputStream", "PipedReader", "PipedWriter", "PrintStream", "PrintWriter", "PushbackInputStream", "PushbackReader", "RandomAccessFile", "Reader", "SequenceInputStream", "SerializablePermission", "StreamTokenizer", "StringBufferInputStream", "StringReader", "StringWriter", "Writer", "CharConversionException", "EOFException", "FileNotFoundException", "InterruptedIOException", "InvalidClassException", "InvalidObjectException", "IOException", "NotActiveException", "NotSerializableException", "ObjectStreamException", "OptionalDataException", "StreamCorruptedException", "SyncFailedException", "UnsupportedEncodingException", "UTFDataFormatException", "WriteAbortedException", "IOError"));

        PACKAGES_OF_INTEREST = Collections.unmodifiableMap(container);
    }



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
        return recommendImports(code);
    }

    @Override public List<Change> detectImprovements(Source code) {
        final Source nonNull = Preconditions.checkNotNull(code);
        return detectImprovements(detectIssues(nonNull));
    }

    @Override public List<Change> detectImprovements(Set<Issue> issues) {
        final List<Change> recommendations = new ArrayList<Change>();
        final Refactorer   refactorer      = Vesper.createRefactorer();

        for(Issue issue : issues){
            recommendations.add(refactorer.createChange(ChangeRequest.forIssue(issue)));
        }

        return recommendations;
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

        final List<Location> foldableLocations = summarizeCodeBySolvingTreeKnapsack(
                visitor.graph(),
                17/*lines of code*/
        );

        // Imports are folded regardless of the previous computation
        final Location foldedImports = foldImportDeclaration(context.getCompilationUnit());

        if(foldedImports != null){
            foldableLocations.add(foldedImports);
        }

        return foldableLocations;

    }

    private static Location foldImportDeclaration(CompilationUnit unit){
        SourceSelection selection = new SourceSelection();
        // TODO(Huascar) maybe this method should be promoted to main util package; please
        // investigate
        final Set<ImportDeclaration> imports = AstUtil.getUsedImports(unit);
        for( ImportDeclaration each : imports){
          selection.add(Locations.locate(each));
        }

        return !selection.isEmpty() ? selection.toLocation() : null;
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

    /**
     * Recommend the required import directives for source to be syntactically correct.
     *
     * @param code The source to be scanned through looking for potential imports to recommend.
     * @return the required import directives.
     */
    static Set<String> recommendImports(Source code){
        final Context context = makeContext(code);

        final Set<String>           types     = AstUtil.getUsedTypesInCode(context.getCompilationUnit());
        final Set<String>           packages  = getJdkPackages();
        final Map<String, Tuple>  freq      = Maps.newHashMap();

        final Set<String> result = Sets.newHashSet();

        // detect used packages
        for(String pkg : packages){
            final Set<String> namespaces =  PACKAGES_OF_INTEREST.get(pkg);
            if(namespaces == null) continue;

            final Set<String> common     = Sets.intersection(namespaces, types);
            if(common.isEmpty()) continue;

            Tuple t;
            if(freq.containsKey(pkg)){
                t = freq.get(pkg).update(common);
                freq.put(pkg, t);
            } else {
                t = new Tuple(common.size(), common);
                freq.put(pkg, t);
            }
        }

        // build package directive
        for(String key : freq.keySet()){
            final Tuple tuple = freq.get(key);
            if(tuple.val >= 5){
                final String wildcard = key + ".*;";
                result.add(wildcard);
            } else {
                for(String typeName : tuple.elements){
                    result.add(key + "." + typeName + ";");
                }
            }
        }

        return result;
    }


    static Set<String> getJdkPackages(){
        final Package[] ps = Package.getPackages();
        final Set<String>  result = Sets.newHashSet();
        for(Package each : ps){
            if(each.getName().contains("sun.") ||
                    each.getName().contains("javax.")
                    || each.getName().contains("org.")) continue;
            result.add(each.getName());
        }

        return result;
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


    private static class Tuple {
        final int val;
        final Set<String> elements;

        Tuple(int val, Set<String> elements){
            this.val      = val;
            this.elements = elements;
        }

        Tuple update(Set<String> seed){
            final Set<String> merged = Sets.union(this.elements, seed);
            final int         freq   = merged.size();

            return new Tuple(freq, merged);
        }
    }

}
