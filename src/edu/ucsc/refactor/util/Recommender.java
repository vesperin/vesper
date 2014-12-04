package edu.ucsc.refactor.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.EclipseJavaSnippetParser;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.MethodDeclarationVisitor;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import edu.ucsc.refactor.spi.RankingStrategy;
import edu.ucsc.refactor.spi.SpaceGeneration;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Recommender {
    private static Map<String, Set<String>> PACKAGES_OF_INTEREST;
    static {
        final Map<String, Set<String>> container = Maps.newHashMap();
        container.put("java.util.zip",          ImmutableSet.of("Adler32", "CheckedInputStream", "CheckedOutputStream", "CRC32", "Deflater", "DeflaterInputStream", "DeflaterOutputStream", "GZIPInputStream", "GZIPOutputStream", "Inflater", "InflaterInputStream", "InflaterOutputStream", "ZipEntry", "ZipFile", "ZipInputStream", "ZipOutputStream", "DataFormatException", "ZipException", "ZipError"));
        container.put("java.util.spi",          ImmutableSet.of("CurrencyNameProvider", "LocaleNameProvider", "LocaleServiceProvider", "TimeZoneNameProvider"));
        container.put("java.util.regex",        ImmutableSet.of("MatchResult", "Matcher", "Pattern", "PatternSyntaxException"));
        container.put("java.util.prefs",        ImmutableSet.of("NodeChangeListener", "PreferenceChangeListener", "PreferencesFactory", "AbstractPreferences", "NodeChangeEvent", "PreferenceChangeEvent", "Preferences", "BackingStoreException", "InvalidPreferencesFormatException"));
        container.put("java.util.logging",      ImmutableSet.of("Filter", "LoggingMXBean", "ConsoleHandler", "ErrorManager", "FileHandler", "Formatter", "Handler", "Level", "Logger", "LoggingPermission", "LogManager", "LogRecord", "MemoryHandler", "SimpleFormatter", "SocketHandler", "StreamHandler", "XMLFormatter"));
        container.put("java.util.jar",          ImmutableSet.of("Pack200.Packer", "Pack200.Unpacker", "Attributes", "Attributes.Name", "JarEntry", "JarFile", "JarInputStream", "JarOutputStream", "Manifest", "Pack200", "JarException"));
        container.put("java.util.concurrent.locks",     ImmutableSet.of("Condition", "Lock", "ReadWriteLock", "AbstractOwnableSynchronizer", "AbstractQueuedLongSynchronizer", "AbstractQueuedSynchronizer", "LockSupport", "ReentrantLock", "ReentrantReadWriteLock", "ReentrantReadWriteLock.ReadLock", "ReentrantReadWriteLock.WriteLock"));
        container.put("java.util.concurrent.atomic",    ImmutableSet.of("AtomicBoolean", "AtomicInteger", "AtomicIntegerArray", "AtomicIntegerFieldUpdater", "AtomicLong", "AtomicLongArray", "AtomicLongFieldUpdater", "AtomicMarkableReference", "AtomicReference", "AtomicReferenceArray", "AtomicReferenceFieldUpdater", "AtomicStampedReference"));
        container.put("java.util.concurrent",   ImmutableSet.of("BlockingDeque", "BlockingQueue", "Callable", "CompletionService", "ConcurrentMap", "ConcurrentNavigableMap", "Delayed", "Executor", "ExecutorService", "Future", "RejectedExecutionHandler", "RunnableFuture", "RunnableScheduledFuture", "ScheduledExecutorService", "ScheduledFuture", "ThreadFactory", "AbstractExecutorService", "ArrayBlockingQueue", "ConcurrentHashMap", "ConcurrentLinkedQueue", "ConcurrentSkipListMap", "ConcurrentSkipListSet", "CopyOnWriteArrayList", "CopyOnWriteArraySet", "CountDownLatch", "CyclicBarrier", "DelayQueue", "Exchanger", "ExecutorCompletionService", "Executors", "FutureTask", "LinkedBlockingDeque", "LinkedBlockingQueue", "PriorityBlockingQueue", "ScheduledThreadPoolExecutor", "Semaphore", "SynchronousQueue", "ThreadPoolExecutor", "ThreadPoolExecutor.AbortPolicy", "ThreadPoolExecutor.CallerRunsPolicy", "ThreadPoolExecutor.DiscardOldestPolicy", "ThreadPoolExecutor.DiscardPolicy", "TimeUnit", "BrokenBarrierException", "CancellationException", "ExecutionException", "RejectedExecutionException", "TimeoutException"));
        container.put("java.util",              ImmutableSet.of("Collection", "Comparator", "Deque", "Enumeration", "EventListener", "Formattable", "Iterator", "List", "ListIterator", "Map", "Map.Entry", "NavigableMap", "NavigableSet", "Observer", "Queue", "RandomAccess", "Set", "SortedMap", "SortedSet", "AbstractCollection", "AbstractList", "AbstractMap", "AbstractMap.SimpleEntry", "AbstractMap.SimpleImmutableEntry", "AbstractQueue", "AbstractSequentialList", "AbstractSet", "ArrayDeque", "ArrayList", "Arrays", "BitSet", "Calendar", "Collections", "Currency", "Date", "Dictionary", "EnumMap", "EnumSet", "EventListenerProxy", "EventObject", "FormattableFlags", "Formatter", "GregorianCalendar", "HashMap", "HashSet", "Hashtable", "IdentityHashMap", "LinkedHashMap", "LinkedHashSet", "LinkedList", "ListResourceBundle", "Locale", "Observable", "PriorityQueue", "Properties", "PropertyPermission", "PropertyResourceBundle", "Random", "ResourceBundle", "ResourceBundle.Control", "Scanner", "ServiceLoader", "SimpleTimeZone", "Stack", "StringTokenizer", "Timer", "TimerTask", "TimeZone", "TreeMap", "TreeSet", "UUID", "Vector", "WeakHashMap", "Formatter.BigDecimalLayoutForm", "ConcurrentModificationException", "DuplicateFormatFlagsException", "EmptyStackException", "FormatFlagsConversionMismatchException", "FormatterClosedException", "IllegalFormatCodePointException", "IllegalFormatConversionException", "IllegalFormatException", "IllegalFormatFlagsException", "IllegalFormatPrecisionException", "IllegalFormatWidthException", "InputMismatchException", "InvalidPropertiesFormatException", "MissingFormatArgumentException", "MissingFormatWidthException", "MissingResourceException", "NoSuchElementException", "TooManyListenersException", "UnknownFormatConversionException", "UnknownFormatFlagsException", "ServiceConfigurationError"));
        container.put("java.text.spi",          ImmutableSet.of("BreakIteratorProvider", "CollatorProvider", "DateFormatProvider", "DateFormatSymbolsProvider", "DecimalFormatSymbolsProvider", "NumberFormatProvider"));
        container.put("java.text",              ImmutableSet.of("AttributedCharacterIterator", "CharacterIterator", "Annotation","AttributedCharacterIterator.Attribute","AttributedString","Bidi","BreakIterator","ChoiceFormat","CollationElementIterator","CollationKey","Collator","DateFormat","DateFormat.Field","DateFormatSymbols","DecimalFormat","DecimalFormatSymbols","FieldPosition","Format","Format.Field","MessageFormat","MessageFormat.Field","Normalizer","NumberFormat","NumberFormat.Field","ParsePosition","RuleBasedCollator","SimpleDateFormat","StringCharacterIterator","Normalizer.Form","ParseException"));
        container.put("java.nio.charset.spi",   ImmutableSet.of("CharsetProvider"));
        container.put("java.nio.charset",       ImmutableSet.of("Charset", "CharsetDecoder", "CharsetEncoder", "CoderResult", "CodingErrorAction", "CharacterCodingException", "IllegalCharsetNameException", "MalformedInputException", "UnmappableCharacterException", "UnsupportedCharsetException", "CoderMalfunctionError"));
        container.put("java.nio.channels.spi",  ImmutableSet.of("AbstractInterruptibleChannel", "AbstractSelectableChannel", "AbstractSelectionKey", "AbstractSelector", "SelectorProvider"));
        container.put("java.nio.channels",      ImmutableSet.of("ByteChannel", "Channel", "GatheringByteChannel", "InterruptibleChannel", "ReadableByteChannel", "ScatteringByteChannel", "WritableByteChannel", "Channels", "DatagramChannel", "FileChannel", "FileChannel.MapMode", "FileLock", "Pipe", "Pipe.SinkChannel", "Pipe.SourceChannel", "SelectableChannel", "SelectionKey", "Selector", "ServerSocketChannel", "SocketChannel", "AlreadyConnectedException", "AsynchronousCloseException", "CancelledKeyException", "ClosedByInterruptException", "ClosedChannelException", "ClosedSelectorException", "ConnectionPendingException", "FileLockInterruptionException", "IllegalBlockingModeException", "IllegalSelectorException", "NoConnectionPendingException", "NonReadableChannelException", "NonWritableChannelException", "NotYetBoundException", "NotYetConnectedException", "OverlappingFileLockException", "UnresolvedAddressException", "UnsupportedAddressTypeException"));
        container.put("java.nio",               ImmutableSet.of("Buffer", "ByteBuffer", "ByteOrder", "CharBuffer", "DoubleBuffer", "FloatBuffer", "IntBuffer", "LongBuffer", "MappedByteBuffer", "ShortBuffer", "BufferOverflowException", "BufferUnderflowException", "InvalidMarkException", "ReadOnlyBufferException"));
        container.put("java.net",               ImmutableSet.of("ContentHandlerFactory", "CookiePolicy", "CookieStore", "DatagramSocketImplFactory", "FileNameMap", "SocketImplFactory", "SocketOptions", "URLStreamHandlerFactory", "Authenticator", "CacheRequest", "CacheResponse", "ContentHandler", "CookieHandler", "CookieManager", "DatagramPacket", "DatagramSocket", "DatagramSocketImpl", "HttpCookie", "HttpURLConnection", "IDN", "Inet4Address", "Inet6Address", "InetAddress", "InetSocketAddress", "InterfaceAddress", "JarURLConnection", "MulticastSocket", "NetPermission", "NetworkInterface", "PasswordAuthentication", "Proxy", "ProxySelector", "ResponseCache", "SecureCacheResponse", "ServerSocket", "Socket", "SocketAddress", "SocketImpl", "SocketPermission", "URI", "URL", "URLClassLoader", "URLConnection", "URLDecoder", "URLEncoder", "URLStreamHandler", "Authenticator.RequestorType", "Proxy.Type", "BindException", "ConnectException", "HttpRetryException", "MalformedURLException", "NoRouteToHostException", "PortUnreachableException", "ProtocolException", "SocketException", "SocketTimeoutException", "UnknownHostException", "UnknownServiceException", "URISyntaxException"));
        container.put("java.math",              ImmutableSet.of("BigDecimal", "BigInteger", "MathContext", "RoundingMode"));
        container.put("java.lang.reflect",      ImmutableSet.of("AnnotatedElement", "GenericArrayType", "GenericDeclaration", "InvocationHandler", "Member", "ParameterizedType", "Type", "TypeVariable", "WildcardType", "AccessibleObject", "Array", "Constructor", "Field", "Method", "Modifier", "Proxy", "ReflectPermission", "InvocationTargetException", "MalformedParameterizedTypeException", "UndeclaredThrowableException", "GenericSignatureFormatError"));
        container.put("java.lang.ref",          ImmutableSet.of("PhantomReference", "Reference", "ReferenceQueue", "SoftReference", "WeakReference"));
        container.put("java.lang.management",   ImmutableSet.of("ClassLoadingMXBean", "CompilationMXBean", "GarbageCollectorMXBean", "MemoryManagerMXBean", "MemoryMXBean", "MemoryPoolMXBean", "OperatingSystemMXBean", "RuntimeMXBean", "ThreadMXBean", "LockInfo", "ManagementFactory", "ManagementPermission", "MemoryNotificationInfo", "MemoryUsage", "MonitorInfo", "ThreadInfo", "MemoryType"));
        container.put("java.lang.instrument",   ImmutableSet.of("ClassFileTransformer", "Instrumentation", "ClassDefinition", "IllegalClassFormatException", "UnmodifiableClassException"));
        container.put("java.lang.annotation",   ImmutableSet.of("Annotation", "ElementType", "RetentionPolicy", "AnnotationTypeMismatchException", "IncompleteAnnotationException", "AnnotationFormatError", "Documented", "Inherited", "Retention", "Target"));
        container.put("java.io",                ImmutableSet.of("Closeable", "DataInput", "DataOutput", "Externalizable", "FileFilter", "FilenameFilter", "Flushable", "ObjectInput", "ObjectInputValidation", "ObjectOutput", "ObjectStreamConstants", "Serializable", "BufferedInputStream", "BufferedOutputStream", "BufferedReader", "BufferedWriter", "ByteArrayInputStream", "ByteArrayOutputStream", "CharArrayReader", "CharArrayWriter", "Console", "DataInputStream", "DataOutputStream", "File", "FileDescriptor", "FileInputStream", "FileOutputStream", "FilePermission", "FileReader", "FileWriter", "FilterInputStream", "FilterOutputStream", "FilterReader", "FilterWriter", "InputStream", "InputStreamReader", "LineNumberInputStream", "LineNumberReader", "ObjectInputStream", "ObjectInputStream.GetField", "ObjectOutputStream", "ObjectOutputStream.PutField", "ObjectStreamClass", "ObjectStreamField", "OutputStream", "OutputStreamWriter", "PipedInputStream", "PipedOutputStream", "PipedReader", "PipedWriter", "PrintStream", "PrintWriter", "PushbackInputStream", "PushbackReader", "RandomAccessFile", "Reader", "SequenceInputStream", "SerializablePermission", "StreamTokenizer", "StringBufferInputStream", "StringReader", "StringWriter", "Writer", "CharConversionException", "EOFException", "FileNotFoundException", "InterruptedIOException", "InvalidClassException", "InvalidObjectException", "IOException", "NotActiveException", "NotSerializableException", "ObjectStreamException", "OptionalDataException", "StreamCorruptedException", "SyncFailedException", "UnsupportedEncodingException", "UTFDataFormatException", "WriteAbortedException", "IOError"));

        PACKAGES_OF_INTEREST = Collections.unmodifiableMap(container);
    }

    private Recommender(){}

    /**
     * Recommends changes for a {@code Source} based on a list of found {@code issues}.
     * E.g., if this {@code Source} has 10 issues in it, then the
     * {@code Refactorer} will recommend 10 changes that will address this 10 issues.
     *
     * <p />
     *
     * Context cache is flushed out after recommending changes for a set of issues. THis mean that
     * if this method is not invoked, then the cached contexts will remain.
     *
     * @param refactorer The active refactorer
     * @param code The {@code Source}
     * @param issues The issues from where changes will be recommended.
     * @return The list of recommended changes
     * @throws java.lang.NullPointerException if {@code Source} null.
     */
    public static List<Change> recommendChanges(Refactorer refactorer, Source code, Set<Issue> issues){
        final List<Change> recommendations = new ArrayList<Change>();

        for(Issue issue : issues){
            recommendations.add(refactorer.createChange(ChangeRequest.forIssue(issue, code)));
        }

        return recommendations;
    }


    private static Set<String> getJdkPackages(){
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

    /**
     * Recommend the required import directives for source to be syntactically correct.
     *
     * @param code The source to be scanned through looking for potential imports to recommend.
     * @return the required import directives.
     */
    public static Set<String> recommendImports(Source code){
        final JavaParser parser = new EclipseJavaParser();

        final Context context = new Context(code);

        ASTNode unit = parser.parseJava(context, EclipseJavaParser.PARSE_STATEMENTS);
        if(unit == null){
            throw new IllegalStateException("Unable to parse source file");
        } else {
            context.setCompilationUnit(AstUtil.getCompilationUnit(unit));
        }


        final Set<String>           types     = AstUtil.getUsedTypesInCode(context.getCompilationUnit());
        final Set<String>           packages  = getJdkPackages();
        final Map<String, Integer>  hits      = Maps.newHashMap();

        final Set<String> result = Sets.newHashSet();

        for(String each : packages){
            final Set<String> knownTypes = PACKAGES_OF_INTEREST.get(each);
            if(knownTypes != null){
                final Set<String> intersect = Sets.intersection(knownTypes, types);
                final boolean valid = (!intersect.isEmpty());
                if(!valid) continue;

                if(!hits.containsKey(each)) { hits.put(each, intersect.size()); } else {
                    hits.put(each, hits.get(each) + intersect.size());
                }

                if(hits.get(each) >= 5){
                    final String wildcard = each + ".*;";
                    result.add(wildcard);
                } else {
                    for(String eachIntersect : intersect){
                        result.add(each + "." + eachIntersect);
                    }
                }
            }
        }

        return result;
    }

    public static List<Clip> generateClips(String text, Source code){

        final ClipSpaceGeneration spaceGeneration = new ClipSpaceGeneration();
        // The clip space represents a multi stage example; an example split into chunks
        // where each chunk increases the complexity of the code example.
        final Set<Clip> clipSpace = spaceGeneration.generateSpace(code);

        // normalize line endings
        text = text.replaceAll("\r\n", "\n");


        // small enough space (< 5)? if yes, then dont rank it.
        if(clipSpace.size() < 5){ // 5? on avg, ppl create 5 stages
           return ImmutableList.copyOf(clipSpace).reverse();
        } else { // otherwise, do the ranking and pick the  top 5 clips (may not be multi-stage
            final ClipRankingStrategy   ranking     = new ClipRankingStrategy();
            final List<Clip>            rankedSpace = ranking.rankSpace(clipSpace, text);
            return rankedSpace.subList(0, Math.min(5, rankedSpace.size()));
        }

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
            final CompilationUnit          unit     = context.getCompilationUnit();

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
               final Commit        commit  = refactorer.apply(change);

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


    static class ClipRankingStrategy implements RankingStrategy {
        @Override public List<Clip> rankSpace(Set<Clip> space, String query) {
            final List<Clip> reversed = ImmutableList.copyOf(space).reverse();
            return ImmutableList.copyOf(reversed);
        }
    }
}
