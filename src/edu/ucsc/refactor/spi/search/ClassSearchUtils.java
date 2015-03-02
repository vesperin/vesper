package edu.ucsc.refactor.spi.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassSearchUtils {

    final static Set<String> allowedPackages = Sets.newHashSet(
            "java.io",
            "java.lang",
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
    );

    /**
     * List of the classes found in the classpath.
     */
    private List<Class<?>> list;

    /**
     * Extension of the resource to be found in the classpath.
     */
    private String extension;

    private String prefix;

    /**
     * Search for the resource with the extension in the classpath. Method
     * self-instantiate factory for every call to ensure thread safety.
     *
     * @return List of all resources with specified extension.
     */
    public static List<Class<?>> searchClassPath() {
        return searchClassPath("", ".class");
    }

    /**
     * Search for the resource with the extension in the classpath. Method
     * self-instantiate factory for every call to ensure thread safety.
     *
     * @param prefix basic filter
     * @return List of all resources with specified extension.
     */
    public static List<Class<?>> searchClassPath(String prefix) {
        return searchClassPath(prefix, ".class");
    }

    /**
     * Search for the resource with the extension in the classpath. Method
     * self-instantiate factory for every call to ensure thread safety.
     *
     * @param extension Mandatory extension of the resource. If all resources
     * are required extension should be empty string. Null extension is not
     * allowed and will cause method to fail.
     *
     * @return List of all resources with specified extension.
     */
    public static List<Class<?>> searchClassPath(String prefix, String extension) {
        ClassSearchUtils factory = new ClassSearchUtils();
        factory.prefix = prefix;
        return factory.find(extension);
    }

    /**
     * Search for the resource with the extension in the classpath.
     *
     * @param extension Mandatory extension of the resource. If all resources
     * are required extension should be empty string. Null extension is not
     * allowed and will cause method to fail.
     *
     * @return List of all resources with specified extension.
     */
    private List<Class<?>> find(String extension) {
        this.extension   = extension;
        this.list        = Lists.newLinkedList();

        final ClassLoader   classloader = this.getClass().getClassLoader();
        String              classpath   = System.getProperty("java.class.path");

        try {
            final Class<? extends ClassLoader> klassLoader = classloader.getClass();
            Method method = klassLoader.getMethod("getClassPath", (Class<?>) null);
            if (method != null) {
                classpath = (String) method.invoke(classloader, (Object) null);
            }
        } catch (Exception e) {
            // ignore
        }

        if (classpath == null) {
            classpath = System.getProperty("java.class.path");
        }

        StringTokenizer tokenizer = new StringTokenizer(
                classpath,
                File.pathSeparator
        );

        String token;
        File dir;
        String name;

        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            dir = new File(token);
            if (dir.isDirectory()) {
                lookInDirectory("", dir);
            }

            if (dir.isFile()) {
                name = dir.getName().toLowerCase();
                if (name.endsWith(".zip") || name.endsWith(".jar")) {
                    this.lookInArchive(dir);
                }
            }
        }

        return this.list;
    }

    /**
     * @param name Name of to parent directories in java class notation (dot
     * separator)
     * @param dir Directory to be searched for classes.
     */
    private void lookInDirectory(String name, File dir) {
        File[] files = dir.listFiles();

        if(files == null) return;

        for(File file : files){
            String fileName = file.getName();
            if (file.isFile() && fileName.toLowerCase().endsWith(this.extension)) {
                try {
                    if (this.extension.equalsIgnoreCase(".class")) {
                        fileName = fileName.substring(0, fileName.length() - 6);
                        // filter ignored resources
                        if (!(name + fileName).startsWith(this.prefix)) {
                            continue;
                        }

                        this.list.add(Class.forName(name + fileName));
                    }
                } catch (ClassNotFoundException e) {
                    // ignore
                } catch (NoClassDefFoundError e) {
                    //ignore too
                }
            }

            // search recursively.
            // I don't like that but we will see how it will work.
            if (file.isDirectory()) {
                lookInDirectory(name + fileName + ".", file);
            }
        }



    }

    /**
     * Search archive files for required resource.
     * @param archive Jar or zip to be searched for classes or other resources.
     */
    private void lookInArchive(File archive) {
        JarFile jarFile;
        try {
            jarFile = new JarFile(archive);
        } catch (IOException e) {
            return;
        }

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry  entry       = entries.nextElement();
            String    entryName   = entry.getName();

            if (entryName.toLowerCase().endsWith(this.extension)) {
                try {
                    if (this.extension.equalsIgnoreCase(".class")) {
                        // convert name into java classloader notation
                        entryName = entryName.substring(0, entryName.length() - 6);
                        entryName = entryName.replace('/', '.');

                        // filter ignored resources
                        if (!entryName.startsWith(this.prefix)) {
                            continue;
                        }

                        boolean allowed = false;
                        for (String pkg : allowedPackages)
                            allowed |= entryName.startsWith(pkg);

                        if (allowed){
                            this.list.add(Class.forName(entryName));
                        }

                    } // ignore other extensions
                } catch (Throwable e){
                    // ignore
                }
            }

        }
    }
}
