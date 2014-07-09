package edu.ucsc.refactor.internal.changers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Location;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveCodeRegion extends SourceChanger {
    private static Set<Integer> STATEMENTS = Sets.newHashSet(
            ASTNode.FOR_STATEMENT, ASTNode.WHILE_STATEMENT,
            ASTNode.DO_STATEMENT, ASTNode.EXPRESSION_STATEMENT,
            ASTNode.IF_STATEMENT, ASTNode.TRY_STATEMENT);


    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.DELETE_REGION);
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {
        final SourceChange change = new SourceChange(cause, this, parameters);

        try {

            Preconditions.checkState(!cause.getAffectedNodes().isEmpty(), "invalid code selection");

            final TypeDeclaration   root      = getTypeDeclaration(cause);
            final ASTRewrite        rewrite   = ASTRewrite.create(root.getAST());

            change.getDeltas().add(removeCodeRegion(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    protected static TypeDeclaration getTypeDeclaration(CauseOfChange cause){
        return AstUtil.parent(TypeDeclaration.class, cause.getAffectedNodes().get(0));
    }


    private Delta removeCodeRegion(TypeDeclaration unit, ASTRewrite rewrite, CauseOfChange cause){
        // check dependencies wrt Root
        final Set<IBinding> S = interCheck(unit, cause);

        // check dependencies wrt to its enclosing method (now its method is its universe)
        intraCheck(unit, cause, S);

        for(ASTNode toBeRemoved : cause.getAffectedNodes()){
            rewrite.remove(toBeRemoved, null);
        }


        return createDelta(unit, rewrite);
    }

    private static Set<IBinding> interCheck(TypeDeclaration unit, CauseOfChange cause) {
        final Set<IBinding> F = generateFieldsUniverse(unit);
        final Set<IBinding> M = generateMethodsUniverse(unit, cause);
        final Set<IBinding> T = generateTypesUniverse(unit);
        final Set<IBinding> S = generateSelection(unit, cause);

        final Set<IBinding> R = Sets.newHashSet();

        for(IBinding s : S){
            if(F.contains(s)){
                R.add(s);
            }

            if(M.contains(s)){
                R.add(s);
            }

            if(T.contains(s)){
                R.add(s);
            }
        }


        if(!R.isEmpty()) throw new IllegalStateException("Code region cannot be deleted; it contains elements outside the selected scope.");

        return S;
    }

    private static void intraCheck(TypeDeclaration unit, CauseOfChange cause, Set<IBinding> s) {
        for( ASTNode eachNode : cause.getAffectedNodes()){
            if(STATEMENTS.contains(eachNode.getNodeType())){
                final MethodDeclaration parent = AstUtil.parent(MethodDeclaration.class, eachNode);


                final Location parentLoc = Locations.locate(parent);
                final Location selectLoc = Locations.locate(eachNode);

                final Set<IBinding> U = AstUtil.getUniqueBindings(parent);
                final Set<IBinding> B = Sets.newHashSet();

                U.retainAll(s);

                for(IBinding eachU : U){
                    final ASTNode node = AstUtil.findDeclaration(eachU, parent);

                    if(node != null){
                        Location nodeLoc;
                        if(node instanceof VariableDeclarationFragment){
                            final IBinding nb = ((VariableDeclarationFragment) node).resolveBinding();
                            final ASTNode  n  = AstUtil.findDeclaration(nb, unit);
                            nodeLoc = Locations.locate(n);
                            if(Locations.liesOutside(nodeLoc, selectLoc) && Locations.inside(parentLoc, nodeLoc)){
                                B.add(nb);
                            }

                        } else if (node instanceof SingleVariableDeclaration){
                            final IBinding nb = ((SingleVariableDeclaration) node).resolveBinding();
                            final ASTNode n = AstUtil.findDeclaration(nb, unit);
                            nodeLoc = Locations.locate(n);
                            if(Locations.liesOutside(nodeLoc, selectLoc) && Locations.inside(parentLoc, nodeLoc)){
                                B.add(nb);
                            }
                        }
                    }

                }

                if(!B.isEmpty()){
                    throw new IllegalStateException("Code region cannot be deleted; it contains elements outside the selected scope.");
                }
            }
        }
    }

    private static Set<IBinding> generateFieldsUniverse(TypeDeclaration unit){
        final ImmutableSet<FieldDeclaration>  F = ImmutableSet.copyOf(unit.getFields());
        final Set<IBinding> RAW = Sets.newHashSet();

        for(FieldDeclaration eachField : F){
            final List<VariableDeclarationFragment> fragments   = eachField.fragments();
            for (VariableDeclarationFragment fragment : fragments) {
                final SimpleName name = fragment.getName();
                final IBinding fb = name.resolveBinding();
                if (!RAW.contains(fb)) {
                    RAW.add(fb);
                }
            }
        }

        return RAW;
    }


    private static Set<IBinding> generateMethodsUniverse(TypeDeclaration unit, CauseOfChange cause){
        final Set<MethodDeclaration> filtered = Sets.newHashSet();
        for(ASTNode eachNode : cause.getAffectedNodes()){
            if(AstUtil.isMethod(eachNode)){
                final MethodDeclaration d = AstUtil.exactCast(MethodDeclaration.class, eachNode);
                filtered.add(d);
            } else {
                final MethodDeclaration parent = AstUtil.parent(MethodDeclaration.class, eachNode);
                if(parent != null){
                    filtered.add(parent);
                }
            }
        }



        final Set<MethodDeclaration>  F = Sets.newHashSet(unit.getMethods());
        final ImmutableSet<MethodDeclaration> DIFF =  Sets.difference(F, filtered).immutableCopy();

        final Set<IBinding> RAW = Sets.newHashSet();

        for(MethodDeclaration declaration : DIFF){
            RAW.add(AstUtil.getDeclaration(declaration.resolveBinding()));
        }


        return RAW;
    }


    private static Set<IBinding> generateTypesUniverse(TypeDeclaration unit){
        ImmutableSet<TypeDeclaration> T = ImmutableSet.copyOf(unit.getTypes());

        final Set<IBinding> RAW = Sets.newHashSet();

        for(TypeDeclaration t : T){
            RAW.addAll(AstUtil.getUniqueBindings(t));
            RAW.add(AstUtil.getDeclaration(t.resolveBinding()));
        }


        return RAW;
    }


    private static Set<IBinding> generateSelection(TypeDeclaration unit, CauseOfChange cause){

        final Set<IBinding> F  = Sets.newHashSet();

        for(ASTNode each : cause.getAffectedNodes()){
            F.addAll(AstUtil.getUniqueBindings(each));
        }


        final Deque<IBinding>   Q = Lists.newLinkedList(F);
        final Set<IBinding>     V = Sets.newHashSet();
        final Set<IBinding>     S = Sets.newHashSet();

        while(!Q.isEmpty()){

            final IBinding s = Q.remove();

            V.add(s);
            S.add(s);


            final ASTNode ss = AstUtil.findDeclaration(s, unit);
            if(ss == null) continue;

            final Set<IBinding> bindings = AstUtil.getUniqueBindings(ss);
            for(IBinding eachBinding: bindings){
                switch (eachBinding.getKind()){
                    case IBinding.VARIABLE:

                        final ASTNode f      = AstUtil.findDeclaration(eachBinding, unit);

                        if(AstUtil.isField(f)){

                            final IBinding   b  = AstUtil.getDeclaration(eachBinding);
                            if(!V.contains(b)){
                                Q.add(b);
                            }
                        } else {
                            if(!V.contains(eachBinding)){
                                Q.add(eachBinding);
                            }
                        }

                        break;

                    default:
                        if(!V.contains(eachBinding)){
                            Q.add(eachBinding);
                        }

                }


            }
        }

        return S;
    }
}
