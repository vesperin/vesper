package edu.ucsc.refactor.internal.visitors;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Evaluates all fields, methods and types available (declared) at a given offset
 * in a compilation unit (Code assist that returns IBindings)
 */
public class ScopeAnalyzer {
    public static final int METHODS             = 1; // Specifies that methods should be reported.
    public static final int VARIABLES           = 2; // Specifies that variables should be reported.
    public static final int TYPES               = 4; // Specifies that types should be reported.
    public static final int CHECK_VISIBILITY    = 16;// Specifies that only visible elems should be added.


    private final Set<ITypeBinding> typeBindingsVisited;
    private final CompilationUnit root;

    public ScopeAnalyzer(CompilationUnit root) {
        if(root == null) {
            throw new IllegalArgumentException(
                    "CompilationUnit is null"
            );
        }

        this.typeBindingsVisited    = new HashSet<ITypeBinding>();
        this.root                   = root;
    }

    private void clearLists() {
        typeBindingsVisited.clear();
    }

    private static String getSignature(IBinding binding) {
        if (binding != null) {
            switch (binding.getKind()) {
                case IBinding.METHOD:
                    return signatureGenerator(binding);
                case IBinding.VARIABLE:
                    return 'V' + binding.getName();
                case IBinding.TYPE:
                    return 'T' + binding.getName();
            }
        }

        return null;
    }

    private static String signatureGenerator(IBinding binding) {
        final StringBuilder signature   = new StringBuilder();

        signature.append('M');
        signature.append(binding.getName()).append('(');

        ITypeBinding[] parameters   = ((IMethodBinding) binding).getParameterTypes();

        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                signature.append(',');
            }

