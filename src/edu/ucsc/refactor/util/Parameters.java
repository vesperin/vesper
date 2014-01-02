package edu.ucsc.refactor.util;

import com.google.common.base.Joiner;
import edu.ucsc.refactor.Parameter;

import java.util.Collections;
import java.util.Map;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Parameters {

    private static final Parameter.Constraint CONSTRAINT = new Parameter.Constraint() {
        @Override public boolean isValid(Object value) {
            return ((String) value).matches("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");
        }
    };

    public static final String METHOD_NEW_NAME         = "New method name";
    public static final String PARAMETER_NEW_NAME      = "New method name";
    public static final String FIELD_NEW_NAME          = "New method name";
    public static final String PARAMETER_CONSTANT_NAME = "Constant name";

    private Parameters(){}

    public static Map<String, Parameter> newMethodName(String value){
        return createParameter(METHOD_NEW_NAME, value, CONSTRAINT);
    }


    public static Map<String, Parameter> newParameterName(String value){
        return createParameter(PARAMETER_NEW_NAME, value, CONSTRAINT);
    }

    public static Map<String, Parameter> newFieldName(String value){
        return createParameter(FIELD_NEW_NAME, value, CONSTRAINT);
    }


    public static Map<String, Parameter> newRandomConstantName(){
        final String name = "CONSTANT_" + HumanNumber.formatNumberToEnglish();
        return createParameter(PARAMETER_CONSTANT_NAME, name, CONSTRAINT);
    }


    private static Map<String, Parameter> createParameter(String key, String value,
                                                          Parameter.Constraint constraint){
        final Parameter parameterObject = new Parameter(key, value/*new name*/);

        parameterObject.getConstraints().add(constraint);

        return Collections.singletonMap(key, parameterObject);
    }


    /**
     * Turns numerical numbers into English-spoken strings, and then connects them
     * using {@code _}.
     *
     * @see {@code http://www.jibble.org/humannumber/HumanNumber.java}
     */
    static class HumanNumber {
        static final String[] UNITS = {"zero", "one", "two", "three", "four", "five", "six",
                "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen",
                "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};

        static final String[] TENS = {"zero", "ten", "twenty", "thirty", "forty", "fifty",
                "sixty", "seventy", "eighty", "ninety"};

        static final String[] ORDERS = {"thousand", "million", "billion", "trillion",
                "quadrillion", "quintillion", "sextillion", "septillion", "octillion",
                "nonillion", "decillion", "undecillion", "duodecillion", "tredecillion",
                "quattuordecillion", "quindecillion", "sexdecillion", "septendecillion",
                "octodecillion", "novemdecillion", "vigintillion"};

        private HumanNumber(){}

        static String formatNumberToEnglish(){
            final int max = 100;
            final int min = 1;
            int r = min + (int) (Math.random() * (max-min));   // between 1 and 99
            final String result = format(r);
            final String[] split = result.split(" ");
            return Joiner.on("_").join(split).toUpperCase();
        }

        static String format(int input) {
            return format(String.valueOf(input));
        }

        static String format(String input) {
            if ((input.length() + 2) / 3 - 1 > ORDERS.length) {
                throw new IllegalArgumentException("Number too big.");
            }

            final StringBuilder result = new StringBuilder();
            int i = input.length();
            int order = -1;

            while (i >= 3) {
                int a = charToInt(input.charAt(i - 3));
                int b = charToInt(input.charAt(i - 2));
                int c = charToInt(input.charAt(i - 1));

                final String number = format(a, b, c);

                if (order >= 0 && !"".equals(number)) {
                    result.insert(0, " " + ORDERS[order]);
                }

                result.insert(0, number);

                if (order == -1 && i > 3 && a == 0 && (b != 0 || c != 0)) {
                    result.insert(0, " and ");
                } else if (i > 3 && (a != 0 || b != 0 || c != 0)) {
                    result.insert(0, ", ");
                }

                order++;
                i = i - 3;

            }

            if(i > 0){
                if (order >= 0) {
                    result.insert(0, " " + ORDERS[order]);
                }

                if (i == 2) {
                    result.insert(0, format(0, charToInt(input.charAt(0)), charToInt(input.charAt(1))));
                } else if (i == 1) {
                    result.insert(0, format(0, 0, charToInt(input.charAt(0))));
                }
            }

            return result.toString();

        }


        private static String format(int a, int b, int c) {
            String result = "";
            if (b == 1) {
                result = UNITS[10 + c];
            } else {
                if (c != 0) {
                    result = UNITS[c];
                }
                if (b >= 2) {
                    if (c != 0) {
                        result = " " + result;
                    }
                    result = TENS[b] + result;
                }
            }

            if (a != 0) {
                if (b != 0 || c != 0) {
                    result = " and " + result;
                }
                result = UNITS[a] + " hundred" + result;
            }
            return result;
        }


        private static int charToInt(char ch) {
            return ch - '0';
        }
    }

}
