package edu.ucsc.refactor.spi.find;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.util.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class TypeSpace {

  private static Map<String, Set<String>> BACKUP;
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

    BACKUP = Collections.unmodifiableMap(container);
  }

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

  private final Map<String, Set<String>> space;
  private final List<Class<?>>           classes;
  private final List<MethodFinder>       finders;

  /**
   * Prevents this class from being
   */
  private TypeSpace(){
    classes = ClassSearch.searchClassPath();
    space   = computeSpace(classes);
    finders = computeFinders(classes);
  }

  public Set<Method> getMethodsMatchingSignature(Class<?> returnType, Class<?>... arguments) {

    final Set<Method> methods = Sets.newLinkedHashSet();
    for(MethodFinder each : finders){
      methods.addAll(each.findInstanceMethods(each.declaringClass, returnType, arguments));
      methods.addAll(each.findStaticMethods(each.declaringClass, returnType, arguments));
    }

    return methods;
  }


  /**
   * Returns the computer package space (built based on all classes found in the classpath)
   */
  public Map<String, Set<String>> getPackageSpace(){
    return space;
  }

  /**
   * Returns a list of classes found in the classpath.
   */
  public List<Class<?>> getClassesInClasspath(){
    return classes;
  }


  public Set<String> getClassInPackage(String pkg){
    if(!getPackageSpace().containsKey(pkg)) return Sets.newLinkedHashSet();
    return getPackageSpace().get(pkg);
  }

  /**
   * Returns the sole PackageSpace instance
   */
  public static TypeSpace getInstance() {
    return Installer.instance;
  }

  public static boolean inJavaLang(String typeName){
    return JAVA_LANG.contains(typeName);
  }

  public static boolean inJavaUtil(String typeName){
    return JAVA_UTIL.contains(typeName);
  }

  private static Map<String, Set<String>> computeSpace(final List<Class<?>> classes){
    final Map<String, Set<String>> emptySpace = Maps.newLinkedHashMap();

    for(Class<?> each : classes){
      final String pkg = each.getPackage().getName();
      if(null == pkg){
        System.out.println(each);
      }
      if(emptySpace.containsKey(pkg)){
        emptySpace.get(pkg).add(each.getSimpleName());
      } else {
        try {
          final String name = each.getSimpleName();
          if(StringUtil.isEmpty(name)) continue;
          final Set<String> container = Sets.newLinkedHashSet();
          container.add(name);
          emptySpace.put(pkg, container);
        } catch (Throwable e){
          // ignore
        }
      }
    }

    for(String key : BACKUP.keySet()){
      final Set<String>  current = BACKUP.get(key);
      if(emptySpace.containsKey(key)){
        emptySpace.get(key).addAll(current);
      } else {
        emptySpace.put(key, current);
      }
    }

    return Collections.unmodifiableMap(emptySpace);
  }

  private static List<MethodFinder> computeFinders(final List<Class<?>> classes){
    final List<MethodFinder> finders = Lists.newLinkedList();
    for(Class<?> each : classes){
      finders.add(new MethodFinder(each));
    }

    return finders;
  }

  /**
   * Lazy loaded singleton;
   * thx to http://blog.crazybob.org/2007/01/lazy-loading-singletons.html
   */
  static class Installer {
    static TypeSpace instance = new TypeSpace();
  }

  static class MethodFinder {
    final Class<?> declaringClass;

    MethodFinder(Class<?> declaringClass){
      this.declaringClass = declaringClass;
    }

    /**
     * Finds instance method matches
     *
     * @param returnType the return type
     * @param arguments  the arguments (in any order)
     * @return list of instance methods
     */
    List<Method> findInstanceMethods(Class<?> klass, Class<?> returnType, Class<?>...
          arguments) {

      final Matcher<Method> methodMatcher = MatchMaker.methodMatcher(returnType, arguments);
      final List<Method> matches = new LinkedList<Method>();

      for (Method method : klass.getMethods()) {
        if ((method.getModifiers() & Modifier.STATIC) == 0)
          if (methodMatcher.matches(method))
            matches.add(method);
      }

      return matches;
    }

    /**
     * Finds static method matches
     *
     * @param returnType the return type
     * @param arguments  the arguments (in any order)
     * @return list of static methods
     */
    List<Method> findStaticMethods(Class<?> klass, Class<?> returnType, Class<?>...
          arguments) {
      final Matcher<Method> methodMatcher = MatchMaker.methodMatcher(returnType, arguments);
      final List<Method> matches = new LinkedList<Method>();

      for (Method method : klass.getMethods())
        if ((method.getModifiers() & Modifier.STATIC) != 0)
          if (methodMatcher.matches(method))
            matches.add(method);

      return matches;
    }
  }


  @Override public String toString() {
    return "TypeSpace(size=" + getClassesInClasspath().size() + ")";
  }

}
