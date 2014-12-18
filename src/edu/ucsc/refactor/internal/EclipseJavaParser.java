package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.internal.util.AstUtil;
import edu.ucsc.refactor.spi.JavaParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class EclipseJavaParser implements JavaParser {
    private static final String             CLASS_NAME          = EclipseJavaParser.class.getName();
    private static final Logger             LOGGER              = Logger.getLogger(CLASS_NAME);
    private static final CompilationUnit    NOTHING             = null;

    public static final int                PARSE_COMPILATION_UNIT    = ASTParser.K_COMPILATION_UNIT;
    public static final int                PARSE_STATEMENTS          = ASTParser.K_STATEMENTS;
    public static final int                PARSE_BODY                = ASTParser.K_CLASS_BODY_DECLARATIONS;

    private ASTParser astParser;

    /**
     * Constructs a new {@link EclipseJavaParser} object.
     */
    public EclipseJavaParser(){
        astParser   = ASTParser.newParser(AST.JLS4);

        astParser.setResolveBindings(true);
        astParser.setEnvironment(null, null, null, true);


        final Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
        astParser.setCompilerOptions(options);
    }

    @Override public CompilationUnit parseJava(Context context) {
        final CompilationUnit unit = AstUtil.exactCast(CompilationUnit.class, parseJava(context,
                PARSE_COMPILATION_UNIT));
        context.setCompilationUnit(unit);
        return unit;
    }

    @Override public ASTNode parseJava(Context context, int mode) {
        astParser.setKind(mode);

        astParser.setStatementsRecovery(true);
        astParser.setBindingsRecovery(true);

        astParser.setUnitName(context.getSource().getName());

        LOGGER.fine("Parsing context: " + context);

        final String content = context.getContents();
        ensureContextHasContent(content);

        astParser.setSource(content.toCharArray());
        ASTNode unit;
        try {
            unit = astParser.createAST(null);
            if(unit == null){
                LOGGER.severe("CompilationUnit is null");
                return NOTHING;
            }

            return unit;
        } catch (RuntimeException error){
            throw logAndThrowRuntimeException(context, error);
        }
    }

    private static void ensureContextHasContent(String content){
        if(content == null || content.isEmpty()){
            throw new RuntimeException(
                    "Source file with " +
                            "no content"
            );
        }
    }


    private static RuntimeException logAndThrowRuntimeException(Context context, RuntimeException error){
        LOGGER.severe(
                "Parser Error: "
                        + SourceLocation.createLocation(context.getSource())
                        + (error.getCause() != null
                        ? error.getCause().getLocalizedMessage() + "." : ".")
        );

        return error;
    }
}
