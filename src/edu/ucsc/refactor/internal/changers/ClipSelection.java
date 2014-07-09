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
        final Set<IBinding> methods     = collectMethodDeclarations(cause);
        final Set<IBinding> slice       = cropCodeSnippet(unit, methods);
        final Set<IBinding> universe    = generateUniverse(unit);

        // Get the nodes not in the slice but are in the Universe. These
        // nodes are the nodes we are interested in removing.

        final Set<IBinding> trash      = difference(universe, slice);

        for(IBinding binding: trash){
            final ASTNode declaringNode = AstUtil.findDeclaration(binding, unit);

            rewrite.remove(declaringNode, null);
        }

        return createDelta(unit, rewrite);
    }

    private Set<IBinding> collectMethodDeclarations(CauseOfChange cause) {
        final Set<IBinding> filtered = Sets.newHashSet();
        for(ASTNode eachNode : cause.getAffectedNodes()){
            if(AstUtil.isMethod(eachNode)){
                final MethodDeclaration d = AstUtil.exactCast(MethodDeclaration.class, eachNode);
                filtered.add(d.resolveBinding());
            } else {
                final MethodDeclaration parent = AstUtil.parent(MethodDeclaration.class, eachNode);
                if(parent != null){
                    filtered.add(parent.resolveBinding());
                }
            }
        }
        return filtered;
    }

    private static Set<IBinding> generateUniverse(TypeDeclaration unit){
        ImmutableSet<TypeDeclaration>   typesSet    = ImmutableSet.copyOf(unit.getTypes());
        ImmutableSet<FieldDeclaration>  fieldsSet   = ImmutableSet.copyOf(unit.getFields());
        ImmutableSet<MethodDeclaration> methodsSet  = ImmutableSet.copyOf(unit.getMethods());
        ImmutableSet<ImportDeclaration> importsSet  = ImmutableSet.copyOf(AstUtil.getUsedImports(((CompilationUnit) unit.getRoot())));


        final Set<IBinding> universeSet = Sets.newHashSet();

        for(TypeDeclaration t : typesSet){
            universeSet.addAll(AstUtil.getUniqueBindings(t));
        }

        for(MethodDeclaration m : methodsSet){
            universeSet.addAll(AstUtil.getUniqueBindings(m));
        }

        for(FieldDeclaration f : fieldsSet){
            universeSet.addAll(AstUtil.getUniqueBindings(f));
        }

        for(ImportDeclaration i : importsSet){
            universeSet.addAll(AstUtil.getUniqueBindings(i));
        }


        final Set<IBinding> U   = Sets.newHashSet();

        for(IBinding each: universeSet){
            switch (each.getKind()){
                case IBinding.METHOD:
                    final ASTNode m = AstUtil.findDeclaration(each, unit);
                    if(AstUtil.isMethod(m)){
                        U.add(each);
                    }

                    break;

                case IBinding.VARIABLE:
                    final ASTNode f      = AstUtil.findDeclaration(each, unit);

                    if(AstUtil.isField(f)){
                        U.add(each);
                    }

                    break;

                case IBinding.TYPE:
                    final ASTNode t  = AstUtil.findDeclaration(each, unit);

                    if(AstUtil.isClass(t)){
                        final TypeDeclaration td = AstUtil.exactCast(
                                TypeDeclaration.class, t);

                        if(td != unit && typesSet.contains(td)){
                            U.add(each);
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

        final Deque<IBinding>   queue   = Lists.newLinkedList(methodBindings);
        final Set<IBinding>     visited = Sets.newHashSet();
        final Set<IBinding>     slice   = Sets.newHashSet();

        while(!queue.isEmpty()){

            final IBinding s = queue.remove();

            visited.add(s);
            slice.add(s);


            final ASTNode node = AstUtil.findDeclaration(s, unit);
            if(node == null) continue;

            final Set<IBinding> bindings = AstUtil.getUniqueBindings(node);
            for(IBinding eachBinding: bindings){
                ASTNode declaringNode;
                switch (eachBinding.getKind()){

                    case IBinding.METHOD:

                        if(!visited.contains(eachBinding)){
                            queue.add(eachBinding);
                        }

                        break;
                    case IBinding.VARIABLE:
                        declaringNode      = AstUtil.findDeclaration(eachBinding, unit);

                        if(AstUtil.isField(declaringNode)){
                            if(!visited.contains(eachBinding)) {
                                queue.add(eachBinding);
                            }
                        }

                        break;

                    case IBinding.TYPE:
                        declaringNode   = AstUtil.findDeclaration(eachBinding, unit);

                        if(AstUtil.isClass(declaringNode)){

                            if(!visited.contains(eachBinding)){
                                queue.add(eachBinding);
                            }
                        }

                        break;

                }
            }


        }

        return slice;
    }



}