            final ITypeBinding paramType  = parameters[i].getErasure();
            signature.append(paramType.getQualifiedName());
        }

        signature.append(')');
        return signature.toString();
    }

    public static boolean hasFlag(int property, int flags) {
        return (flags & property) != 0;
    }

    /**
     * Collects all elements available in a type and its hierarchy
     * @param binding The type binding
     * @param flags Flags defining the elements to report
     * @param requestor the requestor to which all results are reported
     * @return return <code>true</code> if the requestor has reported the binding as found and no further results are required
     */
    private boolean addInheritedElements(ITypeBinding binding, int flags, BindingRequestor requestor) {
        if (!typeBindingsVisited.add(binding)) {
            return false;
        }

        if (hasFlag(VARIABLES, flags)) {
            IVariableBinding[] variableBindings= binding.getDeclaredFields();
            for (IVariableBinding variableBinding : variableBindings) {
                if (requestor.acceptBinding(variableBinding))
                    return true;
            }
        }

        if (hasFlag(METHODS, flags)) {
            IMethodBinding[] methodBindings= binding.getDeclaredMethods();
            for (IMethodBinding curr : methodBindings) {
                if (!curr.isSynthetic() && !curr.isConstructor()) {
                    if (requestor.acceptBinding(curr))
                        return true;
                }
            }
        }

        if (hasFlag(TYPES, flags)) {
            ITypeBinding[] typeBindings= binding.getDeclaredTypes();
            for (ITypeBinding curr : typeBindings) {
                if (requestor.acceptBinding(curr))
                    return true;
            }
        }


        ITypeBinding superClass= binding.getSuperclass();
        if (superClass != null) {
            if (addInheritedElements(superClass, flags, requestor)) {
                return true;
            }
        } else if (binding.isArray()) {
            if (addInheritedElements(root.getAST().resolveWellKnownType("java.lang.Object"),
                    flags, requestor)) {
                return true;
            }
        }

        ITypeBinding[] interfaces = binding.getInterfaces();
        // includes looking for methods:  abstract and then unimplemented methods
        for (ITypeBinding anInterface : interfaces) {
            if (addInheritedElements(anInterface, flags, requestor))  {
                return true;
            }
        }

        return false;
    }


    /**
     * Collects all elements available in a type: its hierarchy and its outer scopes.
     * @param binding The type binding
     * @param flags Flags defining the elements to report
     * @param requestor the requestor to which all results are reported
     * @return return <code>true</code> if the requestor has reported the binding as found and no further results are required
     */
    private boolean addTypeDeclarations(ITypeBinding binding, int flags, BindingRequestor requestor) {
        if (hasFlag(TYPES, flags) && !binding.isAnonymous()) {
            if (requestor.acceptBinding(binding)) {
                return true;
            }

            ITypeBinding[] typeParameters= binding.getTypeParameters();
            for (ITypeBinding typeParameter : typeParameters) {
                if (requestor.acceptBinding(typeParameter)) {
                    return true;
                }
            }
        }

        addInheritedElements(binding, flags, requestor); // add inherited

        if (binding.isLocal()) {
            addOuterDeclarationsForLocalType(binding, flags, requestor);
        } else {
            ITypeBinding declaringClass= binding.getDeclaringClass();
            if (declaringClass != null) {
                if (addTypeDeclarations(declaringClass, flags, requestor))  { // Recursively add inherited
                    return true;
                }
            } else if (hasFlag(TYPES, flags)) {
                if (root.findDeclaringNode(binding) != null) {
                    List types= root.types();
                    for (Object type : types) {
                        if (requestor.acceptBinding(((AbstractTypeDeclaration) type)
                                .resolveBinding())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean addOuterDeclarationsForLocalType(ITypeBinding localBinding, int flags, BindingRequestor requestor) {
        ASTNode node= root.findDeclaringNode(localBinding);
        if (node == null) {
            return false;
        }

        if (node instanceof AbstractTypeDeclaration || node instanceof AnonymousClassDeclaration) {

            if (addLocalDeclarations(node.getParent(), flags, requestor)) {
                return true;
            }

            ITypeBinding parentTypeBinding= getBindingOfParentType(node.getParent());
            if (parentTypeBinding != null) {
                if (addTypeDeclarations(parentTypeBinding, flags, requestor)) {
                    return true;
                }
            }

        }
        return false;
    }

    private static ITypeBinding getBinding(Expression node) {
        if (node != null) {
            return node.resolveTypeBinding();
        }
        return null;
    }

    private static ITypeBinding getQualifier(SimpleName selector) {
        ASTNode parent= selector.getParent();
        switch (parent.getNodeType()) {
            case ASTNode.METHOD_INVOCATION:
                MethodInvocation methodInvocation   = (MethodInvocation) parent;
                if (selector == methodInvocation.getName()) {
                    return getBinding(methodInvocation.getExpression());
                }
                return null;
            case ASTNode.QUALIFIED_NAME:
                QualifiedName qualifiedName= (QualifiedName) parent;
                if (selector == qualifiedName.getName()) {
                    return getBinding(qualifiedName.getQualifier());
                }
                return null;
            case ASTNode.FIELD_ACCESS:
                FieldAccess fieldAccess= (FieldAccess) parent;
                if (selector == fieldAccess.getName()) {
                    return getBinding(fieldAccess.getExpression());
                }
                return null;
            case ASTNode.SUPER_FIELD_ACCESS: {
                ITypeBinding curr= getBindingOfParentType(parent);
                return curr.getSuperclass();
            }
            case ASTNode.SUPER_METHOD_INVOCATION: {
                SuperMethodInvocation superInv= (SuperMethodInvocation) parent;
                if (selector == superInv.getName()) {
                    ITypeBinding curr= getBindingOfParentType(parent);
                    return curr.getSuperclass();
                }
                return null;
            }
            default:
                if (parent instanceof Type) {
                    // bug 67644: in 'a.new X()', all member types of A are visible as location of X.
                    ASTNode normalizedNode= getNormalizedNode(parent);
                    if (normalizedNode.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY) {
                        ClassInstanceCreation creation  = (ClassInstanceCreation) normalizedNode.getParent();
                        return getBinding(creation.getExpression());
                    }
                }
                return null;
        }
    }

    private static class SearchRequestor implements BindingRequestor {

        private final int fFlags;
        private final ITypeBinding fParentTypeBinding;
        private final IBinding fToSearch;
        private boolean fFound;
        private boolean fIsVisible;

        public SearchRequestor(IBinding toSearch, ITypeBinding parentTypeBinding, int flag) {
            fFlags= flag;
            fToSearch= toSearch;
            fParentTypeBinding= parentTypeBinding;
            fFound= false;
            fIsVisible= true;
        }

        public boolean acceptBinding(IBinding binding) {
            if (fFound)
                return true;

            if (binding == null)
                return false;

            if (fToSearch.getKind() != binding.getKind()) {
                return false;
            }

            boolean checkVisibility= hasFlag(CHECK_VISIBILITY, fFlags);
            if (binding == fToSearch) {
                fFound= true;
            } else {
                IBinding bindingDeclaration= getDeclaration(binding);
                if (bindingDeclaration == fToSearch) {
                    fFound= true;
                } else if (bindingDeclaration.getName().equals(fToSearch.getName())) {
                    String signature= getSignature(bindingDeclaration);
                    if (signature != null && signature.equals(getSignature(fToSearch))) {
                        if (checkVisibility) {
                            fIsVisible= false;
                        }
                        return true; // found element that hides the binding to find
                    }
                }
            }

            if (fFound && checkVisibility) {
                fIsVisible= ScopeAnalyzer.isVisible(binding, fParentTypeBinding);
            }
            return fFound;
        }

        public static IBinding getDeclaration(IBinding binding) {
            switch (binding.getKind()) {
                case IBinding.TYPE:
                    return ((ITypeBinding) binding).getTypeDeclaration();
                case IBinding.VARIABLE:
                    return ((IVariableBinding) binding).getVariableDeclaration();
                case IBinding.METHOD:
                    return ((IMethodBinding) binding).getMethodDeclaration();
            }
            return binding;
        }

        public boolean found() {
            return fFound;
        }

        public boolean isVisible() {
            return fIsVisible;
        }
    }

    public boolean isDeclaredInScope(IBinding declaration, SimpleName selector, int flags) {
        try {
            // special case for switch on enum
            if (selector.getLocationInParent() == SwitchCase.EXPRESSION_PROPERTY) {
                ITypeBinding binding= ((SwitchStatement) selector.getParent().getParent()).getExpression().resolveTypeBinding();
                if (binding != null && binding.isEnum()) {
                    return hasEnumConstants(declaration, binding.getTypeDeclaration());
                }
            }

            ITypeBinding parentTypeBinding = getBindingOfParentTypeContext(selector);
            if (parentTypeBinding != null) {
                ITypeBinding binding= getQualifier(selector);
                SearchRequestor requestor= new SearchRequestor(declaration, parentTypeBinding, flags);
                if (binding == null) {
                    addLocalDeclarations(selector, flags, requestor);
                    if (requestor.found()){
                        return requestor.isVisible();
                    }
                    addTypeDeclarations(parentTypeBinding, flags, requestor);
                    if (requestor.found()){
                        return requestor.isVisible();
                    }
                } else {
                    addInheritedElements(binding, flags, requestor);
                    if (requestor.found()){
                        return requestor.isVisible();
                    }
                }
            }
            return false;
        } finally {
            clearLists();
        }
    }

    private boolean addLocalDeclarations(ASTNode node, int flags, BindingRequestor requestor) {
        return addLocalDeclarations(node, node.getStartPosition(), flags, requestor);
    }


    private boolean addLocalDeclarations(ASTNode node, int offset, int flags, BindingRequestor requestor) {
        if (hasFlag(VARIABLES, flags) || hasFlag(TYPES, flags)) {
            BodyDeclaration declaration= findParentBodyDeclaration(node);
            if (declaration instanceof MethodDeclaration || declaration instanceof Initializer) {
                ScopeAnalysisVisitor visitor    = new ScopeAnalysisVisitor(offset, flags, requestor);
                declaration.accept(visitor);
                return visitor.isBreakStatement();
            }
        }
        return false;
    }


    public static BodyDeclaration findParentBodyDeclaration(ASTNode node) {
        while ((node != null) && (!(node instanceof BodyDeclaration))) {
            node= node.getParent();
        }
        return (BodyDeclaration) node;
    }


    private boolean hasEnumConstants(IBinding declaration, ITypeBinding binding) {
        final IVariableBinding[] declaredFields   = binding.getDeclaredFields();
        for (IVariableBinding variableBinding : declaredFields) {
            if (variableBinding == declaration) {
                return true;
            }
        }
        return false;
    }

    private static ITypeBinding getDeclaringType(IBinding binding) {
        switch (binding.getKind()) {
            case IBinding.VARIABLE:
                return ((IVariableBinding) binding).getDeclaringClass();
            case IBinding.METHOD:
                return ((IMethodBinding) binding).getDeclaringClass();
            case IBinding.TYPE:
                ITypeBinding typeBinding= (ITypeBinding) binding;
                if (typeBinding.getDeclaringClass() != null) {
                    return typeBinding;
                }
                return typeBinding;
        }
        return null;
    }


    public IBinding[] getDeclarationsInScope(SimpleName selector, int flags) {
        try {
            // special case for switch on enum
            if (selector.getLocationInParent() == SwitchCase.EXPRESSION_PROPERTY) {
                ITypeBinding binding= ((SwitchStatement) selector.getParent().getParent()).getExpression().resolveTypeBinding();
                if (binding != null && binding.isEnum()) {
                    return getEnumConstants(binding);
                }
            }

            ITypeBinding parentTypeBinding= getBindingOfParentType(selector);
            if (parentTypeBinding != null) {
                ITypeBinding binding= getQualifier(selector);
                DefaultBindingRequestor requestor= new DefaultBindingRequestor(parentTypeBinding, flags);
                if (binding == null) {
                    addLocalDeclarations(selector, flags, requestor);
                    addTypeDeclarations(parentTypeBinding, flags, requestor);
                } else {
                    addInheritedElements(binding, flags, requestor);
                }

                List<IBinding> result= requestor.getResult();
                return result.toArray(new IBinding[result.size()]);
            }
            return null;
        } finally {
            clearLists();
        }
    }



    public IBinding[] getDeclarationsInScope(int offset, int flags) {
        org.eclipse.jdt.core.dom.NodeFinder finder= new org.eclipse.jdt.core.dom.NodeFinder(root,
                offset, 0);
        ASTNode node= finder.getCoveringNode();
        if (node == null) {
            return null;
        }

        if (node instanceof SimpleName) {
            return getDeclarationsInScope((SimpleName) node, flags);
        }

        try {
            ITypeBinding binding= getBindingOfParentType(node);
            DefaultBindingRequestor requestor= new DefaultBindingRequestor(binding, flags);
            addLocalDeclarations(node, offset, flags, requestor);
            if (binding != null) {
                addTypeDeclarations(binding, flags, requestor);
            }
            List<IBinding> result= requestor.getResult();
            return result.toArray(new IBinding[result.size()]);
        } finally {
            clearLists();
        }
    }

    private IVariableBinding[] getEnumConstants(ITypeBinding binding) {
        IVariableBinding[] declaredFields= binding.getDeclaredFields();
        List<IVariableBinding> res= new ArrayList<IVariableBinding>(declaredFields.length);
        for (IVariableBinding curr : declaredFields) {
            if (curr.isEnumConstant()) {
                res.add(curr);
            }
        }
        return res.toArray(new IVariableBinding[res.size()]);
    }


    /**
     * Evaluates if the declaration is visible in a certain context.
     * @param binding The binding of the declaration to examine
     * @param context The context to test in
     * @return {@code true} if the declaration is visible; {@code false} otherwise.
     */
    public static boolean isVisible(IBinding binding, ITypeBinding context) {
        if (binding.getKind() == IBinding.VARIABLE && !((IVariableBinding) binding).isField()) {
            return true; // all local variables found are visible
        }

        ITypeBinding declaring  = getDeclaringType(binding);
        if (declaring == null) {
            return false;
        }

        declaring   = declaring.getTypeDeclaration();

        int modifiers   = binding.getModifiers();
        if (Modifier.isPublic(modifiers) || declaring.isInterface()) { return true; } else if
                (Modifier.isProtected(modifiers) || !Modifier.isPrivate(modifiers)) {

            final boolean sameDeclaringPackage = declaring.getPackage() == context.getPackage();
            final boolean isTypeInScope        = isTypeInScope(
                    declaring,
                    context,
                    Modifier.isProtected(modifiers)
            );

            return sameDeclaringPackage || isTypeInScope;
        }

        // private visibility
        return isTypeInScope(declaring, context, false);
    }

    private static boolean isTypeInScope(ITypeBinding declaring, ITypeBinding context, boolean includeHierarchy) {
        ITypeBinding typeBinding   = context.getTypeDeclaration();
        while (typeBinding != null && typeBinding != declaring) {
            if (includeHierarchy && isInSuperTypeHierarchy(declaring, typeBinding)) {
                return true;
            }

            typeBinding = typeBinding.getDeclaringClass();
        }

        return typeBinding == declaring;
    }

    /*
     * This method is different from Binding.isSuperType as type declarations are compared
     */
    private static boolean isInSuperTypeHierarchy(ITypeBinding possibleSuperTypeDeclaration, ITypeBinding type) {
        if (type == possibleSuperTypeDeclaration) {
            return true;
        }

        ITypeBinding superClass = type.getSuperclass();

        if (superClass != null) {
            if (isInSuperTypeHierarchy(possibleSuperTypeDeclaration, superClass.getTypeDeclaration())) {
                return true;
            }
        }
        if (possibleSuperTypeDeclaration.isInterface()) {
            ITypeBinding[] superInterfaces= type.getInterfaces();
            for (ITypeBinding superInterface : superInterfaces) {

                final boolean isInSuperTypeHierarchy = isInSuperTypeHierarchy(
                        possibleSuperTypeDeclaration,
                        superInterface.getTypeDeclaration()
                );

                if (isInSuperTypeHierarchy) {
                    return true;
                }
            }
        }

        return false;
    }



    public static ASTNode getNormalizedNode(ASTNode node) {
        ASTNode current= node;
        // normalize name
        if (QualifiedName.NAME_PROPERTY.equals(current.getLocationInParent())) {
            current= current.getParent();
        }
        // normalize type
        if (QualifiedType.NAME_PROPERTY.equals(current.getLocationInParent()) ||
                SimpleType.NAME_PROPERTY.equals(current.getLocationInParent())) {
            current= current.getParent();
        }
        // normalize parameterized types
        if (ParameterizedType.TYPE_PROPERTY.equals(current.getLocationInParent())) {
            current= current.getParent();
        }
        return current;
    }


    /**
     * Returns the type binding of the node's type context or null if the node is an annotation, type parameter or super type declaration of a tope level type.
     * The result of this method is equal to the result of {@link #getBindingOfParentType(ASTNode)} for nodes in the type's body.
     * @param node ASTNode object
     * @return the type binding of the node's parent type context
     */
    public static ITypeBinding getBindingOfParentTypeContext(ASTNode node) {
        StructuralPropertyDescriptor lastLocation= null;

        while (node != null) {
            if (node instanceof AbstractTypeDeclaration) {
                final AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) node;
                if (lastLocation == declaration.getBodyDeclarationsProperty()) {
                    return declaration.resolveBinding();
                } else if (declaration instanceof EnumDeclaration && lastLocation == EnumDeclaration.ENUM_CONSTANTS_PROPERTY) {
                    return declaration.resolveBinding();
                }
            } else if (node instanceof AnonymousClassDeclaration) {
                return ((AnonymousClassDeclaration) node).resolveBinding();
            }

            lastLocation    = node.getLocationInParent();
            node            = node.getParent();
        }
        return null;
    }

    /**
     * Returns the type binding of the node's parent type declaration.
     * @param node ASTNode object
     * @return the type binding of the node's parent type declaration
     */
    public static ITypeBinding getBindingOfParentType(ASTNode node) {
        while (node != null) {
            if (node instanceof AbstractTypeDeclaration) {
                return ((AbstractTypeDeclaration) node).resolveBinding();
            } else if (node instanceof AnonymousClassDeclaration) {
                return ((AnonymousClassDeclaration) node).resolveBinding();
            }
            node= node.getParent();
        }
        return null;
    }

    /**
     * A Binding Requestor object
     */
    public static interface BindingRequestor {
        boolean acceptBinding(IBinding binding);
    }


    private static class DefaultBindingRequestor implements BindingRequestor {

        private final List<IBinding> fResult;
        private final HashSet<String> fNamesAdded;
        private final int fFlags;
        private final ITypeBinding fParentTypeBinding;

        public DefaultBindingRequestor(ITypeBinding parentTypeBinding, int flags) {
            fParentTypeBinding= parentTypeBinding;
            fFlags= flags;
            fResult= new ArrayList<IBinding>();
            fNamesAdded= new HashSet<String>();
        }

        /**
         * {@inheritDoc}
         */
        public boolean acceptBinding(IBinding binding) {
            if (binding == null)
                return false;

            String signature= getSignature(binding);
            if (signature != null && fNamesAdded.add(signature)) { // avoid duplicated results from inheritance
                fResult.add(binding);
            }
            return false;
        }

        public List<IBinding> getResult() {
            if (hasFlag(CHECK_VISIBILITY, fFlags)) {
                for (int i= fResult.size() - 1; i >= 0; i--) {
                    IBinding binding= fResult.get(i);
                    if (!isVisible(binding, fParentTypeBinding)) {
                        fResult.remove(i);
                    }
                }
            }
            return fResult;
        }

    }


}

