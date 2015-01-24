package edu.ucsc.refactor.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;
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


    public static List<String> normalize(Iterable<String> docs){
        final List<String> n = Lists.newArrayList();
        for(String each : docs){
            // normalize line endings
            n.add(each.replaceAll("\r\n", "\n"));
        }

        return n;
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

    /**
     * <p>Removes a substring only if it is at the start of a source string,
     * otherwise returns the source string.</p>
     *
     * <p>A <code>null</code> source string will return <code>null</code>.
     * An empty ("") source string will return the empty string.
     * A <code>null</code> search string will return the source string.</p>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <code>null</code> if null String input
     */
    public static String removeStart(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.startsWith(remove)){
            return str.substring(remove.length());
        }
        return str;
    }

    /**
     * <p>Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.</p>
     *
     * <p>A <code>null</code> source string will return <code>null</code>.
     * An empty ("") source string will return the empty string.
     * A <code>null</code> search string will return the source string.</p>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <code>null</code> if null String input
     */
    public static String removeEnd(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private static List<String> prependPrefix(final String prefix, List<String> toElements){
        final List<String> addons = Lists.transform(toElements, new Function<String, String>(){
                    @Override public String apply(String s) {
                        return StringUtil.trim(prefix) + " " + s;
                    }}
        );

        return Lists.newArrayList(addons);
    }

    /**
     * Builds the content to be prepended to an incomplete code example.
     *
     * @param withName the name to use
     * @param withPrefix with prefix to prepend
     * @param withImports the imports to use
     * @return the content to be prepended.
     */
    public static String concat(String withName, boolean withPrefix, List<String> withImports){
        final List<String> addons = withPrefix ? prependPrefix("import", withImports) : withImports;
        addons.add("\nclass " + withName + " {\n");

        return Joiner.on('\n').join(addons);
    }


    /**
     * Returns the offset of a string
     * @param string the string of interest
     * @return the total offset
     */
    public static int offsetOf(String string){
       final String[] split = string.split("(?!^)");

       int offset = -1;
       for (String each : split){
           offset = string.indexOf(each, offset + 1); // avoid duplicates
       }

       return offset;
    }
}
