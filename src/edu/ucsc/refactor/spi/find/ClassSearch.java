package edu.ucsc.refactor.spi.find;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
class ClassSearch {

  /**
   * List of the classes found in the classpath.
   */
  private List<Class<?>> list;

  /**
   * Extension of the resource to be found in the classpath.
   */
  private String extension;

  private Matcher<String> allowedPackages;


  /**
   * Search for the resource with the extension in the classpath. Method
   * self-instantiate factory for every call to ensure thread safety.
   *
   * are required extension should be empty string. Null extension is not
   * allowed and will cause method to fail.
   *
   * @return List of all resources with specified extension.
   */
  public static List<Class<?>> searchClassPath() {
    ClassSearch factory     = new ClassSearch();
    factory.list            = Lists.newLinkedList();
    factory.allowedPackages = MatchMaker.allowedPackages();
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
         if(allowedPackages.matches(each.getPackageName())){
           this.list.add(each.load());
         }
      }
    } catch (IOException e) {
      // ignore
    }

    return this.list;
  }

}
