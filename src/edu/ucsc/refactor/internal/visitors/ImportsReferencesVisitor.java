package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.internal.SourceVisitor;
import edu.ucsc.refactor.internal.util.AstUtil;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ImportsReferencesVisitor extends SourceVisitor {
    private final AtomicReference<CompilationUnit>  root;
    private final Set<String>                       typeNameImports;
    private final Set<SimpleName>                   typeImports;
    private final Set<String>                       staticImports;
    private final boolean                           skipMethodBodies;
    private final ISourceRange                      subRange;

    /**
     * Constructs a new {@link ImportsReferencesVisitor} object.
     * @param visitJavadocTags {@code true} if this visitor must
     *            visit the JavaDocs tags.
     */
    public ImportsReferencesVisitor(boolean visitJavadocTags){
        this(visitJavadocTags, null, false);
    }

    /**
     * Constructs a new {@link ImportsReferencesVisitor} object.
     * @param visitJavadocTags {@code true} if this visitor must visit the JavaDocs tags.
     * @param rangeLimit The {@link ISourceRange} object.
     * @param skipMethodBodies {@code true} if method bodies should be skipped.
     */
    public ImportsReferencesVisitor(boolean visitJavadocTags, ISourceRange rangeLimit,
                                    boolean skipMethodBodies) {
        super(visitJavadocTags);

        this.typeNameImports    = new HashSet<String>();
        this.typeImports        = new HashSet<SimpleName>();
        this.staticImports      = new HashSet<String>();
        this.subRange           = rangeLimit;
        this.root               = new AtomicReference<CompilationUnit>();
        this.skipMethodBodies   = skipMethodBodies;
    }


    public Set<String> getImportNames(){
        for(SimpleName each: getImportSimpleNames()){
            typeNameImports.add(AstUtil.getSimpleNameIdentifier(each));
        }

        return typeNameImports;
    }


    public Set<SimpleName> getImportSimpleNames(){
        return typeImports;
    }

    public Set<String> getStaticImportNames(){
        return staticImports;
    }


    private boolean isAffected(ASTNode node) {
        if (subRange == null) {
            return true;
        }
        int nodeStart   = node.getStartPosition();
        int offset      = subRange.getOffset();
        return nodeStart + node.getLength() > offset && offset + subRange.getLength() >  nodeStart;
    }


    private void addReference(SimpleName name) {
        if (isAffected(name)) {
            typeImports.add(name);
        }
    }

    private void typeRefFound(Name node) {
        if (node != null) {
            while (node.isQualifiedName()) {
                node= ((QualifiedName) node).getQualifier();
            }
            addReference((SimpleName) node);
        }
    }


    private void possibleTypeRefFound(Name node) {
        while (node.isQualifiedName()) {
            node= ((QualifiedName) node).getQualifier();
        }
        IBinding binding= node.resolveBinding();
        if (binding == null || binding.getKind() == IBinding.TYPE) {
            // if the binding is null, we cannot determine if
            // we have a type binding or not, so we will assume
            // we do.
            addReference((SimpleName) node);
        }
    }


    private void possibleStaticImportFound(Name name) {
        if (staticImports == null || root == null) {
            return;
        }

        while (name.isQualifiedName()) {
            name= ((QualifiedName) name).getQualifier();
        }
        if (!isAffected(name)) {
            return;
        }

        IBinding binding= name.resolveBinding();
        if (binding == null || binding instanceof ITypeBinding || !Modifier.isStatic(binding.getModifiers()) || ((SimpleName) name).isDeclaration()) {
            return;
        }

        if (binding instanceof IVariableBinding) {
            IVariableBinding varBinding= (IVariableBinding) binding;
            if (varBinding.isField()) {
                varBinding= varBinding.getVariableDeclaration();
                ITypeBinding declaringClass= varBinding.getDeclaringClass();
                if (declaringClass != null && !declaringClass.isLocal()) {

                    final ScopeAnalyzer scope = new ScopeAnalyzer(root.get());

                    if (scope.isDeclaredInScope(varBinding, (SimpleName)name,
                            ScopeAnalyzer.VARIABLES | ScopeAnalyzer.CHECK_VISIBILITY))
                        return;
                    staticImports.add(AstUtil.getSimpleNameIdentifier(name));
                }
            }
        } else if (binding instanceof IMethodBinding) {
            IMethodBinding methodBinding= ((IMethodBinding) binding).getMethodDeclaration();
            ITypeBinding declaringClass= methodBinding.getDeclaringClass();
            if (declaringClass != null && !declaringClass.isLocal()) {
                final ScopeAnalyzer scope = new ScopeAnalyzer(root.get());
                if (scope.isDeclaredInScope(methodBinding, (SimpleName)name,
                        ScopeAnalyzer.METHODS | ScopeAnalyzer.CHECK_VISIBILITY))
                    return;
                staticImports.add(AstUtil.getSimpleNameIdentifier(name));
            }
        }

    }


    private void doVisitChildren(List elements) {
        for (Object element : elements) {
            ((ASTNode) element).accept(this);
        }
    }

    private void doVisitNode(ASTNode node) {
        if (node != null) {
            node.accept(this);
        }
    }

    @Override
    public boolean visit(CompilationUnit node) {
        if(node.getTypeRoot() == null){
            if(this.root.compareAndSet(this.root.get(), node)){
                return true;
            }
        }
        return super.visit(node);
    }

    protected boolean visitNode(ASTNode node) {
        return isAffected(node);
    }

    @Override public boolean visit(ArrayType node) {
        doVisitNode(node.getElementType());
        return false;
    }

    @Override public boolean visit(SimpleType node) {
        typeRefFound(node.getName());
        return false;
    }

    @Override public boolean visit(QualifiedType node) {
        // nothing to do here, let the qualifier be visited
        return true;
    }

    @Override public boolean visit(QualifiedName node) {
        possibleTypeRefFound(node); // possible ref
        possibleStaticImportFound(node);
        return false;
    }

    @Override public boolean visit(ImportDeclaration node) {
        return false;
    }

    @Override public boolean visit(PackageDeclaration node) {
        doVisitNode(node.getJavadoc());
        doVisitChildren(node.annotations());
        return false;
    }


    @Override public boolean visit(ThisExpression node) {
        typeRefFound(node.getQualifier());
        return false;
    }

    private void evaluateQualifyingExpression(Expression expr, Name selector) {
        if (expr != null) {
            if (expr instanceof Name) {
                Name name= (Name) expr;
                possibleTypeRefFound(name);
                possibleStaticImportFound(name);
            } else {
                expr.accept(this);
            }
        } else if (selector != null) {
            possibleStaticImportFound(selector);
        }
    }

    @Override public boolean visit(ClassInstanceCreation node) {
        doVisitChildren(node.typeArguments());
        doVisitNode(node.getType());
        evaluateQualifyingExpression(node.getExpression(), null);
        if (node.getAnonymousClassDeclaration() != null) {
            node.getAnonymousClassDeclaration().accept(this);
        }
        doVisitChildren(node.arguments());
        return false;
    }

    @Override public boolean visit(MethodInvocation node) {
        evaluateQualifyingExpression(node.getExpression(), node.getName());
        doVisitChildren(node.typeArguments());
        doVisitChildren(node.arguments());
        return false;
    }


    @Override public boolean visit(SuperConstructorInvocation node) {
        if (!isAffected(node)) {
            return false;
        }

        evaluateQualifyingExpression(node.getExpression(), null);
        doVisitChildren(node.typeArguments());
        doVisitChildren(node.arguments());
        return false;
    }

    @Override public boolean visit(FieldAccess node) {
        evaluateQualifyingExpression(node.getExpression(), node.getName());
        return false;
    }

    @Override public boolean visit(SimpleName node) {
        // if the call gets here, it can only be a variable reference
        possibleStaticImportFound(node);
        return false;
    }


    @Override public boolean visit(MarkerAnnotation node) {
        typeRefFound(node.getTypeName());
        return false;
    }


    @Override public boolean visit(NormalAnnotation node) {
        typeRefFound(node.getTypeName());
        doVisitChildren(node.values());
        return false;
    }


    @Override public boolean visit(SingleMemberAnnotation node) {
        typeRefFound(node.getTypeName());
        doVisitNode(node.getValue());
        return false;
    }

    @Override public boolean visit(TypeDeclaration node) {
        return isAffected(node);
    }


    @Override public boolean visit(MethodDeclaration node) {
        if (!isAffected(node)) {
            return false;
        }

        doVisitNode(node.getJavadoc());

        doVisitChildren(node.modifiers());
        doVisitChildren(node.typeParameters());

        if (!node.isConstructor()) {
            doVisitNode(node.getReturnType2());
        }

        doVisitChildren(node.parameters());

        for (Object eachName : node.thrownExceptions()) {
            typeRefFound((Name) eachName);
        }

        if (!skipMethodBodies) {
            doVisitNode(node.getBody());
        }

        return false;
    }

    @SuppressWarnings("unchecked") @Override public boolean visit(TagElement node) {
        final String tagName    = node.getTagName();
        List<Object> list       = node.fragments(); // unchecked warning

        int idx= 0;

        if (tagName != null && !list.isEmpty()) {
            Object first= list.get(0);
            if (first instanceof Name) {
                if ("@throws".equals(tagName) || "@exception".equals(tagName)) {
                    typeRefFound((Name) first);
                } else if ("@see".equals(tagName) || "@link".equals(tagName) || "@linkplain".equals(tagName)) {
                    final Name name   = (Name) first;

                    possibleTypeRefFound(name);
                }

                idx++;
            }
        }

        for (int i  = idx; i < list.size(); i++) {
            doVisitNode((ASTNode) list.get(i));
        }

        return false;
    }

    @Override public boolean visit(MemberRef node) {
        Name qualifier= node.getQualifier();
        if (qualifier != null) {
            typeRefFound(qualifier);
        }
        return false;
    }

    @Override public boolean visit(MethodRef node) {
        Name qualifier= node.getQualifier();
        if (qualifier != null) {
            typeRefFound(qualifier);
        }
        List list= node.parameters();
        if (list != null) {
            doVisitChildren(list); // visit MethodRefParameter with Type
        }
        return false;
    }
}
