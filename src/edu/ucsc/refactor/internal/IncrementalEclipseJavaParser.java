package edu.ucsc.refactor.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.internal.visitors.TypeDeclarationVisitor;
import edu.ucsc.refactor.spi.IncrementalParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Map;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class IncrementalEclipseJavaParser extends EclipseJavaParser implements IncrementalParser {

    private final Set<Integer> SEED = ImmutableSet.of(
            PARSE_COMPILATION_UNIT,
            PARSE_BODY,
            PARSE_STATEMENTS
    );

    /**
     * Creates an incremental parsing object.
     */
    public IncrementalEclipseJavaParser(){
       super();
    }

    @Override  public Map<Class<? extends ASTNode>, ASTNode> offer(Context context) {
        Map<Class<? extends ASTNode>, ASTNode> result =  Maps.newLinkedHashMap();

        for( Integer each : SEED){

            final ASTNode parsed = parseJava(context, each);
            if(parsed != null && !parsed.toString().equals("")){
                switch (parsed.getNodeType()){
                    case ASTNode.BLOCK:
                        result.put(
                                AstUtil.exactCast(Block.class, parsed).getClass(), parsed
                        );
                        break;
                    case ASTNode.TYPE_DECLARATION:
                        result.put(
                                AstUtil.exactCast(TypeDeclaration.class, parsed).getClass(), parsed
                        );
                        break;
                    case ASTNode.COMPILATION_UNIT:
                        result.put(
                                AstUtil.exactCast(CompilationUnit.class, parsed).getClass(), parsed
                        );
                        break;
                }
            }

        }

        return result;
    }
}
