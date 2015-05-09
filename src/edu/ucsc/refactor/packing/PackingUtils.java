package edu.ucsc.refactor.packing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class PackingUtils {

  private static final Set<String> JAVA_LANG = Sets.newHashSet(
        "Cloneable",
        "Runnable",
        "Boolean",
        "Byte",
        "Character",
        "Class",
        "ClassLoader",
        "Compiler",
        "Double",
        "Float",
        "Integer",
        "Long",
        "Math",
        "Number",
        "Object",
        "Process",
        "Runtime",
        "SecurityManager",
        "Short",
        "String",
        "StringBuffer",
        "System",
        "Thread",
        "ThreadGroup",
        "Throwable",
        "Void",
        "ArithmeticException",
        "ArrayIndexOutOfBoundsException",
        "ArrayStoreException",
        "ClassCastException",
        "ClassNotFoundException",
        "CloneNotSupportedException",
        "Exception",
        "IllegalAccessException",
        "IllegalArgumentException",
        "IllegalMonitorStateException",
        "IllegalStateException",
        "IllegalThreadStateException",
        "IndexOutOfBoundsException",
        "InstantiationException",
        "InterruptedException",
        "NegativeArraySizeException",
        "NoSuchFieldException",
        "NoSuchMethodException",
        "NullPointerException",
        "NumberFormatException",
        "RuntimeException",
        "SecurityException",
        "StringIndexOutOfBoundsException",
        "AbstractMethodError",
        "ClassCircularityError",
        "ClassFormatError",
        "Error",
        "ExceptionInInitializerError",
        "IllegalAccessError",
        "IncompatibleClassChangeError",
        "InstantiationError",
        "InternalError",
        "LinkageError",
        "NoClassDefFoundError",
        "NoSuchFieldError",
        "NoSuchMethodError",
        "OutOfMemoryError",
        "StackOverflowError",
        "ThreadDeath",
        "UnknownError",
        "UnsatisfiedLinkError",
        "VerifyError",
        "VirtualMachineError"
  );

  private static final Set<String> JAVA_UTIL = ImmutableSet.of(
        "Collection",
        "Comparator",
        "Deque",
        "Enumeration",
        "EventListener",
        "Formattable",
        "Iterator",
        "List",
        "ListIterator",
        "Map",
        "Map.Entry",
        "NavigableMap",
        "NavigableSet",
        "Observer",
        "Queue",
        "RandomAccess",
        "Set",
        "SortedMap",
        "SortedSet",
        "AbstractCollection",
        "AbstractList",
        "AbstractMap",
        "AbstractMap.SimpleEntry",
        "AbstractMap.SimpleImmutableEntry",
        "AbstractQueue", "AbstractSequentialList",
        "AbstractSet", "ArrayDeque", "ArrayList",
        "Arrays", "BitSet", "Calendar", "Collections",
        "Currency", "Date", "Dictionary", "EnumMap", "EnumSet",
        "EventListenerProxy", "EventObject", "FormattableFlags",
        "Formatter", "GregorianCalendar", "HashMap",
        "HashSet", "Hashtable", "IdentityHashMap",
        "LinkedHashMap", "LinkedHashSet",
        "LinkedList", "ListResourceBundle",
        "Locale", "Observable", "PriorityQueue",
        "Properties", "PropertyPermission",
        "PropertyResourceBundle", "Random",
        "ResourceBundle", "ResourceBundle.Control",
        "Scanner", "ServiceLoader", "SimpleTimeZone",
        "Stack", "StringTokenizer", "Timer",
        "TimerTask", "TimeZone", "TreeMap",
        "TreeSet", "UUID", "Vector", "WeakHashMap",
        "Formatter.BigDecimalLayoutForm",
        "ConcurrentModificationException",
        "DuplicateFormatFlagsException",
        "EmptyStackException",
        "FormatFlagsConversionMismatchException",
        "FormatterClosedException", "IllegalFormatCodePointException",
        "IllegalFormatConversionException",
        "IllegalFormatException", "IllegalFormatFlagsException",
        "IllegalFormatPrecisionException", "IllegalFormatWidthException",
        "InputMismatchException", "InvalidPropertiesFormatException",
        "MissingFormatArgumentException",
        "MissingFormatWidthException", "MissingResourceException",
        "NoSuchElementException", "TooManyListenersException",
        "UnknownFormatConversionException", "UnknownFormatFlagsException",
        "ServiceConfigurationError"
  );

  private PackingUtils(){}

  /**
   * Returns the autoboxing types
   *
   * @param type the type to autobox :)
   * @return a list of types that it could be
   */
  @SuppressWarnings("InstantiatingObjectToGetClassObject")
  public static List<Class<?>> autobox(Class<?> type) {

    if (type == Boolean.class || type == Boolean.TYPE)
      return ImmutableList.<Class<?>>of(Boolean.class, Boolean.TYPE);
    if (type == Character.class || type == Character.TYPE)
      return ImmutableList.<Class<?>>of(Character.class, Character.TYPE);
    if (type == Short.class || type == Short.TYPE)
      return ImmutableList.<Class<?>>of(Short.class, Short.TYPE);
    if (type == Integer.class || type == Integer.TYPE)
      return ImmutableList.<Class<?>>of(Integer.class, Integer.TYPE);
    if (type == Float.class || type == Float.TYPE)
      return ImmutableList.<Class<?>>of(Float.class, Float.TYPE);
    if (type == Double.class || type == Double.TYPE)
      return ImmutableList.<Class<?>>of(Double.class, Double.TYPE);
    if (type == Void.class || type == Void.TYPE)
      return ImmutableList.<Class<?>>of(Void.class, Void.TYPE);
    if (type == new int[0].getClass()) {
      return ImmutableList.<Class<?>>of(new int[0].getClass(), Integer.TYPE);
    }
    if (type == new char[0].getClass()) {
      return ImmutableList.<Class<?>>of(new char[0].getClass(), Character.TYPE);
    }

    if (type == new boolean[0].getClass()) {
      return ImmutableList.<Class<?>>of(new boolean[0].getClass(), Boolean.TYPE);
    }

    return ImmutableList.<Class<?>>of(type);
  }

  public static PackingSpace failoverTypeSpace(){
    return new FailoverTypeSpace();
  }

  /**
   * Returns true if the typeName is member of the package java.lang.
   */
  public static boolean inJavaLang(String typeName){
    return JAVA_LANG.contains(typeName);
  }

  /**
   * Returns true if the typeName is member of the package java.util.
   */
  public static boolean inJavaUtil(String typeName){
    return JAVA_UTIL.contains(typeName);
  }

  public static Set<String> getJdkPackages() {
    final Package[] ps = Package.getPackages();
    final Set<String> result = Sets.newHashSet();
    for (Package each : ps) {
      if (each.getName().contains("sun.") ||
            each.getName().contains("javax.")
            || each.getName().contains("org.") || each.getName().contains("edu.ucsc."))
        continue;
      result.add(each.getName());
    }

    return result;
  }


  /**
   * Performs a union between the current type space and the other type space.
   *
   * @param a the source type space
   * @param b the other type space.
   * @return the updated type space containing the elements of the current
   *    type space and the elements of the other type space.
   */
  public static PackingSpace union(PackingSpace a, PackingSpace b) {
    //if(!a.isEmpty()) return a; // avoid union when necessary

    for(String key : b.packageSet()){
      final Set<String>  current = b.classSet(key);
      if(a.packageSet().contains(key)){
        a.classSet(key).addAll(current);
      } else {
        a.put(key, current);
      }
    }

    return a;
  }

  static class FailoverTypeSpace implements PackingSpace {
    static Map<String, Set<String>> TYPE_SPACE;

    static {
      final Map<String, Set<String>> container = Maps.newHashMap();
      container.put("java.util.zip", ImmutableSet.of("Adler32", "CheckedInputStream",
            "CheckedOutputStream", "CRC32", "Deflater", "DeflaterInputStream",
            "DeflaterOutputStream", "GZIPInputStream", "GZIPOutputStream", "Inflater",
            "InflaterInputStream", "InflaterOutputStream", "ZipEntry", "ZipFile",
            "ZipInputStream", "ZipOutputStream", "DataFormatException", "ZipException", "ZipError"));
      container.put("java.util.spi", ImmutableSet.of("CurrencyNameProvider", "LocaleNameProvider",
            "LocaleServiceProvider", "TimeZoneNameProvider"));
      container.put("java.util.regex", ImmutableSet.of("MatchResult", "Matcher", "Pattern",
            "PatternSyntaxException"));
      container.put("java.util.prefs", ImmutableSet.of("NodeChangeListener",
            "PreferenceChangeListener", "PreferencesFactory", "AbstractPreferences", "NodeChangeEvent",
            "PreferenceChangeEvent", "Preferences", "BackingStoreException",
            "InvalidPreferencesFormatException"));
      container.put("java.util.logging", ImmutableSet.of("Filter", "LoggingMXBean", "ConsoleHandler",
            "ErrorManager", "FileHandler", "Formatter", "Handler", "Level", "Logger",
            "LoggingPermission", "LogManager", "LogRecord", "MemoryHandler", "SimpleFormatter",
            "SocketHandler", "StreamHandler", "XMLFormatter"));
      container.put("java.util.jar", ImmutableSet.of("Pack200.Packer", "Pack200.Unpacker",
            "Attributes", "Attributes.Name", "JarEntry", "JarFile", "JarInputStream",
            "JarOutputStream", "Manifest", "Pack200", "JarException"));
      container.put("java.util.concurrent.locks", ImmutableSet.of("Condition", "Lock",
            "ReadWriteLock", "AbstractOwnableSynchronizer", "AbstractQueuedLongSynchronizer",
            "AbstractQueuedSynchronizer", "LockSupport", "ReentrantLock", "ReentrantReadWriteLock",
            "ReentrantReadWriteLock.ReadLock", "ReentrantReadWriteLock.WriteLock"));
      container.put("java.util.concurrent.atomic", ImmutableSet.of("AtomicBoolean", "AtomicInteger",
            "AtomicIntegerArray", "AtomicIntegerFieldUpdater", "AtomicLong", "AtomicLongArray",
            "AtomicLongFieldUpdater", "AtomicMarkableReference", "AtomicReference",
            "AtomicReferenceArray", "AtomicReferenceFieldUpdater", "AtomicStampedReference"));
      container.put("java.util.concurrent", ImmutableSet.of("BlockingDeque", "BlockingQueue",
            "Callable", "CompletionService", "ConcurrentMap", "ConcurrentNavigableMap", "Delayed",
            "Executor", "ExecutorService", "Future", "RejectedExecutionHandler", "RunnableFuture",
            "RunnableScheduledFuture", "ScheduledExecutorService", "ScheduledFuture", "ThreadFactory",
            "AbstractExecutorService", "ArrayBlockingQueue", "ConcurrentHashMap",
            "ConcurrentLinkedQueue", "ConcurrentSkipListMap", "ConcurrentSkipListSet",
            "CopyOnWriteArrayList", "CopyOnWriteArraySet", "CountDownLatch", "CyclicBarrier",
            "DelayQueue", "Exchanger", "ExecutorCompletionService", "Executors", "FutureTask",
            "LinkedBlockingDeque", "LinkedBlockingQueue", "PriorityBlockingQueue",
            "ScheduledThreadPoolExecutor", "Semaphore", "SynchronousQueue", "ThreadPoolExecutor",
            "ThreadPoolExecutor.AbortPolicy", "ThreadPoolExecutor.CallerRunsPolicy",
            "ThreadPoolExecutor.DiscardOldestPolicy", "ThreadPoolExecutor.DiscardPolicy",
            "TimeUnit", "BrokenBarrierException", "CancellationException", "ExecutionException",
            "RejectedExecutionException", "TimeoutException"));
      container.put("java.util", ImmutableSet.of("Collection", "Comparator", "Deque", "Enumeration",
            "EventListener", "Formattable", "Iterator", "List", "ListIterator", "Map", "Map.Entry",
            "NavigableMap", "NavigableSet", "Observer", "Queue", "RandomAccess", "Set", "SortedMap",
            "SortedSet", "AbstractCollection", "AbstractList", "AbstractMap", "AbstractMap.SimpleEntry",
            "AbstractMap.SimpleImmutableEntry", "AbstractQueue", "AbstractSequentialList",
            "AbstractSet", "ArrayDeque", "ArrayList", "Arrays", "BitSet", "Calendar", "Collections",
            "Currency", "Date", "Dictionary", "EnumMap", "EnumSet", "EventListenerProxy",
            "EventObject", "FormattableFlags", "Formatter", "GregorianCalendar", "HashMap", "HashSet",
            "Hashtable", "IdentityHashMap", "LinkedHashMap", "LinkedHashSet", "LinkedList",
            "ListResourceBundle", "Locale", "Observable", "PriorityQueue", "Properties",
            "PropertyPermission", "PropertyResourceBundle", "Random", "ResourceBundle",
            "ResourceBundle.Control", "Scanner", "ServiceLoader", "SimpleTimeZone", "Stack",
            "StringTokenizer", "Timer", "TimerTask", "TimeZone", "TreeMap", "TreeSet", "UUID",
            "Vector", "WeakHashMap", "Formatter.BigDecimalLayoutForm",
            "ConcurrentModificationException", "DuplicateFormatFlagsException",
            "EmptyStackException", "FormatFlagsConversionMismatchException",
            "FormatterClosedException", "IllegalFormatCodePointException",
            "IllegalFormatConversionException", "IllegalFormatException",
            "IllegalFormatFlagsException", "IllegalFormatPrecisionException",
            "IllegalFormatWidthException", "InputMismatchException",
            "InvalidPropertiesFormatException", "MissingFormatArgumentException",
            "MissingFormatWidthException", "MissingResourceException", "NoSuchElementException",
            "TooManyListenersException", "UnknownFormatConversionException",
            "UnknownFormatFlagsException", "ServiceConfigurationError"));
      container.put("java.text.spi", ImmutableSet.of("BreakIteratorProvider", "CollatorProvider",
            "DateFormatProvider", "DateFormatSymbolsProvider", "DecimalFormatSymbolsProvider",
            "NumberFormatProvider"));
      container.put("java.text", ImmutableSet.of("AttributedCharacterIterator", "CharacterIterator",
            "Annotation", "AttributedCharacterIterator.Attribute", "AttributedString", "Bidi",
            "BreakIterator", "ChoiceFormat", "CollationElementIterator", "CollationKey", "Collator",
            "DateFormat", "DateFormat.Field", "DateFormatSymbols", "DecimalFormat",
            "DecimalFormatSymbols", "FieldPosition", "Format", "Format.Field", "MessageFormat",
            "MessageFormat.Field", "Normalizer", "NumberFormat", "NumberFormat.Field",
            "ParsePosition", "RuleBasedCollator", "SimpleDateFormat", "StringCharacterIterator",
            "Normalizer.Form", "ParseException"));
      container.put("java.nio.charset.spi", ImmutableSet.of("CharsetProvider"));
      container.put("java.nio.charset", ImmutableSet.of("Charset", "CharsetDecoder", "CharsetEncoder",
            "CoderResult", "CodingErrorAction", "CharacterCodingException",
            "IllegalCharsetNameException", "MalformedInputException", "UnmappableCharacterException",
            "UnsupportedCharsetException", "CoderMalfunctionError"));
      container.put("java.nio.channels.spi", ImmutableSet.of("AbstractInterruptibleChannel",
            "AbstractSelectableChannel", "AbstractSelectionKey", "AbstractSelector",
            "SelectorProvider"));
      container.put("java.nio.channels", ImmutableSet.of("ByteChannel", "Channel",
            "GatheringByteChannel", "InterruptibleChannel", "ReadableByteChannel",
            "ScatteringByteChannel", "WritableByteChannel", "Channels", "DatagramChannel",
            "FileChannel", "FileChannel.MapMode", "FileLock", "Pipe", "Pipe.SinkChannel",
            "Pipe.SourceChannel", "SelectableChannel", "SelectionKey", "Selector",
            "ServerSocketChannel", "SocketChannel", "AlreadyConnectedException",
            "AsynchronousCloseException", "CancelledKeyException", "ClosedByInterruptException",
            "ClosedChannelException", "ClosedSelectorException", "ConnectionPendingException",
            "FileLockInterruptionException", "IllegalBlockingModeException",
            "IllegalSelectorException", "NoConnectionPendingException",
            "NonReadableChannelException", "NonWritableChannelException",
            "NotYetBoundException", "NotYetConnectedException", "OverlappingFileLockException",
            "UnresolvedAddressException", "UnsupportedAddressTypeException"));
      container.put("java.nio", ImmutableSet.of("Buffer", "ByteBuffer", "ByteOrder",
            "CharBuffer", "DoubleBuffer", "FloatBuffer", "IntBuffer", "LongBuffer",
            "MappedByteBuffer", "ShortBuffer", "BufferOverflowException", "BufferUnderflowException",
            "InvalidMarkException", "ReadOnlyBufferException"));
      container.put("java.net", ImmutableSet.of("ContentHandlerFactory", "CookiePolicy",
            "CookieStore", "DatagramSocketImplFactory", "FileNameMap", "SocketImplFactory",
            "SocketOptions", "URLStreamHandlerFactory", "Authenticator", "CacheRequest",
            "CacheResponse", "ContentHandler", "CookieHandler", "CookieManager", "DatagramPacket",
            "DatagramSocket", "DatagramSocketImpl", "HttpCookie", "HttpURLConnection", "IDN",
            "Inet4Address", "Inet6Address", "InetAddress", "InetSocketAddress", "InterfaceAddress",
            "JarURLConnection", "MulticastSocket", "NetPermission", "NetworkInterface",
            "PasswordAuthentication", "Proxy", "ProxySelector", "ResponseCache", "SecureCacheResponse",
            "ServerSocket", "Socket", "SocketAddress", "SocketImpl", "SocketPermission", "URI",
            "URL", "URLClassLoader", "URLConnection", "URLDecoder", "URLEncoder", "URLStreamHandler",
            "Authenticator.RequestorType", "Proxy.Type", "BindException", "ConnectException",
            "HttpRetryException", "MalformedURLException", "NoRouteToHostException",
            "PortUnreachableException", "ProtocolException", "SocketException",
            "SocketTimeoutException", "UnknownHostException", "UnknownServiceException",
            "URISyntaxException"));
      container.put("java.math", ImmutableSet.of("BigDecimal", "BigInteger", "MathContext",
            "RoundingMode"));
      container.put("java.lang.reflect", ImmutableSet.of("AnnotatedElement", "GenericArrayType",
            "GenericDeclaration", "InvocationHandler", "Member", "ParameterizedType", "Type",
            "TypeVariable", "WildcardType", "AccessibleObject", "Array", "Constructor", "Field",
            "Method", "Modifier", "Proxy", "ReflectPermission", "InvocationTargetException",
            "MalformedParameterizedTypeException", "UndeclaredThrowableException",
            "GenericSignatureFormatError"));
      container.put("java.lang.ref", ImmutableSet.of("PhantomReference", "Reference",
            "ReferenceQueue", "SoftReference", "WeakReference"));
      container.put("java.lang.management", ImmutableSet.of("ClassLoadingMXBean",
            "CompilationMXBean", "GarbageCollectorMXBean", "MemoryManagerMXBean",
            "MemoryMXBean", "MemoryPoolMXBean", "OperatingSystemMXBean", "RuntimeMXBean",
            "ThreadMXBean", "LockInfo", "ManagementFactory", "ManagementPermission",
            "MemoryNotificationInfo", "MemoryUsage", "MonitorInfo", "ThreadInfo", "MemoryType"));
      container.put("java.lang.instrument", ImmutableSet.of("ClassFileTransformer",
            "Instrumentation", "ClassDefinition", "IllegalClassFormatException",
            "UnmodifiableClassException"));
      container.put("java.lang.annotation", ImmutableSet.of("Annotation", "ElementType",
            "RetentionPolicy", "AnnotationTypeMismatchException", "IncompleteAnnotationException",
            "AnnotationFormatError", "Documented", "Inherited", "Retention", "Target"));
      container.put("java.io", ImmutableSet.of("Closeable", "DataInput", "DataOutput",
            "Externalizable", "FileFilter", "FilenameFilter", "Flushable", "ObjectInput",
            "ObjectInputValidation", "ObjectOutput", "ObjectStreamConstants", "Serializable",
            "BufferedInputStream", "BufferedOutputStream", "BufferedReader", "BufferedWriter",
            "ByteArrayInputStream", "ByteArrayOutputStream", "CharArrayReader", "CharArrayWriter",
            "Console", "DataInputStream", "DataOutputStream", "File", "FileDescriptor",
            "FileInputStream", "FileOutputStream", "FilePermission", "FileReader", "FileWriter",
            "FilterInputStream", "FilterOutputStream", "FilterReader", "FilterWriter", "InputStream",
            "InputStreamReader", "LineNumberInputStream", "LineNumberReader", "ObjectInputStream",
            "ObjectInputStream.GetField", "ObjectOutputStream", "ObjectOutputStream.PutField",
            "ObjectStreamClass", "ObjectStreamField", "OutputStream", "OutputStreamWriter",
            "PipedInputStream", "PipedOutputStream", "PipedReader", "PipedWriter", "PrintStream",
            "PrintWriter", "PushbackInputStream", "PushbackReader", "RandomAccessFile", "Reader",
            "SequenceInputStream", "SerializablePermission", "StreamTokenizer",
            "StringBufferInputStream", "StringReader", "StringWriter", "Writer",
            "CharConversionException", "EOFException", "FileNotFoundException",
            "InterruptedIOException", "InvalidClassException", "InvalidObjectException",
            "IOException", "NotActiveException", "NotSerializableException", "ObjectStreamException",
            "OptionalDataException", "StreamCorruptedException", "SyncFailedException",
            "UnsupportedEncodingException", "UTFDataFormatException", "WriteAbortedException",
            "IOError"));

      TYPE_SPACE = Collections.unmodifiableMap(container);
    }

    private final Map<String, Set<String>> pkgToClasses;
    FailoverTypeSpace(){
      this(TYPE_SPACE);
    }

    FailoverTypeSpace(Map<String, Set<String>> pkgToClasses){
      this.pkgToClasses = pkgToClasses;
    }

    @Override public boolean isEmpty() {
      return pkgToClasses.isEmpty();
    }

    @Override public Set<String> classSet(String pkg) {
      return (pkgToClasses.containsKey(pkg)
            ? pkgToClasses.get(pkg)
            : Sets.<String>newHashSet()
      );
    }

    @Override public Set<String> packageSet() {
      return pkgToClasses.keySet();
    }

    @Override public PackingSpace put(String pkg, Set<String> classMembers) {
      pkgToClasses.put(pkg, classMembers);
      return this;
    }

    @Override public int size() {
      return pkgToClasses.size();
    }
  }
}
