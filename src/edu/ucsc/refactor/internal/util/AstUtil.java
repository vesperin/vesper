package edu.ucsc.refactor.internal.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.NamedLocation;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.ProgramUnitLocation;
import edu.ucsc.refactor.internal.visitors.*;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class AstUtil {

    private static final int FIELD  = 1;
    private static final int METHOD = 2;
    private static final int TYPE   = 4;
    private static final int LABEL  = 8;
    private static final int NAME   = FIELD | TYPE;


    private AstUtil() {}

    public static boolean isAnnotated(MethodDeclaration methodDeclaration) {
        final List modifiers = methodDeclaration.modifiers();
        return !modifiers.isEmpty() && modifiers.get(0) instanceof Annotation;
    }


    public static boolean usesVariable(MethodDeclaration methodDeclaration,
                                       SingleVariableDeclaration variableDeclaration) {

        final String methodBlock = methodDeclaration.getBody().toString();
        return methodBlock.contains(variableDeclaration.getName().toString());
    }

    /**
     * Find the nearest parent node of a certain type for an {@link org.eclipse.jdt.core.dom.ASTNode}.
     *
     * @param thatClass The type class of the parent node to find. Must be derived from ASTNode.
     * @param node      The node to find a parent node for.
     * @param <T>       The ASTNode derived type of the parent node.
     * @return The found parent, or null if no such parent exists.
     */
    public static <T extends ASTNode> T parent(final Class<T> thatClass, final ASTNode node) {

        ASTNode parent = node;

        if (parent.getClass() == thatClass) {
            // if both classes are the same, then no point on
            // executing the do-while code.
            return exactCast(thatClass, parent);
        }

        do {
            parent = parent.getParent();
            if (parent == null) {
                return null;
            }
        } while (parent.getClass() != thatClass);
        return exactCast(thatClass, parent);
    }

    public static int getAnnotationsSize(IBinding binding) {
        return (binding.getAnnotations() != null
                    ? binding.getAnnotations().length
                    : 0
        );
    }

    /**
     * Note that LineComment and BlockComment nodes are not normally visited in an AST because
     * they are not considered part of main structure of the AST. Use CompilationUnit.getCommentList()
     * to find these additional comments nodes.
     *
     * @param unit  the current compilation unit
     * @param base  the selection range
     * @return the locations covered by selection range
     */
    public static List<NamedLocation> getCoveredCommentsLocation(CompilationUnit unit, Location base){
        final List<NamedLocation>   locations   = Lists.newArrayList();
        final List                  comments    = unit.getCommentList();
        final CommentsVisitor       visitor     = new CommentsVisitor();
        final Source                source      = Source.from(unit);


        for(Object each : comments){
            final Comment comment = (Comment) each;
            comment.accept(visitor);
            Location other;
            for(LineComment line : visitor.getLineComments()){
                other = Locations.locate(source, line);
                if(Locations.covers(base, other) || Locations.bothSame(base, other)){
                    if(!containsLocation(locations, other)){
                        locations.add(new ProgramUnitLocation(line, other));
                    }
                }
            }

            for(BlockComment block : visitor.getBlockComments()){
                other = Locations.locate(source, block);
                if(Locations.covers(base, other) || Locations.bothSame(base, other)){
                    if(!containsLocation(locations, other)){
                        locations.add(new ProgramUnitLocation(block, other));
                    }

                }
            }
        }

        return locations;

    }

    private static boolean containsLocation(List<NamedLocation> locations, Location location){
        for(Location each : locations){
            if(Locations.bothSame(each, location)){
                return true;
            }
        }

        return false;
    }


    public static List<Location> getChildrenLocation(ASTNode parent){
        final List<Location> locations = Lists.newArrayList();
        final List<ASTNode> children = AstUtil.getChildren(parent);
        for(ASTNode eachChild : children){
            locations.add(Locations.locate(eachChild));
            if(eachChild instanceof Block){
                final List<ASTNode> children2 = AstUtil.getChildren(eachChild);
                for(ASTNode o : children2){
                    locations.add(Locations.locate(o));
                }
            }
        }

        return locations;
    }


    public static FieldDeclaration getFieldDeclaration(ASTNode node){
        if(AstUtil.isOfType(SimpleName.class, node)){

            final CompilationUnit unit = AstUtil.parent(CompilationUnit.class, node);
            final ASTNode binding = AstUtil.findDeclaration(((SimpleName)node).resolveBinding(), unit);

            final ASTNode parent = AstUtil.parent(FieldDeclaration.class, binding);
            if(parent != null){
                return AstUtil.exactCast(FieldDeclaration.class, parent);
            }
        }

        return null;
    }


    public static TypeDeclaration getTypeDeclaration(ASTNode node){

        if(AstUtil.isOfType(TypeDeclaration.class, node)){
            return AstUtil.exactCast(TypeDeclaration.class, node);
        }

        final Name name =
                AstUtil.isOfType(SimpleType.class, node) ? AstUtil.exactCast(SimpleType.class, node).getName() : ((AstUtil.isOfType(SimpleName.class, node)
                ?  AstUtil.exactCast(SimpleName.class, node) : null));

        if(name == null) return null;

        final ASTNode binding = AstUtil.findDeclaration(
                name.resolveBinding(),
                AstUtil.parent(CompilationUnit.class, node)
        );

        if(binding != null){
            if(AstUtil.isOfType(MethodDeclaration.class, binding)){
                final MethodDeclaration  mtd = AstUtil.exactCast(MethodDeclaration.class, binding);
                if(mtd.isConstructor()){
                    return AstUtil.exactCast(TypeDeclaration.class, mtd.getParent());
                }
            } else if(AstUtil.isOfType(TypeDeclaration.class, binding)){
                return AstUtil.exactCast(TypeDeclaration.class, binding);
            }
        }

        return null;

    }


    public static MethodDeclaration getMethodDeclaration(ASTNode node){
        final SimpleName name = AstUtil.isOfType(MethodInvocation.class, node)
                ? AstUtil.exactCast(MethodInvocation.class, node).getName()
                : (AstUtil.isOfType(MethodDeclaration.class, node) ? AstUtil.exactCast(MethodDeclaration.class, node).getName()
                : (AstUtil.isOfType(SimpleName.class, node) ? AstUtil.exactCast(SimpleName.class, node) : null));

        if(name == null) return null;

        final ASTNode binding = AstUtil.findDeclaration(name.resolveBinding(),  AstUtil.parent(CompilationUnit.class, node));
        if(binding != null){
            if(AstUtil.isOfType(MethodDeclaration.class, binding)){
                return AstUtil.exactCast(MethodDeclaration.class, binding);
            }
        }

        return null;
    }

    public static <T extends ASTNode> T copySubtree(final Class<T> thatClass, AST ast, final ASTNode node) {
        // similar to //(MethodDeclaration)ASTNode.copySubtree(ast, method);
        return thatClass.cast(ASTNode.copySubtree(ast, node));
    }

    public static ASTRewrite createAstRewrite(AST ast) {
        if (ast == null) throw new NullPointerException("createAstRewrite() was given a null AST");
        //  please remember to avoid creating multiple rewrites, one per affected node...
        //  that will make changes to be out of sync and cause source code overrides; e.g.,
        //  delete method A in Src, rename parameter in Src with method A not deleted.
        return ASTRewrite.create(ast);
    }


    public static void copyParameters(List src, MethodDeclaration dst) {
        for (Object eachObj : src) {
            final SingleVariableDeclaration next  = (SingleVariableDeclaration) eachObj;
            final SingleVariableDeclaration param = AstUtil.copySubtree(
                    SingleVariableDeclaration.class,
                    dst.getAST(),
                    next
            );

            //noinspection unchecked
            dst.parameters().add(param); // unchecked warning
        }
    }


    public static void copyArguments(List src, MethodInvocation dst) {
        for (Object eachObj : src) {
            final SingleVariableDeclaration next  = (SingleVariableDeclaration) eachObj;
            final SingleVariableDeclaration param = AstUtil.copySubtree(
                    SingleVariableDeclaration.class,
                    dst.getAST(),
                    next
            );

            //noinspection unchecked
            dst.arguments().add(param); // unchecked warning
        }
    }



    public static void syncSourceProperty(Source updatedSource, ASTNode node) {
        if (node instanceof CompilationUnit) {
            node.setProperty(Source.SOURCE_FILE_PROPERTY, updatedSource);
        } else {
            // do this after each delta's application
            parent(CompilationUnit.class, node).setProperty(
                    Source.SOURCE_FILE_PROPERTY,
                    updatedSource
            );
        }
    }

    public static <T extends ASTNode> T immediateAncestor(Class<T> targetType, ASTNode object){
        try {
            return exactCast(targetType, object);
        } catch (Throwable ex){
            return null;
        }
    }

    public static <T extends ASTNode> T exactCast(Class<T> targetType, ASTNode object){
        return targetType.cast(object);
    }


    public static Set<ASTNode> getUnusedImports(CompilationUnit unit, Set<String> importNames, Set<String> staticNames){
        @SuppressWarnings("unchecked")
        final List<ImportDeclaration> totalImports = unit.imports();

        final Set<ASTNode> result = Sets.newLinkedHashSet();

        for(ImportDeclaration eachDeclaration : totalImports){

            final Name name              = eachDeclaration.getName();
            final boolean isNotAsterisk  = !eachDeclaration.toString().contains("*;");
            final String target          = AstUtil.getSimpleNameIdentifier(name);


            if(!importNames.contains(target) && (!staticNames.contains(target) || staticNames
                    .isEmpty()) && isNotAsterisk){
                result.add(eachDeclaration);
            }
        }

        return result;

    }


    public static String getSimpleNameIdentifier(Name name) {
        if (name.isQualifiedName()) {
            return exactCast(QualifiedName.class, name).getName().getIdentifier();
        } else {
            return exactCast(SimpleName.class, name).getIdentifier();
        }
    }

    public static boolean isMainMethod(MethodDeclaration methodDeclaration) {
        return (methodDeclaration.getName().toString().equals("main"));
    }

    public static boolean processJavadocComments(CompilationUnit astRoot) {
        return !(astRoot != null && astRoot.getTypeRoot() != null)
                || !"package-info.java".equals(astRoot.getTypeRoot().getElementName());
    }


    public static boolean isNodeWithinSelection(Source src, ASTNode node, Location selection) {

        final Location nodeLocation     = Locations.locate(src, node);

        return (Locations.inside(selection, nodeLocation))
                || (Locations.covers(selection, nodeLocation));
    }

    public static boolean isNodeEnclosingMethod(Source src, ASTNode node, Location selection) {

        final Location nodeLocation = Locations.locate(src, node);

        // Is the method completely enclosed by the node?
        return (Locations.inside(nodeLocation, selection));
    }


    public static boolean isNodeExactlyAtLocation(Source src, ASTNode node, Location selection) {

        final Location nodeLocation = Locations.locate(src, node);

        // Is the method at the same position as the other node?
        return (Locations.bothSame(nodeLocation, selection));
    }


    public static List<ASTNode> getChildren(ASTNode node) {
        final List<ASTNode> result = Lists.newArrayList();

        if(node == null){ return result; }

        List list = node.structuralPropertiesForType();

        for(Object each : list){
            final StructuralPropertyDescriptor descriptor = (StructuralPropertyDescriptor) each;
            final Object child = node.getStructuralProperty(descriptor);

            if (child instanceof List){
                result.addAll(convert((List)child));
            } else if (child instanceof ASTNode){
                if(!(child instanceof Javadoc)){
                    result.add((ASTNode)child);
                }
            }

        }

        return result;
    }


    private static List<ASTNode> convert(List list){
        final List<ASTNode> result = Lists.newArrayList();
        for(Object each : list){
            final ASTNode node = (ASTNode) each;
            if(!(node instanceof Javadoc)){
                result.add(node);
            }
        }

        return result;
    }

    public static List<ASTNode> getSwitchCases(SwitchStatement node) {
        final List<ASTNode> result = Lists.newArrayList();
        for (Object element : node.statements()) {
            if (element instanceof SwitchCase) {
                final ASTNode each = (ASTNode) element;
                final SwitchCase switchCase = exactCast(SwitchCase.class, each);
                result.add(switchCase);

            }
        }
        return result;
    }


    public static boolean isFurtherTraversalNecessary(Source src, ASTNode node, Location selection) {
        return isNodeWithinSelection(src, node, selection)
                || isNodeEnclosingMethod(src, node, selection)
                || isNodeExactlyAtLocation(src, node, selection);
    }


    public static SingleVariableDeclaration getVariableDeclaration(ASTNode simpleName){
        return (SingleVariableDeclaration) AstUtil.getVariableDeclaration(((Name)simpleName));
    }

    public static VariableDeclaration getVariableDeclaration(Name node) {
        final IBinding binding = node.resolveBinding();
        if (binding == null && node.getParent() instanceof VariableDeclaration) {
            return (VariableDeclaration) node.getParent();
        }

        if (binding != null && binding.getKind() == IBinding.VARIABLE) {
            final CompilationUnit cu = parent(CompilationUnit.class, node);
            return findVariableDeclaration(((IVariableBinding) binding), cu);
        }

        return null;
    }

    public static ASTNode findDeclaration(IBinding binding, ASTNode root) {
        root = root.getRoot();
        if (root instanceof CompilationUnit) {
            return exactCast(CompilationUnit.class, root).findDeclaringNode(binding);
        }

        return null;
    }

    public static VariableDeclaration findVariableDeclaration(IVariableBinding binding, ASTNode root) {
        if (binding.isField()) {
            return null;
        }

        final ASTNode result = findDeclaration(binding, root);
        if (result instanceof VariableDeclaration) {
            return (VariableDeclaration) result;
        }

        return null;
    }


    /**
     * Returns <code>true</code> iff <code>parent</code> is a true ancestor of <code>node</code>
     * (i.e. returns <code>false</code> if <code>parent == node</code>).
     *
     * @param node   node to test
     * @param parent assumed parent
     * @return <code>true</code> iff <code>parent</code> is a true ancestor of <code>node</code>
     */
    public static boolean isParent(ASTNode node, ASTNode parent) {

        ASTNode a = Preconditions.checkNotNull(node);

        if(parent == null) return false;

        do {
            a = a.getParent();
            if (a == parent) return true;
        } while (a != null);

        return false;
    }


    public static <T extends ASTNode> boolean isOfType(final Class<T> thatClass, final ASTNode node) {
        return node.getClass() == thatClass;
    }


    /**
     * Checks if a variable, a field, or a parameter has any side effects withing its declaring
     * scope (e.g., A TypeDeclaration for a field, a MethodDeclaration for a parameter or local
     * variable).
     *
     * @param reference The reference to a field, parameter, or a local variable.
     * @return {@code true} if the referenced member has any side effects.
     */
    public static boolean isSideEffectFound(SimpleName reference) {
        ASTNode parent = reference.getParent();

        while (parent instanceof QualifiedName) {
            parent = parent.getParent();
        }

        if (parent instanceof FieldAccess) {
            parent = parent.getParent();
        }

        ASTNode node;

        int nameParentType = parent.getNodeType();
        if (nameParentType == ASTNode.ASSIGNMENT) {
            Assignment assignment = (Assignment) parent;
            node = assignment.getRightHandSide();
        } else if (nameParentType == ASTNode.SINGLE_VARIABLE_DECLARATION) {
            final SingleVariableDeclaration declaration = (SingleVariableDeclaration) parent;
            node = declaration.getInitializer();
            if (node == null) { return false; }
        } else if (nameParentType == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
            node = parent;
        } else {
            return false;
        }

        final SideEffectNodesVisitor visitor = new SideEffectNodesVisitor();
        node.accept(visitor);

        return visitor.getSideEffectNodes().size() > 0;
    }


    public static boolean hasVoidReturn(MethodDeclaration method){
        if(method.getReturnType2().isPrimitiveType() ){
            final PrimitiveType primitiveType = AstUtil.exactCast(
                    PrimitiveType.class,
                    method.getReturnType2()
            );

            return primitiveType.getPrimitiveTypeCode() == PrimitiveType.VOID;
        }

        return false;
    }


    /**
     * Get all the AST nodes connected to a given binding. e.g. Declaration of a field and all
     * references. For types, this includes also the constructor declaration. For methods also
     * overridden methods or methods overriding (if existing in the same AST)
     *
     * @param root The root of the AST tree to search; e.g., Type declaration, Method declaration..
     * @param binding The binding of the searched nodes.
     * @return The list of nodes linked to a binding; an empty list if there are none.
     */
    public static List<SimpleName> findByBinding(ASTNode root, IBinding binding) {
        final LinkedNodesVisitor linkedBindings = new LinkedNodesVisitor(binding);
        root.accept(linkedBindings);

        return linkedBindings.getLinkedNodes();
    }


    /**
     * Get all nodes connected to the given name node. If the node has a binding then all nodes connected
     * to this binding are returned. If the node has no binding, then all nodes that also miss a binding
     * and have the same name are returned.
     *
     * @param root The root of the AST tree to search
     * @param name The node to find linked nodes for
     * @return The list of all nodes that have the same name or are connected to
     *      name's binding (if binding is available)
     */
    public static List<SimpleName> findByNode(ASTNode root, SimpleName name) {
        final IBinding binding = name.resolveBinding();

        if (binding != null) {
            return findByBinding(root, binding);
        }

        final List<SimpleName> names = findByProblems(root, name);

        if (names != null) {
            return names;
        }

        int parentKind = name.getParent().getNodeType();
        if (parentKind == ASTNode.LABELED_STATEMENT
                || parentKind == ASTNode.BREAK_STATEMENT
                || parentKind == ASTNode.CONTINUE_STATEMENT) {

            final LabelVisitor labelVisitor = new LabelVisitor(name);

            root.accept(labelVisitor);

            return labelVisitor.getLabels();
        }

        return ImmutableList.of(name);
    }


    public static List<SimpleName> findByProblems(ASTNode parent, SimpleName nameNode) {
        final List<SimpleName> result = new ArrayList<SimpleName>();

        final ASTNode astRoot = parent.getRoot();

        if (!(astRoot instanceof CompilationUnit)) {
            return ImmutableList.of();
        }

        final IProblem[] problems = exactCast(CompilationUnit.class, astRoot).getProblems();

        int nameNodeKind = getNameNodeProblemKind(problems, nameNode);
        if (nameNodeKind == 0) { // no problem on node
            return ImmutableList.of();
        }

        int bodyStart   = parent.getStartPosition();
        int bodyEnd     = bodyStart + parent.getLength();

        String name = nameNode.getIdentifier();

        for (IProblem each : problems) {
            int probStart   = each.getSourceStart();
            int probEnd     = each.getSourceEnd() + 1;

            if (probStart > bodyStart && probEnd < bodyEnd) {
                int currKind = getProblemKind(each);
                if ((nameNodeKind & currKind) != 0) {
                    ASTNode node = NodeFinder.perform(parent, probStart, (probEnd - probStart));
                    if (node instanceof SimpleName
                            && name.equals(exactCast(SimpleName.class, node).getIdentifier())) {
                        result.add((SimpleName) node);
                    }
                }
            }
        }

        return result;
    }


    private static int getProblemKind(IProblem problem) {
        switch (problem.getID()) {
            case IProblem.UndefinedField:
                return FIELD;
            case IProblem.UndefinedMethod:
                return METHOD;
            case IProblem.UndefinedLabel:
                return LABEL;
            case IProblem.UndefinedName:
            case IProblem.UnresolvedVariable:
                return NAME;
            case IProblem.UndefinedType:
                return TYPE;
        }
        return 0;
    }


    private static int getNameNodeProblemKind(IProblem[] problems, SimpleName nameNode) {
        final int nameOffset  = nameNode.getStartPosition();
        final int nameInclEnd = nameOffset + nameNode.getLength() - 1;

        for (IProblem each : problems) {
            if (each.getSourceStart() == nameOffset && each.getSourceEnd() == nameInclEnd) {
                int kind = getProblemKind(each);
                if (kind != 0) {
                    return kind;
                }
            }
        }

        return 0;
    }
}
