package edu.ucsc.refactor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.IssueDetector;

import java.util.*;

import static edu.ucsc.refactor.Context.throwCompilationErrorIfExist;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class CodeIntrospector implements Introspector {


    private static Map<String, Set<String>> PKGS_OF_INTEREST;
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

        PKGS_OF_INTEREST = Collections.unmodifiableMap(container);
    }

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

    @Override public Set<String> findMissingImports(Context context) {
        final Set<String>           types     = AstUtil.getUsedTypesInCode(context.getCompilationUnit());
        final Set<String>           packages  = getJdkPackages();
        final Map<String, Integer>  hits      = Maps.newHashMap();

        final Set<String> result = Sets.newHashSet();

        for(String each : packages){
            final Set<String> knownTypes = PKGS_OF_INTEREST.get(each);
            if(knownTypes != null){
                final Set<String> intersect = Sets.intersection(knownTypes, types);
                final boolean valid = (intersect != null && !intersect.isEmpty());
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

    @Override public List<String> verifySource(Source code) {
        return ImmutableList.copyOf(
                this.host.createContext(code).getSyntaxRelatedProblems()
        );
    }
}
