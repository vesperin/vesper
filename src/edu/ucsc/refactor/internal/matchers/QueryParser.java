package edu.ucsc.refactor.internal.matchers;

import com.google.common.base.Preconditions;
import edu.ucsc.refactor.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static edu.ucsc.refactor.internal.matchers.MatchMaker.*;

/**
 * Parses a CSS selector into an NodeMatcher tree.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class QueryParser {
    private final static String[] COMBINATORS = {",", ">", "+", "~", " "};
    private static final String[] ATTRIBUTE_EVALS = new String[]{"=", "!=", "^=", "$=", "*=", "~="};
    //pseudo selectors :first-child, :last-child, :nth-child, ...
    private static final Pattern NTH_AB = Pattern.compile("((\\+|-)?(\\d+)?)n(\\s*(\\+|-)?\\s*\\d+)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern NTH_B  = Pattern.compile("(\\+|-)?(\\d+)");


    private TokenQueue tq;
    private String query;
    private List<NodeMatcher> evals = new ArrayList<NodeMatcher>();

    /**
     * Create a new QueryParser.
     * @param query CSS query
     */
    private QueryParser(String query) {
        this.query = query;
        this.tq    = new TokenQueue(query);
    }

    /**
     * Parse a CSS query into an Evaluator.
     * @param query CSS query
     * @return Evaluator
     */
    public static NodeMatcher parse(String query) {
        QueryParser p = new QueryParser(query);
        return p.parse();
    }

    /**
     * Parse the query
     *
     * @return NodeMatcher
     */
    NodeMatcher parse(){
        tq.consumeWhitespace();

        if (tq.matchesAny(COMBINATORS)) { // if starts with a combinator, use root as elements
            evals.add(isRoot());
            combinator(tq.consume());
        } else {
            findElements();
        }

        while (!tq.isEmpty()) {
            // hierarchy and extras
            boolean seenWhite = tq.consumeWhitespace();

            if (tq.matchesAny(COMBINATORS)) {
                combinator(tq.consume());
            } else if (seenWhite) {
                combinator(' ');
            } else { // E.class, E#id, E[attr] etc. AND
                findElements(); // take next el, #. etc off queue
            }
        }

        if (evals.size() == 1)
            return evals.get(0);

        return MatchMaker.and(evals);
    }

    private static boolean isOr(NodeMatcher matcher){
        return ((AbstractNodeMatcher)matcher).isOrMatcher();
    }


    private void combinator(char combinator) {
        tq.consumeWhitespace();
        String subQuery = consumeSubQuery(); // support multi > children

        NodeMatcher rootEval;    // the new topmost evaluator
        NodeMatcher currentEval; // the evaluator the new eval will be combined to. could be root, or rightmost or.

        NodeMatcher newEval          = parse(subQuery); // the evaluator to add into target evaluator
        boolean replaceRightMost = false;

        if (evals.size() == 1) {
            rootEval = currentEval = evals.get(0);
            // make sure OR (,) has precedence:
            if (isOr(rootEval) && combinator != ',') {
                currentEval = ((AbstractNodeMatcher) currentEval).rightMostMatcher();
                replaceRightMost = true;
            }
        } else {
            rootEval = currentEval = MatchMaker.and(evals);
        }

        evals.clear();

        // for most combinators: change the current eval into an AND of the current eval and the new eval
        if (combinator == '>')
            currentEval = and(newEval, immediateParent(currentEval));
        else if (combinator == ' ')
            currentEval = and(newEval, parent(currentEval));
        else if (combinator == '+')
            currentEval = and(newEval, immediatePreviousSibling(currentEval));
        else if (combinator == '~')
            currentEval = and(newEval, previousSibling(currentEval));
        else if (combinator == ',') { // group or.
            currentEval = or(currentEval, newEval);
        }
        else
            throw new RuntimeException("Unknown combinator: " + combinator);

        if (replaceRightMost)
            ((AbstractNodeMatcher) rootEval).replaceRightMostMatcher(currentEval);
        else rootEval = currentEval;
        evals.add(rootEval);
    }


    private String consumeSubQuery() {
        StringBuilder sq = new StringBuilder();
        while (!tq.isEmpty()) {
            if (tq.matches("("))
                sq.append("(").append(tq.chompBalanced('(', ')')).append(")");
            else if (tq.matches("["))
                sq.append("[").append(tq.chompBalanced('[', ']')).append("]");
            else if (tq.matchesAny(COMBINATORS))
                break;
            else
                sq.append(tq.consume());
        }
        return sq.toString();
    }


    private void findElements() {
        if (tq.matchChomp("#"))
            byId();
        else if (tq.matchChomp("."))
            byClass();
        else if (tq.matchesWord())
            byTag();
        else if (tq.matches("["))
            byAttribute();
        else if (tq.matchChomp("*"))
            allElements();
        else if (tq.matchChomp(":lt("))
            indexLessThan();
        else if (tq.matchChomp(":gt("))
            indexGreaterThan();
        else if (tq.matchChomp(":eq("))
            indexEquals();
        else if (tq.matches(":has("))
            has();
        else if (tq.matches(":contains("))
            contains(false);
//        else if (tq.matches(":containsOwn("))
//            contains(true);
        else if (tq.matches(":matches("))
            matches(false);
//        else if (tq.matches(":matchesOwn("))
//            matches(true);
        else if (tq.matches(":not("))
            not();
        else if (tq.matchChomp(":nth-child("))
            cssNthChild(false, false);
        else if (tq.matchChomp(":nth-last-child("))
            cssNthChild(true, false);
        else if (tq.matchChomp(":nth-of-type("))
            cssNthChild(false, true);
        else if (tq.matchChomp(":nth-last-of-type("))
            cssNthChild(true, true);
        else if (tq.matchChomp(":first-child"))
            evals.add(new Evaluator.IsFirstChild());
        else if (tq.matchChomp(":last-child"))
            evals.add(new Evaluator.IsLastChild());
        else if (tq.matchChomp(":first-of-type"))
            evals.add(new Evaluator.IsFirstOfType());
        else if (tq.matchChomp(":last-of-type"))
            evals.add(new Evaluator.IsLastOfType());
        else if (tq.matchChomp(":only-child"))
            evals.add(new Evaluator.IsOnlyChild());
        else if (tq.matchChomp(":only-of-type"))
            evals.add(new Evaluator.IsOnlyOfType());
        else if (tq.matchChomp(":empty"))
            evals.add(new Evaluator.IsEmpty());
        else if (tq.matchChomp(":root"))
            evals.add(isRoot());
        else // unhandled
            throw new RuntimeException(
                    String.format(
                            "Could not parse query '%s': unexpected token at '%s'",
                            query,
                            tq.remainder()
                    )
            );

    }


    private void byId() {
        final String idValue = tq.consumeCssIdentifier();
        Preconditions.checkState(StringUtil.isStringEmpty(idValue), "byId() was given an empty id.");
        evals.add(id(idValue));
    }


    private void byClass() {
        String className = tq.consumeCssIdentifier();
        Preconditions.checkState(StringUtil.isStringEmpty(className), "byClass() was given an empty class name.");
        evals.add(MatchMaker.className(className.trim().toLowerCase()));
    }


    private void byTag() {
        String tagName = tq.consumeElementSelector();
        Preconditions.checkState(StringUtil.isStringEmpty(tagName), "byTag() was given an empty tag.");

        // namespaces: if element name is "abc:def", selector must be "abc|def", so flip:
        if (tagName.contains("|"))
            tagName = tagName.replace("|", ":");

        evals.add(MatchMaker.tag(tagName.trim().toLowerCase()));
    }

    private void byAttribute() {
        TokenQueue cq = new TokenQueue(tq.chompBalanced('[', ']')); // content queue
        String key = cq.consumeToAny(ATTRIBUTE_EVALS); // eq, not, start, end, contain, match, (no val)
        Preconditions.checkState(StringUtil.isStringEmpty(key), "byAttribute() was given an empty attribute.");
        cq.consumeWhitespace();

        if (cq.isEmpty()) {
            if (key.startsWith("^"))
                evals.add(attributeStarting(key.substring(1)));
            else
                evals.add(attribute(key));
        } else {
            if (cq.matchChomp("="))
                evals.add(new Evaluator.AttributeWithValue(key, cq.remainder()));

            else if (cq.matchChomp("!="))
                evals.add(new Evaluator.AttributeWithValueNot(key, cq.remainder()));

            else if (cq.matchChomp("^="))
                evals.add(new Evaluator.AttributeWithValueStarting(key, cq.remainder()));

            else if (cq.matchChomp("$="))
                evals.add(new Evaluator.AttributeWithValueEnding(key, cq.remainder()));

            else if (cq.matchChomp("*="))
                evals.add(new Evaluator.AttributeWithValueContaining(key, cq.remainder()));

            else if (cq.matchChomp("~="))
                evals.add(new Evaluator.AttributeWithValueMatching(key, Pattern.compile(cq.remainder())));
            else
                throw new RuntimeException(String.format("Could not parse attribute query '%s': unexpected token at '%s'", query, cq.remainder()));
        }
    }

    private void allElements() {
        evals.add(new Evaluator.AllElements());
    }

    // pseudo selectors :lt, :gt, :eq
    private void indexLessThan() {
        evals.add(new Evaluator.IndexLessThan(consumeIndex()));
    }

    private void indexGreaterThan() {
        evals.add(new Evaluator.IndexGreaterThan(consumeIndex()));
    }

    private void indexEquals() {
        evals.add(new Evaluator.IndexEquals(consumeIndex()));
    }


    // :not(selector)
    private void not() {
        tq.consume(":not");
        String subQuery = tq.chompBalanced('(', ')');
        Preconditions.checkState(StringUtil.isStringEmpty(subQuery), ":not(selector) sub-select must not be empty");
        evals.add(MatchMaker.not(parse(subQuery)));
    }

    // pseudo selector :has(el)
    private void has() {
        tq.consume(":has");
        String subQuery = tq.chompBalanced('(', ')');
        Preconditions.checkArgument(StringUtil.isStringEmpty(subQuery), ":has(el) sub-select must not be empty");
        evals.add(hasElement(parse(subQuery)));
    }

    private int consumeIndex() {
        String indexS = tq.chompTo(")").trim();
        Preconditions.checkArgument(StringUtil.isNumeric(indexS), "Index must be numeric");
        return Integer.parseInt(indexS);
    }


    // pseudo selector :contains(text), containsOwn(text)
    private void contains(boolean own) {
        tq.consume(own ? ":containsOwn" : ":contains");
        String searchText = TokenQueue.unescape(tq.chompBalanced('(', ')'));
        Preconditions.checkArgument(StringUtil.isStringEmpty(searchText), ":contains(text) query must not be empty");
        if (own)
            evals.add(containsText(searchText));
        else
            evals.add(containsText(searchText));
    }

    // :matches(regex), matchesOwn(regex)
    private void matches(boolean own) {
        tq.consume(own ? ":matchesOwn" : ":matches");
        String regex = tq.chompBalanced('(', ')'); // don't un-escape, as regex bits will be escaped
        Preconditions.checkArgument(StringUtil.isStringEmpty(regex), ":matches(regex) query must not be empty");

        if (own)
            evals.add(matchesText(Pattern.compile(regex)));
        else
            evals.add(matchesText(Pattern.compile(regex)));
    }


    private void cssNthChild(boolean backwards, boolean ofType) {
        String argS = tq.chompTo(")").trim().toLowerCase();
        java.util.regex.Matcher mAB = NTH_AB.matcher(argS);
        java.util.regex.Matcher mB = NTH_B.matcher(argS);

        final int a, b;

        if ("odd".equals(argS)) {
            a = 2;
            b = 1;
        } else if ("even".equals(argS)) {
            a = 2;
            b = 0;
        } else if (mAB.matches()) {
            a = mAB.group(3) != null ? Integer.parseInt(mAB.group(1).replaceFirst("^\\+", "")) : 1;
            b = mAB.group(4) != null ? Integer.parseInt(mAB.group(4).replaceFirst("^\\+", "")) : 0;
        } else if (mB.matches()) {
            a = 0;
            b = Integer.parseInt(mB.group().replaceFirst("^\\+", ""));
        } else {
            throw new RuntimeException(String.format("Could not parse nth-index '%s': unexpected format", argS));
        }
        if (ofType)
            if (backwards)
                evals.add(new Evaluator.IsNthLastOfType(a, b));
            else
                evals.add(new Evaluator.IsNthOfType(a, b));
        else {
            if (backwards)
                evals.add(new Evaluator.IsNthLastChild(a, b));
            else
                evals.add(new Evaluator.IsNthChild(a, b));
        }
    }

}
