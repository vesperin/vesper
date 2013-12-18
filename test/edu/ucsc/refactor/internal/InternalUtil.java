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
        return Locations.locateWord(code, word);
    }


    public static Source createSourceWithMagicNumber(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tvoid boom(String msg){ if(msg.length() > 1) {}}\n")
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

    public static Source createSource(String name, StringBuilder builder){
        return new Source(name, builder.toString());
    }
}
