package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.util.Locations;

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


    public static Source createSourceWithUsedField(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tint a = 0;")
                        .append("\tvoid boom(String msg){ a = 1; if(msg.length() > 1) {}}\n")
                        .append("}")
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

    public static Source createSource(String name, StringBuilder builder){
        return new Source(name, builder.toString());
    }
}
