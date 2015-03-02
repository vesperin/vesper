package edu.ucsc.refactor.spi.search;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */

import com.google.common.collect.Collections2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A method finder class
 */
class MethodMatcher {

    public final Class<?> klass;

    /**
     * Constructs the method finder (doh)
     *
     * @param klass the class
     */
    MethodMatcher(Class<?> klass) {
        this.klass = klass;
    }

    /**
     * Finds instance method matches
     *
     * @param returnType the return type
     * @param arguments  the arguments (in any order)
     * @return list of instance methods
     */
    public List<Method> findInstanceMethods(Class<?> returnType, Class<?>... arguments) {

        List<Method> matches = new LinkedList<Method>();

        for (Method method : klass.getMethods()) {
            if ((method.getModifiers() & Modifier.STATIC) == 0)
                if (matches(method, returnType, arguments))
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
    public List<Method> findStaticMethods(Class<?> returnType, Class<?>... arguments) {

        List<Method> matches = new LinkedList<Method>();

        for (Method method : klass.getMethods())
            if ((method.getModifiers() & Modifier.STATIC) != 0)
                if (matches(method, returnType, arguments))
                    matches.add(method);

        return matches;
    }

    /**
     * Tests a method if it is a match
     *
     * @param method     the method to test
     * @param returnType the return type
     * @param arguments  the arguments (in any order)
     * @return true if it matches
     */
    private boolean matches(Method method, Class<?> returnType, Class<?>... arguments) {

        boolean returnTypeIsOk = false;
        for (Class<?> ic : getInterchangeable(returnType))
            if (ic.isAssignableFrom(method.getReturnType()))
                returnTypeIsOk = true;

        if (!returnTypeIsOk)
            return false;

        Class<?>[] methodArguments = method.getParameterTypes();

        if (methodArguments.length != arguments.length)
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

    private static Collection<List<Class<?>>> permute(Class<?>... arguments){
        return Collections2.orderedPermutations(Arrays.asList
                (arguments), new Comparator<Class<?>>() {
            @Override public int compare(Class<?> o1, Class<?> o2) {
                if (o1.equals(o2)) { return 0; }

                if (o1.isAssignableFrom(o2)) { return -1; } else {
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

        return new Class<?>[]{type};
    }

}

