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
