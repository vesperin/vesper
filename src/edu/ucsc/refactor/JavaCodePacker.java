package edu.ucsc.refactor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.reflect.ClassPath;
import edu.ucsc.refactor.packing.MatchingStrategy;
import edu.ucsc.refactor.packing.PackingSpace;
import edu.ucsc.refactor.packing.PackingSpaceGeneration;
import edu.ucsc.refactor.packing.PackingUtils;
import edu.ucsc.refactor.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class JavaCodePacker implements CodePacker {

  private final MatchingStrategy matcher;
  private final PackingSpace typeSpace;

  /**
   * Creates the JavaCodePacker object.
   */
  public JavaCodePacker(){
    this(new InMemoryPrecomputation(), new IntersectionBasedMatchingStrategy());
  }

  /**
   * Creates the JavaCodePacker object.
   *
   * @param precomputation the step that builds the type space.
   * @param matchingLogic  the step that matches a set of types to a set of packages.
   */
  public JavaCodePacker(PackingSpaceGeneration precomputation, MatchingStrategy matchingLogic){
    this.matcher    = matchingLogic;
    this.typeSpace  = Preconditions.checkNotNull(precomputation).generateSpace
          (PackingUtils.ALLOWED_PACKAGES);
  }

  @Override public String existingHeader(Source code) {
    final Context context = CodeIntrospector.makeContext(code);

    final String withName = StringUtil.extractFileName(code.getName());
    final List<String> withImports = CodeIntrospector.findImports(
          CodeIntrospector.findImports(context)
    );

    return StringUtil.concat(withName, false, withImports);
  }

  @Override public String missingHeader(Source code, String name) {
    final Set<String> header = missingImports(code, "import");
    final String cname = ("".equals(name)
          ? StringUtil.extractFileName(code.getName())
          : name
    );

    header.add("class " + cname + " {");

    return Joiner.on("\n").join(header);
  }

  @Override public Set<String> missingImports(Source code, final String prepend) {
    final Introspector introspector = Vesper.createIntrospector();

    Preconditions.checkNotNull(typeSpace);

    final List<String> hits = this.matcher.matches(introspector.typesInside(code), typeSpace);

    final List<String> transformed = Lists.transform(hits, new Function<String, String>() {
            @Override public String apply(String s) {
              if("".equals(prepend)) return s;
              return StringUtil.trim(prepend) + " " + s;
            }
          }
    );

    return Sets.newLinkedHashSet(transformed);
  }

  @Override public Set<String> missingImports(Source code) {
    return missingImports(code, "import");
  }

  @Override public Source packs(Source code, String name) {
    final String top    = missingHeader(code, name);
    final boolean main  = needsMethod(code.getContents());

    final String body   = main ?
          ("\npublic static void main(String... args){\n" + StringUtil.trim(code.getContents()) +
                "\n}") :
          code.getContents();

    final String bottom = "}";

    final String content = top + body + bottom;

    return Source.from(code, content);
  }

  @Override public Source packs(Source code) {
    return packs(code, "");
  }

  @Override public Source unpacks(Source packed, Source original) {
    final String addon    = existingHeader(packed);
    final String content  = packed.getContents();
    final boolean main    = needsMethod(content);

    final String  end   = !main && hasMain(content)
          && !hasMain(original.getContents()) ? "}}" : "}";
    final String  start = !main && hasMain(content)
          && !hasMain(original.getContents())
          ? addon + "\npublic static void main(String... args){\n"
          : addon;

    final String currentContent = packed.getContents();
    final String updatedContent = StringUtil.trim(
          StringUtil.removeEnd(
                StringUtil.trim(StringUtil.removeStart(currentContent, start)),
                end
          )
    );

    final Source cropped = Source.from(packed, updatedContent);
    cropped.setName("Scratched.java");
    return cropped;
  }

  static boolean hasMain(String content){
    return content.contains(("public static void main"));
  }

  static boolean needsMethod(String content){
    final String regex  = "^\\s*?(((public|private|protected|static|final|native|synchronized" +
          "|abstract|threadsafe|transient)\\s+?)*)\\s*?(\\w+?)\\s+?(\\w+?)\\s*?\\(([^)]*)\\)[\\w\\s,]*?";
    final Pattern pattern = Pattern.compile(regex);
    if(hasMain(content)) return false;

    final List<String> lines = StringUtil.contentToLines(content);
    for(String eachLine : lines){
      final Matcher matcher   = pattern.matcher(eachLine);
      if(matcher.find()) {
        return false;
      }
    }

    return true;
  }

  static List<Class<?>> getClasspath(Set<String> allowedPackages){
    return ClassCatcher.catchClassesInClassPath(allowedPackages);
  }

  static List<MethodFinder> computeMethodFinders(final List<Class<?>> classes){
    final List<Class<?>> targetClasses = classes.isEmpty() ? getClasspath(PackingUtils
          .ALLOWED_PACKAGES) : classes;
    final List<MethodFinder> finders = Lists.newLinkedList();
    for(Class<?> each : targetClasses){
      finders.add(new MethodFinder(each));
    }

    return finders;
  }


  static Set<Method> getMethodsMatchingSignature(List<Class<?>> classpath, Class<?> returnType,
                                                 Class<?>... arguments) {
    final List<MethodFinder> finders = computeMethodFinders(classpath);
    final Set<Method> methods = Sets.newLinkedHashSet();
    for(MethodFinder each : finders){
      methods.addAll(each.findInstanceMethods(each.declaringClass, returnType, arguments));
      methods.addAll(each.findStaticMethods(each.declaringClass, returnType, arguments));
    }

    return methods;
  }


  static class InMemoryPrecomputation implements PackingSpaceGeneration {
    @Override public PackingSpace generateSpace(Set<String> allowedPackages) {
      final List<Class<?>> classpath = ClassCatcher.catchClassesInClassPath(allowedPackages);

      final Map<String, Set<String>> precomputedSpace = Maps.newLinkedHashMap();

      for(Class<?> each : classpath){
        final String pkg = each.getPackage().getName();
        if(null == pkg){ System.out.println(each); }
        if(precomputedSpace.containsKey(pkg)){
          precomputedSpace.get(pkg).add(each.getSimpleName());
        } else {
          try {
            final String name = each.getSimpleName();
            if(StringUtil.isEmpty(name)) continue;
            final Set<String> container = Sets.newLinkedHashSet();
            container.add(name);
            precomputedSpace.put(pkg, container);
          } catch (Throwable e){
            // ignore
          }
        }
      }

      final PackingSpace online  = new InMemoryTypeSpace(precomputedSpace);
      final PackingSpace offline = PackingUtils.failoverTypeSpace();

      return PackingUtils.union(online, offline);
    }
  }

  static class IntersectionBasedMatchingStrategy implements MatchingStrategy {
    @Override public List<String> matches(Set<String> types, PackingSpace typeSpace) {
      final Set<String>       packages  = PackingUtils.getJdkPackages();
      final Map<String, Freq> freq      = Maps.newHashMap();

      final Set<String> result = Sets.newHashSet();
      // if duplicate, the pkg with max number of instances win
      final Map<String, Set<String>> seenNamespace = Maps.newLinkedHashMap();

      // detect used packages
      for (String pkg : packages) {
        final Set<String> namespaces = typeSpace.classSet(pkg);
        if (namespaces.isEmpty()) continue;

        final Set<String> common = Sets.intersection(namespaces, types);
        if (common.isEmpty()) continue;

        Freq t;
        if (freq.containsKey(pkg)) {
          t = freq.get(pkg).update(common);
          freq.put(pkg, t);
        } else {
          t = new Freq(common.size(), common);
          freq.put(pkg, t);
        }

        addSeenBefore(seenNamespace, common, pkg);
      }

      final Map<String, Freq> copy = Maps.newLinkedHashMap(freq);
      // build package directive
      for (String key : freq.keySet()) {
        final Freq record = freq.get(key);
        for(String typeName : record.elements){
          if(PackingUtils.inJavaLang(typeName)) continue;

          if(PackingUtils.inJavaUtil(typeName) && !"java.util".equals(key)){
            continue;
          }

          if(seenNamespace.containsKey(typeName) && !seenNamespace.get(typeName).isEmpty()){
            final Set<String> otherLinkedPackages = seenNamespace.get(typeName);
            if(isThisPackageMax(key, copy, otherLinkedPackages)){
              result.add(key + "." + typeName + ";");
            }
          } else {
            result.add(key + "." + typeName + ";");
          }
        }

      }

      return ImmutableList.copyOf(result);
    }

    static void addSeenBefore(Map<String, Set<String>> source, Set<String> newOnes, String pkg) {

      for (String key : newOnes) {
        if(source.containsKey(key)){ source.get(key).add(pkg); } else {
          final Set<String> pkgs = Sets.newLinkedHashSet();
          pkgs.add(pkg);
          source.put(key, pkgs);
        }
      }
    }

    static boolean isThisPackageMax(String currentPackage, Map<String, Freq> copy, Set<String>
          otherLinkedPackages){
      final int currentPackageWeight = copy.get(currentPackage).val;

      int max = currentPackageWeight;
      for(String otherPackages : otherLinkedPackages){
        final int otherPackageWeight = copy.get(otherPackages).val;
        if(otherPackageWeight > max){
          max = otherPackageWeight;
        }
      }

      return max == currentPackageWeight;
    }

  }

  static class Freq {
    final int val;
    final Set<String> elements;

    Freq(int val, Set<String> elements) {
      this.val = val;
      this.elements = elements;
    }

    Freq update(Set<String> seed) {
      final Set<String> merged = Sets.union(this.elements, seed);
      final int freq = merged.size();

      return new Freq(freq, merged);
    }
  }

  /**
   * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
   */
  static class ClassCatcher {
    private List<Class<?>>  listOfClasses;
    private Set<String>     allowedPackages;


    /**
     * Search for the resource with the extension in the classpath. Method
     * self-instantiate factory for every call to ensure thread safety.
     *
     * are required extension should be empty string. Null extension is not
     * allowed and will cause method to fail.
     *
     * @return List of all resources with specified extension.
     */
    static List<Class<?>> catchClassesInClassPath(Set<String> allowedPackages) {
      ClassCatcher factory    = new ClassCatcher();
      factory.listOfClasses   = Lists.newLinkedList();
      factory.allowedPackages = allowedPackages;
      return factory.search();
    }


    /**
     * Search for the resource with the extension in the classpath.
     *
     * are required extension should be empty string. Null extension is not
     * allowed and will cause method to fail.
     *
     * @return List of all resources with specified extension.
     */
    private List<Class<?>> search() {

      final ClassLoader   classloader = this.getClass().getClassLoader();
      try {
        final ImmutableSet<ClassPath.ClassInfo> all = ClassPath.from(classloader)
              .getTopLevelClasses();

        for(ClassPath.ClassInfo each : all){
          if(allowedPackages.contains(each.getPackageName())){
            this.listOfClasses.add(each.load());
          }
        }
      } catch (IOException e) {
        // ignore
      }

      return this.listOfClasses;
    }
  }

  /**
   * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
   */
  static class InMemoryTypeSpace implements PackingSpace {

    private final Map<String, Set<String>> pkgToClasses;

    /**
     * Construct an in-memory type space.
     * @param pkgToClasses a map between a java package and its class members.
     */
    public InMemoryTypeSpace(Map<String, Set<String>> pkgToClasses){
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

  interface ObjectMatcher<T> {
    /**
     * matches an object of type {@code T}.
     * @param that The object to be matched.
     * @return true if matched; false otherwise.
     */
    boolean matches(T that);
  }

  static class MatchMaker {

    /**
     * Creates a method matcher.
     * @param returnType the return type to be matched
     * @param arguments  the method arguments to be matched
     * @return a new method matcher.
     */
    public static ObjectMatcher<Method> methodMatcher(Class<?> returnType, Class<?>... arguments){
      return new MethodMatcher(returnType, arguments);
    }

    /**
     * Creates a method matcher
     */
    static class MethodMatcher implements ObjectMatcher<Method> {
      private final Class<?> returnType;
      private final Set<Class<?>> arguments;

      MethodMatcher(Class<?> returnType, Class<?>... arguments){

        this.returnType = returnType;
        this.arguments  = Sets.newLinkedHashSet(asList(Preconditions.checkNotNull(arguments)));
        Preconditions.checkArgument(!this.arguments.contains(null));

      }
      @Override public boolean matches(Method that) {
        boolean returnTypeIsOk = false;
        for (Class<?> ic : getInterchangeable(returnType))
          if (ic.isAssignableFrom(that.getReturnType()))
            returnTypeIsOk = true;

        if (!returnTypeIsOk)
          return false;

        Class<?>[] methodArguments = that.getParameterTypes();

        if (methodArguments.length != arguments.size())
          return false;

        if (methodArguments.length == 0) {
          return true;
        } else {

          final Collection<List<Class<?>>> permutations = permute(arguments);

          outer:
          for (List<Class<?>> permutation : permutations) {
            for (int i = 0; i < methodArguments.length; i++) {

              boolean canAssign = false;
              for (Class<?> ic : getInterchangeable(permutation.get(i)))
                if (methodArguments[i].isAssignableFrom(ic))
                  canAssign = true;

              if (!canAssign)
                continue outer;
            }
            return true;
          }

          return false;
        }
      }

      private static Collection<List<Class<?>>> permute(Set<Class<?>> arguments){
        return Collections2.orderedPermutations(arguments, new Comparator<Class<?>>() {
          @Override
          public int compare(Class<?> o1, Class<?> o2) {
            if (o1.equals(o2)) {
              return 0;
            }

            if (o1.isAssignableFrom(o2)) {
              return -1;
            } else {
              if (!o2.isAssignableFrom(o2)) {
                throw new IllegalArgumentException(
                      "The classes share no relation"
                );
              }

              return 1;
            }
          }
        });

      }


      /**
       * Returns the autoboxing types
       *
       * @param type the type to autobox :)
       * @return a list of types that it could be
       */
      @SuppressWarnings("InstantiatingObjectToGetClassObject")
      private static Class<?>[] getInterchangeable(Class<?> type) {

        if (type == Boolean.class || type == Boolean.TYPE)
          return new Class<?>[]{Boolean.class, Boolean.TYPE};
        if (type == Character.class || type == Character.TYPE)
          return new Class<?>[]{Character.class, Character.TYPE};
        if (type == Short.class || type == Short.TYPE)
          return new Class<?>[]{Short.class, Short.TYPE};
        if (type == Integer.class || type == Integer.TYPE)
          return new Class<?>[]{Integer.class, Integer.TYPE};
        if (type == Float.class || type == Float.TYPE)
          return new Class<?>[]{Float.class, Float.TYPE};
        if (type == Double.class || type == Double.TYPE)
          return new Class<?>[]{Double.class, Double.TYPE};
        if (type == Void.class || type == Void.TYPE)
          return new Class<?>[]{Void.class, Void.TYPE};
        if(type == new int[0].getClass()){
          return new Class<?>[]{new int[0].getClass(), Integer.TYPE};
        }
        if(type == new char[0].getClass()){
          return new Class<?>[]{new char[0].getClass(), Character.TYPE};
        }

        if(type == new boolean[0].getClass()){
          return new Class<?>[]{new boolean[0].getClass(), Boolean.TYPE};
        }

        return new Class<?>[]{type};
      }
    }
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

      final ObjectMatcher<Method> methodMatcher = MatchMaker.methodMatcher(returnType, arguments);
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
      final ObjectMatcher<Method> methodMatcher = MatchMaker.methodMatcher(returnType, arguments);
      final List<Method> matches = new LinkedList<Method>();

      for (Method method : klass.getMethods())
        if ((method.getModifiers() & Modifier.STATIC) != 0)
          if (methodMatcher.matches(method))
            matches.add(method);

      return matches;
    }
  }
}
