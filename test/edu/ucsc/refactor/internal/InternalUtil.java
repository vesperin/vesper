package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.util.Locations;
import edu.ucsc.refactor.util.SourceFormatter;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class InternalUtil {
    private InternalUtil(){}


    public static Location locateWord(Source code, String word){
        return Locations.locateWord(code, word).get(0);
    }

    public static Source createGeneralSource(){
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = \"Hi!\";\n"
                + "\tString boom(String msg){ if(null != msg) { return boom(null);} "
                + "return \"Hi!\";}\n"
                + "\t/** {@link Name#boom(String)}**/String baam(String msg){ "
                + " return msg; }\n"
                + "}";

        return new Source("Name.java", content);
    }


    public static Source createGeneralCropableSource(){
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = \"Hi!\";\n"
                + "\tString boom(String msg){ if(null != msg) { return boom(null);} "
                + "return \"Hi!\";}\n"
                + "\t/** {@link Name#boom(String)}**/String baam(String msg){ "
                + " return msg; }\n"
                + "String beem(String text){ return boom(text); }"
                + "}";

        return new Source("Name.java", content);
    }


    public static Source createGeneralCropableSource2(){
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = \"Hi!\";\n"
                + "\tString boom(String msg){ if(null != msg) { return beem(null);} "
                + "return \"Hi!\";}\n"
                + "\t/** {@link Name#boom(String)}**/String baam(String msg){ "
                + " return msg; }\n"
                + "String beem(String text){ return this.msg + text; }"
                + "}";

        return new Source("Name.java", content);
    }


    public static Source createGeneralCropableSource3(){
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = \"Hi!\";\n"
                + "\tString boom(String msg){ if(null != msg) { return beem(null);} "
                + "return \"Hi!\";}\n"
                + "\t/** {@link Name#boom(String)}**/String baam(String msg){ "
                + " return msg; }\n"
                + "String beem(String text){ check(true, null); return this.msg + text; }"
                + "\tstatic void check(\n"
                + "\t\tboolean cond, String message\n"
                + "\t) throws RuntimeException {\n"
                + "\t\tcond = !cond;"
                + "\t\tif(!cond) throw new IllegalArgumentException();\n"
                + "\t}\n"
                + "}";

        return new Source("Name.java", content);
    }

    public static Source createGeneralSourceWithInvalidSelection(){
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = \"Hi!\";\n"
                + "\tString boom(){ if(null != msg) { return boom(null);} "
                + "return \"Hi!\";}\n"
                + "\t/** {@link Name#boom(String)}**/String baam(String msg){ this.msg = msg "
                + "+ (msg+this.msg); return boom(this.msg); }\n"
                + "}";

        return new Source("Name.java", content);
    }


    public static Source createSourceWithMagicNumber(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tvoid boom(String msg){ if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }


    public static Source createSourceWithUnusedField(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tint a = 0;")
                        .append("\tvoid boom(String msg){ if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }

    public static Source createScratchedSourceWithOneMethod(){

        final String content = "class ScratchedCodeSnippet {\n" +
                "\tpublic static void main(String[] args) {\n" +
                "\t\tint[] arr = { 12, 23, 43, 34, 3, 6, 7, 1, 9, 6 };\n" +
                "\t\t{\n" +
                "\t\t\tint temp;\n" +
                "\t\t\tfor (int i = 0; i < arr.length; i++) {\n" +
                "\t\t\t\tfor (int j = 0; j < arr.length - i; j++) {\n" +
                "\t\t\t\t\tif (arr[j] > arr[j + 1]) {\n" +
                "\t\t\t\t\t\ttemp = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = temp;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        return createSource(
                "ScratchedCodeSnippet.java",
                new StringBuilder(content)
        );
    }


    public static Source createScratchedSourceForClipping(){

        final String content = "class ScratchedCodeSnippet {\n" +
                "  public static void main(String[] args) {\n" +
                "    int[] arr = {12, 23, 43, 34, 3, 6, 7, 1, 9, 6};\n" +
                "    {\n" +
                "      int temp;\n" +
                "      for (int i = 0; i < arr.length; i++) {\n" +
                "        for (int j = 0; j < arr.length - i; j++) {\n" +
                "          if (arr[j] > arr[j + 1]) {\n" +
                "            temp = arr[j];\n" +
                "            arr[j + 1] = arr[j];\n" +
                "            arr[j + 1] = temp;\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    for (int i = 0; i < arr.length; i++) {\n" +
                "      System.out.print(arr[i] + \" \");\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        return createSource(
                "ScratchedCodeSnippet.java",
                new StringBuilder(content)
        );
    }


    public static Source createScratchedSourceWithOneFieldAccessedInMethod(){

        final String content = "class ScratchedCodeSnippet {\n" +
                "\tint[] arr = null;" +
                "\tpublic void sort(String[] args) {\n" +
                "\t\tarr = new int[]{ 12, 23, 43, 34, 3, 6, 7, 1, 9, 6 };\n" +
                "\t\t{\n" +
                "\t\t\tint temp;\n" +
                "\t\t\tfor (int i = 0; i < arr.length; i++) {\n" +
                "\t\t\t\tfor (int j = 0; j < arr.length - i; j++) {\n" +
                "\t\t\t\t\tif (arr[j] > arr[j + 1]) {\n" +
                "\t\t\t\t\t\ttemp = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = temp;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        return createSource(
                "ScratchedCodeSnippet.java",
                new StringBuilder(content)
        );
    }


    public static Source createBrokenBubbleSortSource() {
        final String content = "class ScratchedCodeSnippet {\n" +
                "\tpublic static void main(String[] arguments) {\n" +
                "\t\tint[] arr = { 12, 23, 43, 34, 3, 6, 7, 1, 9, 6 };\n" +
                "\t\t{\n" +
                "\t\t\tint temp;\n" +
                "\t\t\tfor (int i = 0; i < arr.length; i++) {\n" +
                "\t\t\t\tfor (int j = 0; j < arr.length - i; j++) {\n" +
                "\t\t\t\t\tif (arr[j] > arr[j + 1]) {\n" +
                "\t\t\t\t\t\ttemp = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = temp;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\tfor (int i = 0; i < arr.length; i++) {\n" +
                "\t\t\tSystem.out.print(arr[i] + \" \");\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        return createSource("ScratchedCodeSnippet.java", new StringBuilder(content));
    }


    public static Source createQuickSortSource(){
        final String content = "import java.util.Random;\n" +
                "\n" +
                "public class Quicksort {\n" +
                "  private static Random rand = new Random();\n" +
                "\n" +
                "  public static void quicksort(int[] arr, int left, int right) {\n" +
                "    if (left < right) {\n" +
                "      int pivot = randomizedPartition(arr, left, right);\n" +
                "      quicksort(arr, left, pivot);\n" +
                "      quicksort(arr, pivot + 1, right);\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  private static int randomizedPartition(int[] arr, int left, int right) {\n" +
                "    int swapIndex = left + rand.nextInt(right - left) + 1;\n" +
                "    swap(arr, left, swapIndex);\n" +
                "    return partition(arr, left, right);\n" +
                "  }\n" +
                "\n" +
                "  private static int partition(int[] arr, int left, int right) {\n" +
                "    int pivot = arr[left];\n" +
                "    int i = left - 1;\n" +
                "    int j = right + 1;\n" +
                "    while (true) {\n" +
                "      do\n" +
                "        j--;\n" +
                "      while (arr[j] > pivot);\n" +
                "\n" +
                "      do\n" +
                "        i++;\n" +
                "      while (arr[i] < pivot);\n" +
                "\n" +
                "      if (i < j)\n" +
                "        swap(arr, i, j);\n" +
                "      else\n" +
                "        return j;\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  private static void swap(int[] arr, int i, int j) {\n" +
                "    int tmp = arr[i];\n" +
                "    arr[i] = arr[j];\n" +
                "    arr[j] = tmp;\n" +
                "  }\n" +
                "\n" +
                "  // Sort 100k elements that are in reversed sorted order\n" +
                "  public static void main(String[] args) {\n" +
                "    int arr[] = new int[100000];\n" +
                "    for (int i = 0; i < arr.length; i++)\n" +
                "      arr[i] = arr.length - i;\n" +
                "\n" +
                "    System.out.println(\"First 20 elements\");\n" +
                "    System.out.print(\"Before sort: \");\n" +
                "    for (int i = 0; i < 20; i++)\n" +
                "      System.out.print(arr[i] + \" \");\n" +
                "    System.out.println();\n" +
                "\n" +
                "    quicksort(arr, 0, arr.length - 1);\n" +
                "    System.out.print(\"After sort: \");\n" +
                "    for (int i = 0; i < 20; i++)\n" +
                "      System.out.print(arr[i] + \" \");\n" +
                "    System.out.println();\n" +
                "  }\n" +
                "\n" +
                "}\n";

        return createSource("Quicksort.java", new StringBuilder(new SourceFormatter().format(content)));
    }


    public static Source createBrokenBubbleSortSource2() {
        final String content = "class BubbleSort {\n" +
                "\tpublic static void main(String[] arguments) {\n" +
                "\t\tint[] arr = { 12, 23, 43, 34, 3, 6, 7, 1, 9, 6 };\n" +
                "\t}\n" +
                "}";

        return createSource("BubbleSort.java", new StringBuilder(content));
    }


    public static Source createBrokenBubbleSortSource3() {
        final String content = "class BubbleSort {\n\tpublic static void sort() {\n\t\tint[] arr = { 12, 23, 43, 34, 3, 6, 7, 1, 9, 6 };\n\t}\n}";

        return createSource("BubbleSort.java", new StringBuilder(content));
    }


    public static Source createBrokenSourceWithOneMethod(){

        final String content = "class ScratchedCodeSnippet {\n" +
                "\tpublic static void main(String[] args) {\n" +
                "\t\tint[] arr = { 12, 23, 43, 34, 3, 6, 7, 1, 9, 6 };\n" +
                "\t\t{\n" +
                "\t\t\tint temp;\n" +
                "\t\t\tfor (int i = 0; i < arr.length; i++) {\n" +
                "\t\t\t\tfor (int j = 0; j < arr.length - i; j++) {\n" +
                "\t\t\t\t\tif (arr[j] > arr[j + 1]) {\n" +
                "\t\t\t\t\t\ttemp = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = arr[j];\n" +
                "\t\t\t\t\t\tarr[j + 1] = temp;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n";

        return createSource(
                "ScratchedCodeSnippet.java",
                new StringBuilder(content)
        );
    }


    public static Source createSourceWithUsedField() {
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tint a = 0;")
                        .append("\tvoid boom(String msg){ a = 1; if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }

    public static Source createSourceWithMissingImports(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name implements Comparator {\n")
                        .append("\tint a = 0;")
//                        .append("\tint compare(Object a, Object c){ return 0; }")
                        .append("\tvoid boom(String msg){ a = 1; if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }


    public static Source createSourceWithOnlyStatements(){
        return createSource(
                "Name.java",
                new StringBuilder("\n")
                        .append("\tList<String> list = new ArrayList<String>();\n")
                        .append("\tSet<String> list = new HashSet<String>();\n")
                        .append("\tMap<String,String> map = new HashMap<String, String>();\n")
                        .append("\tTreeSet<String> list = new TreeSet<String>();\n")
                        .append("\tString toString = toString(list);\n")
                        .append("\tpublic int compare(Object a, String c){ return 0; }")
//                        .append("\tvoid boom(String msg){ a = 1; if(msg.length() > 1) {}}\n")
                        .append("")
        );
    }


    public static Source createSourceWithCommentsAndStatements(){
        return createSource(
                "Sample.java",
                new StringBuilder("\n")
                        .append("\tList<String> list = new ArrayList<String>();\n")
                        .append("\tSet<String> list = new HashSet<String>();\n")
                        .append("\tMap<String,String> map = new HashMap<String, String>();\n")
                        .append("\tTreeSet<String> list = new TreeSet<String>();\n")
                        .append("\n\tthis is a simple program\n\n")
                        .append("\tpublic int compare(Object a, String c){ return 0; }")
                        .append("")
        );
    }


    public static Source createSourceWithSomeUsedFieldAndLocalVariable(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tint a = 0;")
                        .append("\tvoid boom(String msg){ a = 1; int b = 0; b = 1; if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }


    public static Source createSourceWithSomeUnUsedLocalVariable(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tint a = 0;")
                        .append("\tvoid boom(String msg){ a = 1; int b = 0; if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }


    public static Source createSourceWithSomeConstructorInvocation(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tint a = 0;\n")
                        .append("\tName(){}")
                        .append("\tvoid boom(String msg){ final Name old = new Name(); \n")
                        .append("a = 1; int b = 0; b = 1; if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }


    public static Source createSourceForClippingAtClassLevel(){
        String content = "import java.util.*;\n" +
                "import java.lang.*;\n" +
                "\n" +
                "class ComparatorSorter {\n" +
                "  private static <K, V extends Comparable<V>> Map<K, V> sortByComparator(Map<K, V> unsortMap) {\n" +
                "    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());\n" +
                "    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {\n" +
                "      @Override\n" +
                "      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {\n" +
                "        return o1.getValue().compareTo(o2.getValue());\n" +
                "      }\n" +
                "    });\n" +
                "    Map<K, V> sortedMap = new LinkedHashMap<K, V>();\n" +
                "    for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {\n" +
                "      Map.Entry<K, V> entry = it.next();\n" +
                "      sortedMap.put(entry.getKey(), entry.getValue());\n" +
                "    }\n" +
                "    return sortedMap;\n" +
                "  }\n" +
                "}";

        return createSource(
                "ComparatorSorter.java",
                new StringBuilder(content)
        );
    }


    public static Source createSourceWithJavaDocs(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\t/** {@link Name#boom(String)} **/")
                        .append("\tvoid boom(){}\n")
                        .append("\tvoid baam(){ boom(); }\n")
                        .append("}")
        );
    }


    public static Source createSourceWithDuplicatedMethods(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\t/** {@link Name#boom(String)} **/")
                        .append("\tvoid boom(){ System.out.println(1); }\n")
                        .append("\tvoid baam(){ System.out.println(1); }\n")
                        .append("\tvoid beem(){ System.out.println(1); }\n")
                        .append("\tvoid buum(){ baam(); }\n")
                        .append("}")
        );
    }


    public static Source createSourceNoIssues(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("}")
        );
    }

    public static Source createSourceWithUnusedMethodAndParameter(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tvoid boom(String msg){}\n")
                        .append("\tvoid baam(String msg){ boom(msg);}\n")
                        .append("}")
        );
    }


    public static Source createSourceWithUsedMethodAndParameter(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tvoid boom(String msg){ System.out.println(msg);}\n")
                        .append("\tvoid baam(String msg){ boom(msg);}\n")
                        .append("}")
        );
    }

    public static Source createSourceWithOneUnusedImportDirective(){
        return createSource(
                "Name.java",
                new StringBuilder("import java.util.List; \n")
                        .append("class Name {\n")
                        .append("\tvoid boom(String msg){}\n")
                        .append("}")
        );
    }


    public static Source createSourceWithGenerics(){
        final String content = "import java.util.PriorityQueue;\nimport java.util.List;\nimport java.util.ArrayList;\nimport java.util.Collections;\n\nclass Vertex implements Comparable<Vertex>\n{\n    public final String name;\n    public Edge[] adjacencies;\n    public double minDistance = Double.POSITIVE_INFINITY;\n    public Vertex previous;\n    public Vertex(String argName) { name = argName; }\n    public String toString() { return name; }\n    public int compareTo(Vertex other)\n    {\n        return Double.compare(minDistance, other.minDistance);\n    }\n\n}\n\n\nclass Edge\n{\n    public final Vertex target;\n    public final double weight;\n    public Edge(Vertex argTarget, double argWeight)\n    { target = argTarget; weight = argWeight; }\n}\n\npublic class Dijkstra\n{\n    public static void computePaths(Vertex source)\n    {\n        source.minDistance = 0.;\n        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();\n    vertexQueue.add(source);\n\n    while (!vertexQueue.isEmpty()) {\n        Vertex u = vertexQueue.poll();\n\n            // Visit each edge exiting u\n            for (Edge e : u.adjacencies)\n            {\n                Vertex v = e.target;\n                double weight = e.weight;\n                double distanceThroughU = u.minDistance + weight;\n        if (distanceThroughU < v.minDistance) {\n            vertexQueue.remove(v);\n\n            v.minDistance = distanceThroughU ;\n            v.previous = u;\n            vertexQueue.add(v);\n        }\n            }\n        }\n    }\n\n    public static List<Vertex> getShortestPathTo(Vertex target)\n    {\n        List<Vertex> path = new ArrayList<Vertex>();\n        for (Vertex vertex = target; vertex != null; vertex = vertex.previous)\n            path.add(vertex);\n\n        Collections.reverse(path);\n        return path;\n    }\n\n    public static void main(String[] args)\n    {\n        // mark all the vertices \n        Vertex A = new Vertex(\"A\");\n        Vertex B = new Vertex(\"B\");\n        Vertex D = new Vertex(\"D\");\n        Vertex F = new Vertex(\"F\");\n        Vertex K = new Vertex(\"K\");\n        Vertex J = new Vertex(\"J\");\n        Vertex M = new Vertex(\"M\");\n        Vertex O = new Vertex(\"O\");\n        Vertex P = new Vertex(\"P\");\n        Vertex R = new Vertex(\"R\");\n        Vertex Z = new Vertex(\"Z\");\n\n        // set the edges and weight\n        A.adjacencies = new Edge[]{ new Edge(M, 8) };\n        B.adjacencies = new Edge[]{ new Edge(D, 11) };\n        D.adjacencies = new Edge[]{ new Edge(B, 11) };\n        F.adjacencies = new Edge[]{ new Edge(K, 23) };\n        K.adjacencies = new Edge[]{ new Edge(O, 40) };\n        J.adjacencies = new Edge[]{ new Edge(K, 25) };\n        M.adjacencies = new Edge[]{ new Edge(R, 8) };\n        O.adjacencies = new Edge[]{ new Edge(K, 40) };\n        P.adjacencies = new Edge[]{ new Edge(Z, 18) };\n        R.adjacencies = new Edge[]{ new Edge(P, 15) };\n        Z.adjacencies = new Edge[]{ new Edge(P, 18) };\n\n\n        computePaths(A); // run Dijkstra\n        System.out.println(\"Distance to \" + Z + \": \" + Z.minDistance);\n        List<Vertex> path = getShortestPathTo(Z);\n        System.out.println(\"Path: \" + path);\n    }\n}";
        return createSource("Vertex.java", new StringBuilder(content));
    }


    public static Source createSourceWithOneUsedStaticNestedClass(){
        final String content = "import java.util.List; \n" +
                "class Preconditions {\n" +
                "\t\tPreconditions(B b){}" +
                "\tstatic void check(\n" +
                "\t\tboolean cond, String message\n" +
                "\t) throws RuntimeException {\n" +
                "\t\tB bbb = new B(); cond = !cond;" +
                "\t\tif(!cond) throw new IllegalArgumentException();\n" +
                "\t}\n" +
                "\tstatic void check2(\n" +
                "B b){}" +
                "\tstatic class B{}\n" +
                "}";

        return createSource(
                "Preconditions.java",
                new StringBuilder(content)
        );
    }


    public static Source createSourceWithOneUnusedStaticNestedClass(){
        final String content = "import java.util.List; \n" +
                "class Preconditions {\n" +
                "\tstatic void check(\n" +
                "\t\tboolean cond, String message\n" +
                "\t) throws RuntimeException {\n" +
                "\t\tcond = !cond;" +
                "\t\tif(!cond) throw new IllegalArgumentException();\n" +
                "\t}\n" +
                "\tstatic class B{}\n" +
                "}";

        return createSource(
                "Preconditions.java",
                new StringBuilder(content)
        );
    }

    public static Source createSourceUsingStackoverflowExample(){
        final String content = "import java.io.*;\n" +
                "import java.util.Arrays;\n" +
                "\n" +
                "public class MergeSort {\n" +
                "\n" +
                "\tpublic static void main(String[] args) throws IOException {\n" +
                "\t\tBufferedReader R = new BufferedReader(new InputStreamReader(System.in));\n" +
                "\t\tint arraySize = Integer.parseInt(R.readLine());\n" +
                "\t\tint[] inputArray = new int[arraySize];\n" +
                "\t\tfor (int i = 0; i < arraySize; i++) {\n" +
                "\t\t\tinputArray[i] = Integer.parseInt(R.readLine());\n" +
                "\t\t}\n" +
                "\t\tmergeSort(inputArray);\n" +
                "\n" +
                "\t\tfor (int j = 0; j < inputArray.length; j++) {\n" +
                "\t\t\tSystem.out.println(inputArray[j]);\n" +
                "\t\t}\n" +
                "\n" +
                "\t}\n" +
                "\n" +
                "\tstatic void mergeSort(int[] A) {\n" +
                "\t\tif (A.length > 1) {\n" +
                "\t\t\tint q = A.length / 2;\n" +
                "\t\t\tint[] leftArray = Arrays.copyOfRange(A, 0, q);\n" +
                "\t\t\tint[] rightArray = Arrays.copyOfRange(A, q + 1, A.length);\n" +
                "\t\t\tmergeSort(leftArray);\n" +
                "\t\t\tmergeSort(rightArray);\n" +
                "\t\t\tA = merge(leftArray, rightArray);\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\n" +
                "\tstatic int[] merge(int[] l, int[] r) {\n" +
                "\t\tint totElem = l.length + r.length;\n" +
                "\t\tint[] a = new int[totElem];\n" +
                "\t\tint i, li, ri;\n" +
                "\t\ti = li = ri = 0;\n" +
                "\t\twhile (i < totElem) {\n" +
                "\t\t\tif ((li < l.length) && (ri < r.length)) {\n" +
                "\t\t\t\tif (l[li] < r[ri]) {\n" +
                "\t\t\t\t\ta[i] = l[li];\n" +
                "\t\t\t\t\ti++;\n" +
                "\t\t\t\t\tli++;\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\ta[i] = r[ri];\n" +
                "\t\t\t\t\ti++;\n" +
                "\t\t\t\t\tri++;\n" +
                "\t\t\t\t}\n" +
                "\t\t\t} else {\n" +
                "\t\t\t\tif (li >= l.length) {\n" +
                "\t\t\t\t\twhile (ri < r.length) {\n" +
                "\t\t\t\t\t\ta[i] = r[ri];\n" +
                "\t\t\t\t\t\ti++;\n" +
                "\t\t\t\t\t\tri++;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t\tif (ri >= r.length) {\n" +
                "\t\t\t\t\twhile (li < l.length) {\n" +
                "\t\t\t\t\t\ta[i] = l[li];\n" +
                "\t\t\t\t\t\tli++;\n" +
                "\t\t\t\t\t\ti++;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t\treturn a;\n" +
                "\n" +
                "\t}\n" +
                "\n" +
                "}";

        return createSource(
                "MergeSort.java",
                new StringBuilder(content)
        );

    }


    public static Source createSourceWithShortNameMembers(){
        final String content = "class Quicksort {\n" +
                "  public static void qsort(int[] arrayOfIntegers, int si, int ei) {\n" +
                "    if (ei <= si || si >= ei) {\n" +
                "    } else {\n" +
                "      int pivot = arrayOfIntegers[si];\n" +
                "      int i = si + 1;\n" +
                "      int tmp;\n" +
                "      for (int j = si + 1; j <= ei; j++) {\n" +
                "        if (pivot > arrayOfIntegers[j]) {\n" +
                "          tmp = arrayOfIntegers[j];\n" +
                "          arrayOfIntegers[j] = arrayOfIntegers[i];\n" +
                "          arrayOfIntegers[i] = tmp;\n" +
                "          i++;\n" +
                "        }\n" +
                "      }\n" +
                "      arrayOfIntegers[si] = arrayOfIntegers[i - 1];\n" +
                "      arrayOfIntegers[i - 1] = pivot;\n" +
                "      qsort(arrayOfIntegers, si, i - 2);\n" +
                "      qsort(arrayOfIntegers, i, ei);\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        return createSource("Quicksort.java", new StringBuilder(content));
    }


    public static Source createSourceWithShortNameMembers3(){
        String content = "class ScratchedCodeSnippet {\n" +
                "  public static void main(String[] args) {\n" +
                "    int[] arr = {12, 23, 43, 34, 3, 6, 7, 1, 9, 6};\n" +
                "    {\n" +
                "      int temp;\n" +
                "      for (int idx = 0; idx < arr.length; idx++) {\n" +
                "        for (int j = 0; j < arr.length - idx; j++) {\n" +
                "          if (arr[j] > arr[j + 1]) {\n" +
                "            temp = arr[j];\n" +
                "            arr[j + 1] = arr[j];\n" +
                "            arr[j + 1] = temp;\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    for (int idx = 0; idx < arr.length; idx++) {\n" +
                "      System.out.print(arr[idx] + \" \");\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        return createSource("ScratchedCodeSnippet.java", new StringBuilder(content));
    }


    public static Source createSourceWithShortNameMembers2(){
        String content = "class Bubblesort {\n" +
                "  public static void sort(String[] args) {\n" +
                "    int[] arrayOfIntegers = {12, 23, 43, 34, 3, 6, 7, 1, 9, 6};\n" +
                "    {\n" +
                "      int tempVar;\n" +
                "      for (int i = 0; i < arrayOfIntegers.length; i++) {\n" +
                "        for (int j = 0; j < arrayOfIntegers.length - i; j++) {\n" +
                "          if (arrayOfIntegers[j] > arrayOfIntegers[j + 1]) {\n" +
                "            tempVar = arrayOfIntegers[j];\n" +
                "            arrayOfIntegers[j + 1] = arrayOfIntegers[j];\n" +
                "            arrayOfIntegers[j + 1] = tempVar;\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    for (int i = 0; i < arrayOfIntegers.length; i++) {\n" +
                "      System.out.print(arrayOfIntegers[i] + \" \");\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        return createSource("BubbleSort.java", new StringBuilder(content));
    }

    public static Source createSortingFromLowestToHighest(){
        String content = "import java.util.*;\n" +
                "import java.lang.*;\n" +
                "\n" +
                "class Sorting {\n" +
                "  private Sorting() {\n" +
                "    throw new Error(\"This class cannot be instantiated; it is a util class!\");\n" +
                "  }\n" +
                "\n" +
                "  static void sort(Integer[] arr) {\n" +
                "    Arrays.sort(arr, new Comparator<Integer>() {\n" +
                "      @Override\n" +
                "      public int compare(Integer x, Integer y) {\n" +
                "        return x - y;\n" +
                "      }\n" +
                "    });\n" +
                "  }\n" +
                "\n" +
                "  public static void main(String... args) {\n" +
                "    Integer[] arr = {12, 67, 1, 34, 9, 78, 6, 31};\n" +
                "    Sorting.sort(arr);\n" +
                "    System.out.println(\"low to high:\" + Arrays.toString(arr));\n" +
                "  }\n" +
                "\n" +
                "}\n";

        return createSource("Sorting.java", new StringBuilder(content));
    }

    public static Source createSourceWithStaticNestedClass_ClippingEntireInnerClass(){
       String content = "import java.util.ArrayList;\n" +
               "import java.util.Collections;\n" +
               "import java.util.List;\n" +
               "\n" +
               "public class Runme {\n" +
               "\n" +
               "  public static void main(String args[]) {\n" +
               "\n" +
               "    ToSort toSort1 = new ToSort(new Float(3), \"3\");\n" +
               "    ToSort toSort2 = new ToSort(new Float(6), \"6\");\n" +
               "    ToSort toSort3 = new ToSort(new Float(9), \"9\");\n" +
               "    ToSort toSort4 = new ToSort(new Float(1), \"1\");\n" +
               "    ToSort toSort5 = new ToSort(new Float(5), \"5\");\n" +
               "    ToSort toSort6 = new ToSort(new Float(0), \"0\");\n" +
               "    ToSort toSort7 = new ToSort(new Float(3), \"3\");\n" +
               "    ToSort toSort8 = new ToSort(new Float(-3), \"-3\");\n" +
               "\n" +
               "    List<ToSort> sortList = new ArrayList<ToSort>();\n" +
               "    sortList.add(toSort1);\n" +
               "    sortList.add(toSort2);\n" +
               "    sortList.add(toSort3);\n" +
               "    sortList.add(toSort4);\n" +
               "    sortList.add(toSort5);\n" +
               "    sortList.add(toSort6);\n" +
               "    sortList.add(toSort7);\n" +
               "    sortList.add(toSort8);\n" +
               "\n" +
               "    Collections.sort(sortList);\n" +
               "\n" +
               "    for (ToSort toSort : sortList) {\n" +
               "      System.out.println(toSort.toString());\n" +
               "    }\n" +
               "  }\n" +
               "\n" +
               "  public static class ToSort implements Comparable {\n" +
               "\n" +
               "    private Float val;\n" +
               "    private String id;\n" +
               "\n" +
               "    public ToSort(Float val, String id) {\n" +
               "      this.val = val;\n" +
               "      this.id = id;\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public int compareTo(Object o) {\n" +
               "\n" +
               "      ToSort f = (ToSort) o;\n" +
               "\n" +
               "      if (val.floatValue() > f.val.floatValue()) {\n" +
               "        return 1;\n" +
               "      } else if (val.floatValue() < f.val.floatValue()) {\n" +
               "        return -1;\n" +
               "      } else {\n" +
               "        return 0;\n" +
               "      }\n" +
               "\n" +
               "    }\n" +
               "\n" +
               "    @Override\n" +
               "    public String toString() {\n" +
               "      return this.id;\n" +
               "    }\n" +
               "  }\n" +
               "\n" +
               "}\n";

        return createSource("Runme.java", new StringBuilder(content));
    }

    public static Source createToSortSource(){
        String content = "import java.util.ArrayList;\n" +
                "import java.util.Collections;\n" +
                "import java.util.List;\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class ToSort implements Comparable {\n" +
                "\n" +
                "    private Float val;\n" +
                "    private String id;\n" +
                "\n" +
                "    public ToSort(Float val, String id) {\n" +
                "      this.val = val;\n" +
                "      this.id = id;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int compareTo(Object o) {\n" +
                "\n" +
                "      ToSort f = (ToSort) o;\n" +
                "\n" +
                "      if (val.floatValue() > f.val.floatValue()) {\n" +
                "        return 1;\n" +
                "      } else if (val.floatValue() < f.val.floatValue()) {\n" +
                "        return -1;\n" +
                "      } else {\n" +
                "        return 0;\n" +
                "      }\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public String toString() {\n" +
                "      return this.id;\n" +
                "    }\n" +
                "  }";

        return createSource("ToSort.java", new StringBuilder(content));
    }


    public static Source createSourceWithOptimizationBug(){
        String content = "import java.util.*;\n" +
                "import java.lang.*;\n" +
                "import java.util.regex.*;\n" +
                "import java.text.*; \n" +
                "class Tunein {\n" +
                "  public static void main(String[] args) throws Exception {\n" +
                "    List<String> values = new ArrayList<String>();\n" +
                "    values.add(\"AB\");\n" +
                "    values.add(\"A012B\");\n" +
                "    values.add(\"CD\");\n" +
                "    values.add(\"1\");\n" +
                "    values.add(\"10\");\n" +
                "    values.add(\"01\");\n" +
                "    values.add(\"9\");\n" +
                "\n" +
                "    int maxLen = 0;\n" +
                "    for (String string : values) {\n" +
                "        if (string.length() > maxLen) {\n" +
                "            maxLen = string.length();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    Collections.sort(values, new MyComparator(maxLen));\n" +
                "\n" +
                "    System.out.println(values);\n" +
                "}\n" +
                "  \n" +
                "  public static String leftPad(String stringToPad, String padder, Integer size) {\n" +
                "\n" +
                "    final StringBuilder strb = new StringBuilder(size.intValue());\n" +
                "    final StringCharacterIterator sci = new StringCharacterIterator(padder);\n" +
                "\n" +
                "    while (strb.length() < (size.intValue() - stringToPad.length())) {\n" +
                "        for (char ch = sci.first(); ch != CharacterIterator.DONE; ch = sci.next()) {\n" +
                "            if (strb.length() < (size.intValue() - stringToPad.length())) {\n" +
                "                strb.insert(strb.length(), String.valueOf(ch));\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    return strb.append(stringToPad).toString();\n" +
                "}\n" +
                "  \n" +
                "  public static class MyComparator implements Comparator<String> {\n" +
                "    private int maxLen;\n" +
                "    private static final String REGEX = \"[0-9]+\";\n" +
                "\n" +
                "    public MyComparator(int maxLen) {\n" +
                "        this.maxLen = maxLen;\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public int compare(String obj1, String obj2) {\n" +
                "        String o1 = obj1;\n" +
                "        String o2 = obj2;\n" +
                "        // both numbers\n" +
                "        if (o1.matches(\"[1-9]+\") && o2.matches(\"[1-9]+\")) {\n" +
                "            Integer integer1 = Integer.valueOf(o1);\n" +
                "            Integer integer2 = Integer.valueOf(o2);\n" +
                "            return integer1.compareTo(integer2);\n" +
                "        }\n" +
                "\n" +
                "        // both string\n" +
                "        if (o1.matches(\"[a-zA-Z]+\") && o2.matches(\"[a-zA-Z]+\")) {\n" +
                "            return o1.compareTo(o2);\n" +
                "        }\n" +
                "\n" +
                "        Pattern p = Pattern.compile(REGEX);\n" +
                "        Matcher m1 = p.matcher(o1);\n" +
                "        Matcher m2 = p.matcher(o2);\n" +
                "\n" +
                "        List<String> list = new ArrayList<String>();\n" +
                "        while (m1.find()) {\n" +
                "            list.add(m1.group());\n" +
                "        }\n" +
                "        for (String string : list) {\n" +
                "            o1.replaceFirst(string, leftPad(string, \"0\", maxLen));\n" +
                "        }\n" +
                "\n" +
                "        list.clear();\n" +
                "\n" +
                "        while (m2.find()) {\n" +
                "            list.add(m2.group());\n" +
                "        }\n" +
                "        for (String string : list) {\n" +
                "            o2.replaceFirst(string, leftPad(string, \"0\", maxLen));\n" +
                "        }\n" +
                "        return o1.compareTo(o2);\n" +
                "\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "\n" +
                "}";

        return createSource("Tunein.java", new StringBuilder(content));
    }


    public static Source createGeneralBrokenSource(){
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = ...\n"
                + "\tString boom(String msg){ if(null != msg) { return boom(null);} "
                + "return \"Hi!\";}\n"
                + "\t/** {@link Name#boom(String)}**/String baam(String msg){ "
                + " return msg; }\n"
                + "}";

        return new Source("Name.java", content);
    }


    public static Source createMethodOnlyCodeExample(){
        final String content = "public void greet(){\n" +
                "\tSystem.out.println(\"Hello, world!\");\n" +
                "}";

        return  new Source("EditMe.java", content);
    }


    public static Source createMethodWithShellCodeExample(){
        final String content = "class WellManners {\n" +
                "  public void greet() {\n" +
                "    System.out.println(\"Hello, world!\");\n" +
                "  }\n" +
                "}";

        return  new Source("WellManners.java", content);
    }

    public static Source createIncompleteQuickSortCodeExample(){
        final String content = "private static Random rand = new Random();\n" +
                "\n" +
                "public static void quicksort(int[] arr, int left, int right)\n" +
                "{\n" +
                "\t\t\tif (left < right)\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\tint pivot = randomizedPartition(arr, left, right);\n" +
                "\t\t\t\t\tquicksort(arr, left, pivot);\n" +
                "\t\t\t\t\tquicksort(arr, pivot + 1, right);\n" +
                "\t\t\t}\n" +
                "}\n" +
                "\n" +
                "private static int randomizedPartition(int[] arr, int left, int right)\n" +
                "{\n" +
                "\t\t\tint swapIndex = left + rand.nextInt(right - left) + 1;\n" +
                "\t\t\tswap(arr, left, swapIndex);\n" +
                "\t\t\treturn partition(arr, left, right);\n" +
                "}\n" +
                "\n" +
                "private static int partition(int[] arr, int left, int right)\n" +
                "{\n" +
                "\t\t\tint pivot = arr[left];\n" +
                "\t\t\tint i = left - 1;\n" +
                "\t\t\tint j = right + 1;\n" +
                "\t\t\twhile (true)\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\tdo\n" +
                "\t\t\t\t\t\t\t\tj--;\n" +
                "\t\t\t\t\twhile (arr[j] > pivot);\n" +
                "\n" +
                "\t\t\t\t\tdo\n" +
                "\t\t\t\t\t\t\t\ti++;\n" +
                "\t\t\t\t\twhile (arr[i] < pivot);\n" +
                "\n" +
                "\t\t\t\t\tif (i < j)\n" +
                "\t\t\t\t\t\t\t\tswap(arr, i, j);\n" +
                "\t\t\t\t\telse\n" +
                "\t\t\t\t\t\t\t\treturn j;\n" +
                "\t\t\t}\n" +
                "}\n" +
                "\n" +
                "private static void swap(int[] arr, int i, int j)\n" +
                "{\n" +
                "\t\t\tint tmp = arr[i];\n" +
                "\t\t\tarr[i] = arr[j];\n" +
                "\t\t\tarr[j] = tmp;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "public static void main(String[] args)\n" +
                "{\n" +
                "\t\t\tint arr[] = new int[100000];\n" +
                "\t\t\tfor (int i = 0; i < arr.length; i++)\n" +
                "\t\t\t\t\tarr[i] = arr.length - i;\n" +
                "\n" +
                "\t\t\tSystem.out.println(\"First 20 elements\");\n" +
                "\t\t\tSystem.out.print(\"Before sort: \");\n" +
                "\t\t\tfor (int i = 0; i < 20; i++)\n" +
                "\t\t\t\t\tSystem.out.print(arr[i] + \" \");\n" +
                "\t\t\tSystem.out.println();\n" +
                "\n" +
                "\t\t\tquicksort(arr, 0, arr.length - 1);\n" +
                "\t\t\tSystem.out.print(\"After sort: \");\n" +
                "\t\t\tfor (int i = 0; i < 20; i++)\n" +
                "\t\t\t\t\tSystem.out.print(arr[i] + \" \");\n" +
                "\t\t\tSystem.out.println();\n" +
                "}";

        return new Source("Scratched.java", content);
    }

    public static Source updatedIncompleteQuickSortCodeExample(){
        final String content = "private static Random rand = new Random();\n" +
                "\n" +
                "  public static void quicksort(int[] arr, int left, int right) {\n" +
                "\tif (left < right) {\n" +
                "\t\tint pivot = randomPartition(arr, left, right);\n" +
                "\t\tquicksort(arr, left, pivot);\n" +
                "\t\tquicksort(arr, pivot + 1, right);\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "  private static int randomPartition(int[] arr, int left, int right) {\n" +
                "\tint swapIndex = left + rand.nextInt(right - left) + 1;\n" +
                "\tswap(arr, left, swapIndex);\n" +
                "\treturn partition(arr, left, right);\n" +
                "}\n" +
                "\n" +
                "  private static int partition(int[] arr, int left, int right) {\n" +
                "\tint pivot = arr[left];\n" +
                "\tint i = left - 1;\n" +
                "\tint j = right + 1;\n" +
                "\twhile (true) {\n" +
                "\t\tdo\n" +
                "\t\t\tj--;\n" +
                "\t\twhile (arr[j] > pivot);\n" +
                "\t\tdo\n" +
                "\t\t\ti++;\n" +
                "\t\twhile (arr[i] < pivot);\n" +
                "\t\tif (i < j)\n" +
                "\t\t\tswap(arr, i, j);\n" +
                "\t\telse\n" +
                "\t\t\treturn j;\n" +
                "\t}\n" +
                "}\n" +
                "\n" +
                "  private static void swap(int[] arr, int i, int j) {\n" +
                "\tint tmp = arr[i];\n" +
                "\tarr[i] = arr[j];\n" +
                "\tarr[j] = tmp;\n" +
                "}";

        return new Source("Scratched.java", content);
    }


    public static Source createIncompleteCodeExampleWithUnusedNestedClass(){
       String content = "public static void greet(String message) {\n" +
               "  System.out.println(message);\n" +
               "}\n" +
               "\n" +
               "static class Wow {\n" +
               "\tString statement = null;\n" +
               "}";

        return new Source("Scratched.java", content);
    }


    public static Source createIncompleteCodeExampleWithUsedNestedClass(){
        String content = "public static void greet(String message) {\n" +
                "  Wow wow = new Wow();\n" +
                "\twow.statement = message;\n" +
                "  System.out.println(wow.statement);\n" +
                "}\n" +
                "\n" +
                "static class Wow {\n" +
                "\tString statement = null;\n" +
                "}";

        return new Source("Scratched.java", content);
    }



    public static Source createSource(String name, StringBuilder builder){
        return new Source(name, builder.toString());
    }
}
