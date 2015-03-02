package edu.ucsc.refactor.spi.search;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Searcher {
    private final List<MethodMatcher> klasses;

    /**
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Searcher() throws IOException, ClassNotFoundException {
        this.klasses = new LinkedList<MethodMatcher>();

        final List<Class<?>> rawClasses = ClassSearchUtils.searchClassPath();
        for(Class<?> each : rawClasses){
            klasses.add(new MethodMatcher(each));
        }
    }


    public void buildQuery(Class<?> returnType, Class<?>... arguments) {
        final Set<String> definitions = Sets.newHashSet();

        for (int i = 0; i < arguments.length; i++)
            System.out.print((i == 0 ? "" : ", ") + arguments[i].getSimpleName());

        System.out.println(" -> " + returnType.getSimpleName());

        Set<Method> methods = findMethods(returnType, arguments);

        for (Method method : methods){
            System.out.println("\t" + method);
        }

        System.out.println();
    }



    /**
     * Finds a set of methods
     * @param returnType the return type
     * @param arguments the arguments (in any order)
     * @return a set of methods
     */
    public Set<Method> findMethods(Class<?> returnType, Class<?>... arguments) {

        Set<Method> methods = new LinkedHashSet<Method>();

        if (arguments.length > 0) {
            // the first argument is the class
            final Class<?>      declaringClass  = arguments[0];
            final MethodMatcher instance        = new MethodMatcher(declaringClass);

            // the rest are the actual method parameters
            final Class<?>[] rest = new Class<?>[arguments.length - 1];
            System.arraycopy(arguments, 1, rest, 0, rest.length);

            methods.addAll(instance.findInstanceMethods(returnType, rest));
        } else {
            for (MethodMatcher k : klasses)
                methods.addAll(k.findInstanceMethods(returnType, arguments));
        }

        for (MethodMatcher k : klasses)
            methods.addAll(k.findStaticMethods(returnType, arguments));

        return methods;
    }



    public static void main(String... args) throws Exception {
        Searcher m = new Searcher();


        // print some examples
//        m.buildQuery(Integer.class, new int[0].getClass());
        m.buildQuery(String.class, String.class, Character.class, Character.class);
        m.buildQuery(int.class, String.class, int.class);
//        m.buildQuery(Integer.class, String.class);
//        m.buildQuery(Void.class, List.class);
//        m.buildQuery(Void.class, List.class);
//        m.buildQuery(ImmutableList.class);
    }

}
