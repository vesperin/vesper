package edu.ucsc.refactor.spi.find;

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
