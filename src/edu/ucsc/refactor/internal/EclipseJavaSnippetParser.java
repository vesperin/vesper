package edu.ucsc.refactor.internal;

import com.google.common.collect.ImmutableSet;
import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.ResultPackage;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.JavaSnippetParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class EclipseJavaSnippetParser extends EclipseJavaParser implements JavaSnippetParser {

    private final Set<Integer> SEED = ImmutableSet.of(
            PARSE_COMPILATION_UNIT,
            PARSE_BODY,
            PARSE_STATEMENTS
    );

    /**
     * Creates an incremental parsing object.
     */
    public EclipseJavaSnippetParser(){
       super();
    }

    @Override  public ResultPackage offer(Context context) {
        ResultPackage result =  ResultPackage.empty();

        for( Integer each : SEED){

            final ASTNode parsed = parseJava(context, each);
            if(PARSE_COMPILATION_UNIT == each && isWellConstructedCompilationUnit(parsed)){
                result = ResultPackage.makePackage(parsed, false);
                break;
            } else {
               if(PARSE_BODY == each && isMissingTypeDeclarationUnit(parsed)){
                   result = ResultPackage.makePackage(parsed, true);
                   break;
               } else if(PARSE_STATEMENTS == each){
                   final TypeDeclaration parent = AstUtil.parent(TypeDeclaration.class, parsed);
                   final String          name   = parent.getName().getIdentifier();

                   if("MISSING".equals(name)) {
                       result = ResultPackage.makePackage(parsed, true);
                   }
               }
            }
        }

        return result;
    }


    public static boolean isWellConstructedCompilationUnit(ASTNode parsed){
        if(AstUtil.isOfType(CompilationUnit.class, parsed)){
            final CompilationUnit unit = AstUtil.exactCast(CompilationUnit.class, parsed);
            if(!unit.toString().equals("") && !unit.types().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMissingTypeDeclarationUnit(ASTNode parsed){
        if(AstUtil.isOfType(TypeDeclaration.class, parsed)){
            final TypeDeclaration unit = AstUtil.exactCast(TypeDeclaration.class, parsed);
            if(!unit.toString().equals("") && "MISSING".equals(unit.getName().getIdentifier())) {
                return true;
            }
        }

        return false;
    }
}
