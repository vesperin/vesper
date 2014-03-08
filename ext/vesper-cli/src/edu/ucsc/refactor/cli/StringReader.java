package edu.ucsc.refactor.cli;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Parses a string that corresponds to a Vesper command
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class StringReader {
    static final String WHITESPACE_AND_QUOTES_DELIMITER = " \t\r\n\"[],";
    static final String QUOTES_ONLY_DELIMITER           = "\"";
    static final String DOUBLE_QUOTE                    = "\"";
    static final String NOTHING                         = "";

    private final StringBuilder parentheses = new StringBuilder();

    public Iterable<String> process(String statement){
        final List<String> result = Lists.newArrayList();

        String  delimiter = WHITESPACE_AND_QUOTES_DELIMITER;

        final StringTokenizer parser = new StringTokenizer(
                statement,
                delimiter,
                true/*=>return Tokens*/
        );

        while (parser.hasMoreTokens()) {

            String token = process(parser.nextToken(delimiter), delimiter, parser);

            if(!isDoubleQuote(token)){ addNonTrivialWordToResult(token, result); } else {
                delimiter = flipDelimiters(delimiter);
            }
        }

        parentheses.delete(0, parentheses.length());
        return result;
    }

    private String process(String token, String delimiter, StringTokenizer parser){
        String curatedToken;
        if("[".equals(token)){
            cacheContentInParen(token);
            do {
                token = parser.nextToken(delimiter);
                cacheContentInParen(token);
                curatedToken = releaseContentInParen(token);
            } while(!isParenGone() && curatedToken == null);
            return curatedToken;
        } else {
            return token;
        }


    }

    private boolean isParenGone(){
        return parentheses.toString().isEmpty();
    }

    private void cacheContentInParen(String left){
        if(textHasContent(left)){
            if("[".equals(left)) {
                parentheses.append(left);
            } else if(!isParenGone()){
                parentheses.append(left);
            }
        }
    }

    private String releaseContentInParen(String text){
        if("]".equals(text)){
            return popContent();
        }

        return null;
    }

    private String popContent(){
        return parentheses.toString();
    }

    private String flipDelimiters(String currentDelimiter){
        return (currentDelimiter.equals(WHITESPACE_AND_QUOTES_DELIMITER)
                ? QUOTES_ONLY_DELIMITER
                : WHITESPACE_AND_QUOTES_DELIMITER
        );
    }

    private boolean isDoubleQuote(String token){
        return token.equals(DOUBLE_QUOTE);
    }


    private boolean textHasContent(String text){
        return (text != null) && (!text.trim().equals(NOTHING));
    }

    private void addNonTrivialWordToResult(String token, List<String> result){
        if (textHasContent(token)) {
            result.add(token.trim());
        }
    }
}
