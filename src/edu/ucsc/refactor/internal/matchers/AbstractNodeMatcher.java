package edu.ucsc.refactor.internal.matchers;

import com.google.common.base.Objects;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public abstract class AbstractNodeMatcher implements NodeMatcher {
    protected final ArrayList<NodeMatcher> matchers;
    protected int num = 0;

    /**
     * Construct an {@code AbstractNodeMatcher}
     */
    protected AbstractNodeMatcher(){
        matchers = new ArrayList<NodeMatcher>();
    }

    /**
     * @param that The other matcher.
     * @return a new matcher which returns {@code true} if both this and that given
     *      matcher return {@code true} .
     */
    public NodeMatcher and(NodeMatcher that){
        return new AndMatcher(this, that);
    }

    /**
     * @return {@code true} if dealing with {@code OrMatcher}, {@code false} otherwise.
     */
    protected boolean isOrMatcher(){
        return this instanceof OrMatcher;
    }

    /**
     *
     * @return {@code true} if dealing with {@code AndMatcher}, {@code false} otherwise.
     */
    protected boolean isAndMatcher(){
        return this instanceof AndMatcher;
    }


    /**
     * @param that The other matcher.
     * @return a new matcher which returns {@code true} if either this or that given
     *      matcher return {@code true} .
     */
    public NodeMatcher or(NodeMatcher that){
        return new OrMatcher(this, that);
    }


    NodeMatcher rightMostMatcher() {
        return num > 0 ? matchers.get(num - 1) : null;
    }

    void replaceRightMostMatcher(NodeMatcher replacement) {
        matchers.set(num - 1, replacement);
    }


    void updateNumMatchers() {
        // used so we don't need to bash on size() for every match test
        num = matchers.size();
    }


    /**
     * The AndMatcher
     */
    private static class AndMatcher extends AbstractNodeMatcher {
        private final NodeMatcher a, b;

        public AndMatcher(NodeMatcher a, NodeMatcher b) {
            super();
            this.a = a;
            this.b = b;

            matchers.add(a);
            matchers.add(b);
            updateNumMatchers();
        }

        @Override public boolean match(ASTNode root, ASTNode t) {
            return a.match(root, t) && b.match(root, t);
        }

        @Override public boolean equals(Object other) {
            return other instanceof AndMatcher
                    && ((AndMatcher) other).a.equals(a)
                    && ((AndMatcher) other).b.equals(b);
        }

        @Override public int hashCode() {
            return Objects.hashCode(a.hashCode(), b.hashCode());
        }

        @Override public String toString() {
            return ":and(" + a + ", " + b + ")";
        }

    }

    /**
     * The OrMatcher
     */
    private static class OrMatcher extends AbstractNodeMatcher {
        private final NodeMatcher a, b;

        public OrMatcher(NodeMatcher a, NodeMatcher b) {
            super();
            this.a = a;
            this.b = b;

            matchers.add(a);
            matchers.add(b);
            updateNumMatchers();
        }

        @Override public boolean match(ASTNode root, ASTNode t) {
            return a.match(root, t) || b.match(root, t);
        }

        @Override public boolean equals(Object other) {
            return other instanceof OrMatcher
                    && ((OrMatcher) other).a.equals(a)
                    && ((OrMatcher) other).b.equals(b);
        }

        @Override public int hashCode() {
            return Objects.hashCode(a.hashCode(), b.hashCode());
        }

        @Override public String toString() {
            return ":or(" + a + ", " + b + ")";
        }
    }
}
