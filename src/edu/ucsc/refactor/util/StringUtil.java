package edu.ucsc.refactor.util;

import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.util.regex.Pattern;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class StringUtil {
    public static final char[] LEADING_CHARS = new char[] {' ', '\t', '\r', '\n'};

    private StringUtil(){}

    public static String extractClassName(String content){
        final Iterable<String> chunks = Splitter.on(
                Pattern.compile("\r\n|\n|\r|[^\\S\\n]")
        ).trimResults().split(
                content.substring(0, content.indexOf("{"))
        );
        return Iterables.get(chunks, Iterables.indexOf(chunks, Predicates.equalTo("class")) + 1);
    }

    public static String extractFileName(String filePathName) {
        if ( filePathName == null )
            return null;

        int dotPos      = filePathName.lastIndexOf( '.' );
        int slashPos    = filePathName.lastIndexOf( '\\' );

        if ( slashPos == -1 ){
            slashPos = filePathName.lastIndexOf( '/' );
        }

        if ( dotPos > slashPos ){
            return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0,
                    dotPos );
        }

        return filePathName.substring( slashPos > 0 ? slashPos + 1 : 0 );
    }

    public static int size(String content){
        if(StringUtil.isStringEmpty(content)) return 0;
        return content.length();
    }


    public static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }


    public static boolean isStringEmpty(String input){
        return ((input != null && input.length() == 0) || (input == null));
    }


    public static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public static String trimStart(final String value, char ch){
        return trimStart(value, new char[] {ch});
    }

    /**
     * Trims a value's beginning of all the given chars. Does so repeatedly until no more matches are found.
     *
     * @throws NullPointerException When an argument is null.
     */
    public static String trimStart(final String value, final char[] chars){
        int startIndex = 0;
        while (startIndex <= value.length() - 1 && contains(chars, value.charAt(startIndex)))
            startIndex++;

        return value.substring(startIndex);
    }

    public static boolean contains(final char[] sequence, char ch){
        for (char c : sequence)
            if (c == ch)
                return true;

        return false;
    }


    public static String trimEnd(final String value, char ch){
        return trimEnd(value, new char[] {ch});
    }

    /**
     * Trims a value's tail of all the given chars. Does so repeatedly until no more matches are found.
     *
     * @throws NullPointerException When an argument is null.
     */
    public static String trimEnd(final String value, final char[] chars){
        int endIndex = value.length() - 1;
        while (endIndex > 0 && contains(chars, value.charAt(endIndex)))
            endIndex--;

        return value.substring(0, endIndex + 1);
    }


    /**
     * Trims a value of all whitespace chars, i.e. ' ', '\t', '\r', '\n'. Does so repeatedly until no more matches are found.
     *
     * @throws NullPointerException When an argument is null.
     */
    public static String trim(String value){
        return trim(value, LEADING_CHARS);
    }

    /**
     * Trims a value of all the given chars. Does so repeatedly until no more matches are found.
     *
     * @throws NullPointerException When an argument is null.
     */
    public static String trim(String value, char[] chars){
        return trimEnd(trimStart(value, chars), chars);
    }


}
