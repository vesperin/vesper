package edu.ucsc.refactor.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.*;
import edu.ucsc.refactor.internal.EclipseJavaParser;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.JavaParser;
import edu.ucsc.refactor.spi.RankingStrategy;
import org.eclipse.jdt.core.dom.ASTNode;

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

    private static final int BOUNDARY = 5;

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

    private static Context makeContext(Source code){
        final JavaParser parser = new EclipseJavaParser();

        final Context context = new Context(code);

        ASTNode unit = parser.parseJava(context, EclipseJavaParser.PARSE_STATEMENTS);
        if(unit == null){
            throw new IllegalStateException("Unable to parse source file");
        } else {
            context.setCompilationUnit(AstUtil.getCompilationUnit(unit));
        }

        return context;
    }

    private static Set<String> normalize(Set<String> docs){
        final Set<String> n = Sets.newHashSet();
        for(String each : docs){
            // normalize line endings
            n.add(each.replaceAll("\r\n", "\n"));
        }

        return n;
    }

    /**
     * Ranks and then recommends the top K ranked clips.
     *
     * @param docs A set of documents describing the code example.
     * @param clipSpace The space to be ranked.
     * @return The top K ranked clips.
     */
    public static List<Clip> recommendClips(List<Clip> clipSpace, Set<String> docs){
        final Set<String> normalized = normalize(docs);

        // small enough space (< 5)? if yes, then dont rank it.
        if(clipSpace.size() <= BOUNDARY){ // 5? on avg, ppl create 5 stages
            return clipSpace;
        } else { // otherwise, do the ranking and pick the  top 5 clips (may not be multi-stage
            final ClipRankingStrategy   ranking     = new ClipRankingStrategy();
            final List<Clip>            rankedSpace = ranking.rankSpace(clipSpace, normalized);
            return rankedSpace.subList(0, Math.min(BOUNDARY, rankedSpace.size()));
        }
    }

    static class ClipRankingStrategy implements RankingStrategy {
        @Override public List<Clip> rankSpace(List<Clip> space, Set<String> docs) {
            final List<Clip> reversed = ImmutableList.copyOf(space).reverse();
            return ImmutableList.copyOf(reversed);
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
