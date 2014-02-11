package edu.ucsc.refactor.internal.matchers;


import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.LocalVariableVisitor;
import edu.ucsc.refactor.internal.visitors.SimpleNameVisitor;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class MatchMaker {
    private MatchMaker(){}

    /**
     * Returns a matcher which matches any input.
     */
    public static NodeMatcher any() {
        return ANY;
    }

    private static final NodeMatcher ANY = new AbstractNodeMatcher() {
        @Override public boolean match(ASTNode root, ASTNode element) {
            return true;
        }

        @Override public String toString() {
            return "any()";
        }
    };

    /**
     * Returns a matcher that inverts another matcher.
     */
    public static NodeMatcher not(NodeMatcher that){
        return new Not(that);
    }


    public static NodeMatcher and(Iterable<NodeMatcher> matchers){
        NodeMatcher first = null;
        for(NodeMatcher each : matchers){

            if(first == null){ first = each; } else {
                first = ((AbstractNodeMatcher) first).and(each);
            }
        }

        return first;
    }

    public static NodeMatcher and(NodeMatcher...matchers) {
        return and(Arrays.asList(matchers));
    }


    public static NodeMatcher or(NodeMatcher...matchers) {
        return or(Arrays.asList(matchers));
    }

    public static NodeMatcher or(Iterable<NodeMatcher> matchers){
        NodeMatcher first = null;
        for(NodeMatcher each : matchers){

            if(first == null){ first = each; } else {
                first = ((AbstractNodeMatcher) first).or(each);
            }
        }

        return first;
    }

    public static NodeMatcher isRoot(){
        return new AbstractNodeMatcher() {
            @Override public boolean match(ASTNode root, ASTNode element) {
                return root == element;
            }

            @Override public String toString() {
                return ":root";
            }
        };
    }


    public static NodeMatcher immediateParent(final NodeMatcher matcher){
        return new AbstractNodeMatcher() {
            @Override public boolean match(ASTNode root, ASTNode element) {
                final ASTNode parent = element.getParent();
                return parent != null && matcher.match(null, parent);
            }

            @Override public String toString() {
                return String.format(":ImmediateParent%s", matcher);
            }
        };
    }


    public static NodeMatcher parent(final NodeMatcher matcher){
        return new AbstractNodeMatcher() {
            @Override public boolean match(ASTNode root, ASTNode element) {
                ASTNode parent;

                do {
                    parent = element.getParent();
                    if (matcher.match(null, parent)) return true;
                } while (parent != null);

                return false;

            }

            @Override public String toString() {
                return  String.format(":parent%s", matcher);
            }
        };
    }


    public static NodeMatcher previousSibling(final NodeMatcher matcher){
        return new AbstractNodeMatcher() {
            @Override public boolean match(ASTNode root, ASTNode element) {
                final ASTNode root = element.getParent();
                if(root == null){
                    return false;
                }

                final List<ASTNode> siblings = AstUtil.getChildren(root);
                final int idx = Iterables.indexOf(siblings, Predicates.equalTo(element));

                if(idx == 0) {
                    return false;
                }

                ASTNode prev = siblings.get(idx - 1);

                while(prev != null){

                    if(matcher.match(null, prev)){
                        return true;
                    }

                    prev = siblings.get(idx - 1);
                }

                return false;
            }

            @Override public String toString() {
                return String.format(":prev%s",  matcher);
            }
        };
    }


    public static NodeMatcher immediatePreviousSibling(final NodeMatcher matcher){
        return new AbstractNodeMatcher() {
            @Override public boolean match(ASTNode root, ASTNode element) {
                final ASTNode root = element.getParent();
                if(root == null){
                    return false;
                }

                final List<ASTNode> siblings = AstUtil.getChildren(root);
                final int idx = Iterables.indexOf(siblings, Predicates.equalTo(element));

                if(idx == 0) {
                    return false;
                }

                final ASTNode prev = siblings.get(idx - 1);
                return prev != null && matcher.match(null, prev);
            }

            @Override public String toString() {
                return String.format(":prev%s",  matcher);
            }
        };
    }



    public static NodeMatcher id(final String id){
        return new IdMatcher(id);
    }


    public static NodeMatcher className(final String className){
        return new ClassMatcher(className);
    }


    public static NodeMatcher tag(final String tagName){

        // tagName === AstNode.getNodeType
        return new TagMatcher(tagName);
    }


    public static NodeMatcher attribute(final String attr){
        return new AttributeMatcher(attr);
    }


    public static NodeMatcher attributeStarting(final String keyPrefix){
        return new AttributeStartingMatcher(keyPrefix);
    }


    public static NodeMatcher hasElement(final NodeMatcher matcher){
        return new HasMatcher(matcher);
    }


    public static NodeMatcher containsText(final String searchText){
        return new AbstractNodeMatcher() {
            final String text = searchText.toLowerCase();

            @Override public boolean match(ASTNode root, ASTNode element) {
                return (element.toString().toLowerCase().contains(text));
            }

            @Override public String toString() {
                return String.format(":contains(%s", text);
            }
        };
    }


    public static NodeMatcher matchesText(Pattern pattern){
        return new Matches(pattern);
    }


    /**
     * NodeMatcher for matching Element (and its descendants) text with regex
     */
    public static final class Matches extends AbstractNodeMatcher {
        private Pattern pattern;

        public Matches(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override public boolean match(ASTNode root, ASTNode element) {
            java.util.regex.Matcher m = pattern.matcher(element.toString());
            return m.find();
        }

        @Override public String toString() {
            return String.format(":matches(%s", pattern);
        }
    }

    static class HasMatcher extends AbstractNodeMatcher {
        final NodeMatcher delegate;

        public HasMatcher(NodeMatcher delegate) {
            this.delegate = delegate;
        }

        @Override public boolean match(ASTNode root, ASTNode element) {
            final List<ASTNode> children = AstUtil.getChildren(element);
            for (ASTNode e : children) {
                if (e != element && delegate.match(null, e)) {
                    return true;
                }
            }

            return false;
        }

        @Override public String toString() {
            return String.format(":has(%s)", delegate);
        }
    }


    static final class TagMatcher extends AbstractNodeMatcher {
        private String tagName;
        // todo(Huascar) think if the tag match the method invocation, or
        // local variable, name of the method, etc.
        public TagMatcher(String tagName) {
            super();
            this.tagName = tagName;
        }

        @Override public boolean match(ASTNode root, ASTNode element) {

            final SimpleNameVisitor names = new SimpleNameVisitor();
            element.accept(names);

            final List<SimpleName> ids = names.getSimpleNames();

            for(SimpleName each: ids ){
                if(each.getIdentifier().equals(tagName)){
                    return true;
                }
            }

            return false;
        }

        @Override public String toString() {
            return String.format("%s", tagName);
        }
    }


    /**
     * NodeMatcher for ASTNode id
     */
    private static final class IdMatcher extends AbstractNodeMatcher {
        private String id;
        // todo(Huascar) think if the id match the node type, or name of the method
        public IdMatcher(String id) {
            super();
            this.id = id;
        }

        @Override public boolean match(ASTNode root, ASTNode node) {

            final SimpleNameVisitor names = new SimpleNameVisitor();
            node.accept(names);

            final List<SimpleName> ids = names.getSimpleNames();

            for(SimpleName each: ids ){
                if(id.equals(each.getIdentifier()) && node == each.getParent()){
                    return true;
                }
            }

            return false;
        }

        @Override public String toString() {
            return String.format("#%s", id);
        }

    }


    /**
     * NodeMatcher for element class
     */
    private static final class ClassMatcher extends AbstractNodeMatcher {
        private String className;

        public ClassMatcher(String className) {
            this.className = className;
        }

        @Override public boolean match(ASTNode root, ASTNode element) {
            return (element.getClass().getName().trim().toLowerCase().equals(className));
        }

        @Override public String toString() {
            return String.format(".%s", className);
        }

    }


    /**
     * NodeMatcher for attribute name matching
     */
    private static final class AttributeMatcher extends AbstractNodeMatcher {
        private String key;

        public AttributeMatcher(String key) {
            super();
            this.key = key;
        }

        @Override public boolean match(ASTNode root, ASTNode element) {
            final Source   source   = Source.from(element);
            final Location location = Locations.locate(element);

            final LocalVariableVisitor visitor = new LocalVariableVisitor(source, location);
            element.accept(visitor);

            return visitor.hasAttribute(key);
        }

        @Override public String toString() {
            return String.format("[%s]", key);
        }

    }


    private static final class AttributeStartingMatcher extends AbstractNodeMatcher {
        private String keyPrefix;

        public AttributeStartingMatcher(String keyPrefix) {
            super();
            this.keyPrefix = keyPrefix;
        }

        @Override public boolean match(ASTNode root, ASTNode element) {
            final Source   source   = Source.from(element);
            final Location location = Locations.locate(element);

            final LocalVariableVisitor visitor = new LocalVariableVisitor(source, location);
            element.accept(visitor);

            return visitor.attributeStartsWith(keyPrefix);
        }

        @Override public String toString() {
            return String.format("[^%s]", keyPrefix);
        }

    }


    private static class Not extends AbstractNodeMatcher {
        final NodeMatcher delegate;

        Not(NodeMatcher delegate){
            super();
            this.delegate = delegate;
        }

        @Override public boolean match(ASTNode root, ASTNode element) {
            return !delegate.match(null, element);
        }

        @Override public String toString() {
            return "not(" + delegate + ")";
        }
    }


    public static final class IsOnlyChild extends AbstractNodeMatcher {

        @Override public boolean matches(Element root, Element element) {
            final Element p = element.parent();
            return p!=null && !(p instanceof Document) && element.siblingElements().size() == 0;
        }
        @Override
        public String toString() {
            return ":only-child";
        }
    }

    public static final class IsOnlyOfType extends Evaluator {
        @Override
        public boolean matches(Element root, Element element) {
            final Element p = element.parent();
            if (p==null || p instanceof Document) return false;

            int pos = 0;
            Elements family = p.children();
            for (int i = 0; i < family.size(); i++) {
                if (family.get(i).tag().equals(element.tag())) pos++;
            }
            return pos == 1;
        }
        @Override
        public String toString() {
            return ":only-of-type";
        }
    }

}
