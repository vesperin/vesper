package edu.ucsc.refactor.spi.find;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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

  /**
   * Method name prefix
   */
  private String prefix;

  private Matcher<String> allowedPackages;

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
    ClassSearch factory          = new ClassSearch();
    factory.prefix          = prefix;
    factory.allowedPackages = MatchMaker.allowedPackages();
    return factory.search(extension);
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
  private List<Class<?>> search(String extension) {
    this.extension   = extension;
    this.list        = Lists.newLinkedList();

    String              classpath   = System.getProperty("java.class.path");

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
        this.lookInDirectory("", dir);
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

            if(allowedPackages.matches(entryName)){
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
