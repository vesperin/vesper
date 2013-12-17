package edu.ucsc.refactor.internal.detectors;

import edu.ucsc.refactor.Source;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class DetectorsTestUtil {
    private DetectorsTestUtil(){}

    static Source createSourceWithMagicNumber(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tvoid boom(String msg){ if(msg.length() > 1) {}}\n")
                        .append("}")
        );
    }

    static Source createSourceNoIssues(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("}")
        );
    }

    static Source createSourceWithUnusedMethodAndParameter(){
        return createSource(
                "Name.java",
                new StringBuilder("class Name {\n")
                        .append("\tvoid boom(String msg){}\n")
                        .append("}")
        );
    }

    static Source createSourceWithOneUnusedImportDirective(){
        return createSource(
                "Name.java",
                new StringBuilder("import java.util.List; \n")
                        .append("class Name {\n")
                        .append("\tvoid boom(String msg){}\n")
                        .append("}")
        );
    }

    static Source createSource(String name, StringBuilder builder){
        return new Source(name, builder.toString());
    }
}
