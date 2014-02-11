package edu.ucsc.refactor.util;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class StringUtil {
    private StringUtil(){}

    public static String extractName(String filePathName) {
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


    /**
     * Tests if a code point is "whitespace" as defined in the HTML spec.
     * @param c code point to test
     * @return true if code point is whitespace, false otherwise
     */
    public static boolean isWhitespace(int c){
        return c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r';
    }


    /**
     * Tests if a string is numeric, i.e. contains only digit characters
     * @param string string to test
     * @return true if only digit chars, false if empty or null or contains non-digit characters
     */
    public static boolean isNumeric(String string) {
        if (string == null || string.length() == 0)
            return false;

        int l = string.length();
        for (int i = 0; i < l; i++) {
            if (!Character.isDigit(string.codePointAt(i))) {
                return false;
            }
        }

        return true;
    }



    public static boolean isStringEmpty(String input){
        return ((input != null && input.length() == 0) || (input == null));
    }


    public static boolean equals(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }

}
