package edu.ucsc.refactor.internal.changers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.internal.Delta;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.Refactoring;
import edu.ucsc.refactor.spi.SourceChanger;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ClipSelection extends SourceChanger {
    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Refactoring.CLIP_SELECTION);
    }

    @Override protected Change initChanger(CauseOfChange cause, Map<String, Parameter> parameters) {
        final SourceChange change = new SourceChange(cause, this, parameters);

        try {

            Preconditions.checkState(!cause.getAffectedNodes().isEmpty(), "invalid code selection");

            final TypeDeclaration   root    = getTypeDeclaration(cause);
            final ASTRewrite        rewrite = ASTRewrite.create(root.getAST());


            change.getDeltas().add(cropCodeRegionNotInClippedRegion(root, rewrite, cause));

        } catch (Throwable ex){
            change.getErrors().add(ex.getMessage());
        }

        return change;
    }

    protected static TypeDeclaration getTypeDeclaration(CauseOfChange cause){
        return AstUtil.parent(TypeDeclaration.class, cause.getAffectedNodes().get(0));
    }


    private Delta cropCodeRegionNotInClippedRegion(TypeDeclaration unit, ASTRewrite rewrite, CauseOfChange cause){
        final Set<IBinding> filtered = Sets.newHashSet();
        for(ASTNode eachNode : cause.getAffectedNodes()){
            if(AstUtil.isMethod(eachNode)){
                final MethodDeclaration d = AstUtil.exactCast(MethodDeclaration.class, eachNode);
                filtered.add(AstUtil.getDeclaration(d.resolveBinding()));
            } else {
                final MethodDeclaration parent = AstUtil.parent(MethodDeclaration.class, eachNode);
                if(parent != null){
                    filtered.add(AstUtil.getDeclaration(parent.resolveBinding()));
                }
            }
        }


        final Set<IBinding> SLICE       = cropCodeSnippet(unit, filtered);
        final Set<IBinding> UNIVERSE    = generateUniverse(unit);

        // do the whole things about set difference and get the nodes not in the slice. these
        // nodes are the ones to be removed.

        final Set<IBinding> REMOVE      = difference(UNIVERSE, SLICE);

        for(IBinding bindingToRemove: REMOVE){
            final ASTNode declaringNode = AstUtil.findDeclaration(bindingToRemove, unit);

            rewrite.remove(declaringNode, null);
        }

        return createDelta(unit, rewrite);
    }

    private static Set<IBinding> generateUniverse(TypeDeclaration unit){
        ImmutableSet<TypeDeclaration>   T = ImmutableSet.copyOf(unit.getTypes());
        ImmutableSet<FieldDeclaration>  F = ImmutableSet.copyOf(unit.getFields());
        ImmutableSet<MethodDeclaration> M = ImmutableSet.copyOf(unit.getMethods());
        ImmutableSet<ImportDeclaration> I = ImmutableSet.copyOf(AstUtil.getUsedImports(((CompilationUnit) unit.getRoot())));


        final Set<IBinding> RAW = Sets.newHashSet();

        for(TypeDeclaration t : T){
            RAW.addAll(AstUtil.getUniqueBindings(t));
        }

        for(MethodDeclaration m : M){
            RAW.addAll(AstUtil.getUniqueBindings(m));
        }

        for(FieldDeclaration f : F){
            RAW.addAll(AstUtil.getUniqueBindings(f));
        }

        for(ImportDeclaration i : I){
            RAW.addAll(AstUtil.getUniqueBindings(i));
        }


        final Set<IBinding> U   = Sets.newHashSet();

        for(IBinding each: RAW){
            switch (each.getKind()){
                case IBinding.METHOD:
                    final ASTNode m = AstUtil.findDeclaration(each, unit);
                    if(AstUtil.isMethod(m)){
                        final MethodDeclaration name       = AstUtil.exactCast(MethodDeclaration.class, m);
                        U.add(AstUtil.getDeclaration(name.resolveBinding()));
                    }

                    break;

                case IBinding.VARIABLE:
                    final ASTNode f      = AstUtil.findDeclaration(each, unit);

                    if(AstUtil.isField(f)){
                        final SimpleName name           = AstUtil.getSimpleName(f);

                        U.add(AstUtil.getDeclaration(name.resolveBinding()));
                    }

                    break;

                case IBinding.TYPE:
                    final ASTNode t  = AstUtil.findDeclaration(each, unit);

                    if(AstUtil.isClass(t)){
                        final TypeDeclaration td = AstUtil.exactCast(
                                TypeDeclaration.class, t);

                        if(td != unit && T.contains(td)){
                            U.add(td.resolveBinding());
                        }

                    }

                    break;
            }
        }

        return U;
    }

    private static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.removeAll(setB);
        return tmp;
    }

    private static Set<IBinding> cropCodeSnippet(TypeDeclaration unit, Set<IBinding> methodBindings){

        final Deque<IBinding>   Q = Lists.newLinkedList(methodBindings);
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
                ASTNode declaringNode;
                switch (eachBinding.getKind()){

                    case IBinding.METHOD:

                        if(!V.contains(eachBinding)){
                            Q.add(eachBinding);
                        }

                        break;
                    case IBinding.VARIABLE:
                        declaringNode      = AstUtil.findDeclaration(eachBinding, unit);

                        if(AstUtil.isField(declaringNode)){
                            final SimpleName name           = AstUtil.getSimpleName(declaringNode);
                            final IBinding   fieldBinding   = AstUtil.getDeclaration(name.resolveBinding());
                            if(!V.contains(fieldBinding)) {
                                Q.add(fieldBinding);
                            }
                        }

                        break;

                    case IBinding.TYPE:
                        declaringNode   = AstUtil.findDeclaration(eachBinding, unit);

                        if(AstUtil.isClass(declaringNode)){
                            final TypeDeclaration td = AstUtil.exactCast(
                                    TypeDeclaration.class, declaringNode);

                            final ITypeBinding tb = td.resolveBinding();

                            if(!V.contains(tb)){
                                Q.add(tb);
                            }
                        }

                        break;

                }
            }


        }

        return S;
    }



}
