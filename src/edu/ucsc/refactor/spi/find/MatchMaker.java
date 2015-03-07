package edu.ucsc.refactor.spi.find;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
class MatchMaker {

  final static Set<String> allowedPackages = Sets.newLinkedHashSet(asList(
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
  ));

  private MatchMaker() {}

  /**
   * Creates a package matcher.
   * @param allowedPackages packages that should be inspected.
   * @return a new package matcher object.
   */
  public static Matcher<String> exactPackagesMatch(Set<String> allowedPackages){
    return new PackageMatcher(allowedPackages);
  }

  /**
   * Creates a package matcher.
   * @return a new 'ONLY ALLOWED' package matcher object.
   */
  public static Matcher<String> allowedPackages(){
    return MatchMaker.exactPackagesMatch(allowedPackages);
  }

  /**
   * Creates a method matcher.
   * @param returnType the return type to be matched
   * @param arguments  the method arguments to be matched
   * @return a new method matcher.
   */
  public static Matcher<Method> methodMatcher(Class<?> returnType, Class<?>... arguments){
    return new MethodMatcher(returnType, arguments);
  }


  static class PackageMatcher implements Matcher<String> {
    final Set<String> allowedPackages;
    PackageMatcher(Set<String> allowedPackages){
      this.allowedPackages = Preconditions.checkNotNull(allowedPackages);
    }

    @Override public boolean matches(String that) {
      boolean allowed = false;
      for (String pkg : allowedPackages) {
        allowed |= that.startsWith(pkg) && !that.startsWith("edu.ucsc.refactor");
      }

      return allowed;
    }
  }

  /**
   * Creates a method matcher
   */
  static class MethodMatcher implements Matcher<Method> {
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
