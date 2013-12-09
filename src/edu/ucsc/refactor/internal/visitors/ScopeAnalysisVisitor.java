package edu.ucsc.refactor.internal.visitors;

import edu.ucsc.refactor.internal.SourceVisitor;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ScopeAnalysisVisitor extends SourceVisitor {
    private int     position;
    private int     flags;
    private boolean breakStatement;

    private final ScopeAnalyzer.BindingRequestor requestor;

    public ScopeAnalysisVisitor(int position, int flags, ScopeAnalyzer.BindingRequestor requestor){
        this(position, flags, requestor, false);
    }

    public ScopeAnalysisVisitor(int position, int flags, ScopeAnalyzer.BindingRequestor requestor,
                                boolean visitJavadocTags) {
        super(visitJavadocTags);
        this.position   = position;
        this.flags      = flags;
        this.requestor  = requestor;
        breakStatement  = false;
    }

    public boolean isBreakStatement(){
        return breakStatement;
    }


    private boolean isInside(ASTNode node) {
        int start   = node.getStartPosition();
        int end     = start + node.getLength();

        return start <= position && position < end;
    }

    public boolean visit(MethodDeclaration node) {
        if (isInside(node)) {
            Block body  = node.getBody();
            if (body != null) {
                body.accept(this);
            }
            visitBackwards(node.parameters());
            visitBackwards(node.typeParameters());
        }
        return false;
    }


    public boolean visit(TypeParameter node) {
        if (ScopeAnalyzer.hasFlag(ScopeAnalyzer.TYPES, flags) && node.getStartPosition() < position) {
            breakStatement = requestor.acceptBinding(node.getName().resolveBinding());
        }
        return !breakStatement;
    }

    public boolean visit(SwitchCase node) {
        // switch on enum allows to use enum constants without qualification
        if (ScopeAnalyzer.hasFlag(ScopeAnalyzer.VARIABLES, flags) && !node.isDefault() && isInside(node.getExpression())) {
            SwitchStatement switchStatement= (SwitchStatement) node.getParent();
            ITypeBinding binding= switchStatement.getExpression().resolveTypeBinding();
            if (binding != null && binding.isEnum()) {
                IVariableBinding[] declaredFields= binding.getDeclaredFields();
                for (IVariableBinding variableBinding : declaredFields) {
                    if (variableBinding.isEnumConstant()) {
                        breakStatement = requestor.acceptBinding(variableBinding);
                        if (breakStatement)
                            return false;
                    }
                }
            }
        }

        return false;
    }


    public boolean visit(Initializer node) {
        return !breakStatement && isInside(node);
    }

    public boolean visit(Statement node) {
        return !breakStatement && isInside(node);
    }

    @SuppressWarnings("UnusedParameters")
    public boolean visit(ASTNode node) {
        return false;
    }

    public boolean visit(Block node) {
        if (isInside(node)) {
            visitBackwards(node.statements());
        }
        return false;
    }

    public boolean visit(VariableDeclaration node) {
        if (ScopeAnalyzer.hasFlag(ScopeAnalyzer.VARIABLES, flags) && node.getStartPosition() < position) {
            breakStatement = requestor.acceptBinding(node.resolveBinding());
        }
        return !breakStatement;
    }

    public boolean visit(VariableDeclarationStatement node) {
        visitBackwards(node.fragments());
        return false;
    }

    public boolean visit(VariableDeclarationExpression node) {
        visitBackwards(node.fragments());
        return false;
    }

    public boolean visit(CatchClause node) {
        if (isInside(node)) {
            node.getBody().accept(this);
            node.getException().accept(this);
        }
        return false;
    }

    public boolean visit(ForStatement node) {
        if (isInside(node)) {
            node.getBody().accept(this);
            visitBackwards(node.initializers());
        }
        return false;
    }

    public boolean visit(TypeDeclarationStatement node) {
        if (ScopeAnalyzer.hasFlag(ScopeAnalyzer.TYPES, flags) && node.getStartPosition() + node.getLength() < position) {
            breakStatement = requestor.acceptBinding(node.resolveBinding());
            return false;
        }
        return !breakStatement && isInside(node);
    }

    private void visitBackwards(List list) {
        if (breakStatement) return;

        for (int i= list.size() - 1; i >= 0; i--) {
            ASTNode astNode = (ASTNode) list.get(i);
            if (astNode.getStartPosition() < position) {
                astNode.accept(this);
            }
        }
    }
}
