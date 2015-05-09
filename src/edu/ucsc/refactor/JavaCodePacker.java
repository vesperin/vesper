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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class JavaCodePacker implements CodePacker {
  private static final Set<String> ALLOWED_PACKAGES;
  static {
    final Set<String> pkgs = Sets.newLinkedHashSet(
          asList(
                "java.io",
                //"java.lang", //Dont include these; they dont require to be imported
                "java.math",
                "java.net",
                "java.nio",
                "java.text",
                "java.util",
                "java.sql",
                "org.w3c.dom",
                "javax.print",
                "javax.sound",
                "javax.imageio",
                "javax.swing",
                "java.awt",
                "javax.accessibility",
                "org.ietf.jgss",
                "javax.xml",
                "javax.security",
                "javax.crypto",
                "java.security",
                "javax.script",
                "org.xml.sax",
                "javax.jws",
                "java.applet",
                "javax.tools",
                "javax.management",
                "javax.transaction",
                "javax.net",
                "java.rmi",
                "javax.naming",
                "javax.activity",
                "java.beans",
                "javax.activation",
                "com.google.common",
                "com.google.gson",
                "org.eclipse.jdt.core",
                "org.junit",
                "difflib"
          )
    );

    ALLOWED_PACKAGES = ImmutableSet.copyOf(pkgs);
  }


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
    this.typeSpace  = Preconditions.checkNotNull(precomputation).generateSpace(ALLOWED_PACKAGES);
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
    // todo(Huascar) detect if there is a missing method; if there is then include it.
    final String body   = code.getContents();
    final String bottom = "}";

    final String content = top + body + bottom;

    return Source.from(code, content);
  }

  @Override public Source packs(Source code) {
    return packs(code, "");
  }

  @Override public Source unpacks(Source packed) {
    final String addon = existingHeader(packed);

    final String currentContent = packed.getContents();
    final String updatedContent = StringUtil.trim(
          StringUtil.removeEnd(
                StringUtil.removeStart(currentContent, addon),
                "}"
          )
    );

    final Source cropped = Source.from(packed, updatedContent);
    cropped.setName("Scratched.java");
    return cropped;
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
}
