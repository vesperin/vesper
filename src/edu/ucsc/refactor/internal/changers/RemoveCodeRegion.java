package edu.ucsc.refactor.internal.changers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

import java.util.*;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveCodeRegion extends SourceChanger {
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

        final Set<IBinding>         allClassBindings    = AstUtil.getUniqueBindings(unit);
        final Queue<Set<IBinding>>  selectionBindings   = getSelectionBindings(cause);

        allClassBindings.retainAll(selectionBindings.remove());

        if(!allClassBindings.isEmpty()){ // there is a chance that ONE String type binding will show up
            throw new RuntimeException(
                    "code region cannot be deleted. Some of its elements are used throughout the source code!"
            );
        }


        for(ASTNode toBeRemoved : cause.getAffectedNodes()){
            rewrite.remove(toBeRemoved, null);
        }


        return createDelta(unit, rewrite);
    }

    private static Queue<Set<IBinding>> getSelectionBindings(CauseOfChange cause) {
        final Map<ASTNode, Set<IBinding>> targets = Maps.newHashMap();
        for(ASTNode eachAffected : cause.getAffectedNodes()){
            final Set<IBinding> children = AstUtil.getUniqueBindings(eachAffected);
            if(!targets.containsKey(eachAffected)){
                targets.put(eachAffected, children.isEmpty() ? Sets.<IBinding>newHashSet() : children);
            }
        }

        final Queue<Set<IBinding>>  selectionBindings = Lists.newLinkedList();
        for(ASTNode e : cause.getAffectedNodes()){

            if(selectionBindings.isEmpty()) {
                selectionBindings.add(targets.get(e));
            } else {
                final Set<IBinding> m1 = selectionBindings.remove();
                final Set<IBinding> m2 = targets.get(e);
                m1.addAll(m2);

                selectionBindings.add(m1);
            }
        }
        return selectionBindings;
    }
}
