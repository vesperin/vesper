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

    public static Source createSourceForRenamingStuff(){
        final String content = "import java.util.List; \n"
                + "import java.util.Collection; \n"
                + "class Name {\n"
                + "String msg = \"Hi!\";\n"
                + "\tString boom(String msg){ if(null != msg) { return boom(null);} "
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

    public static Source createSource(String name, StringBuilder builder){
        return new Source(name, builder.toString());
    }
}
