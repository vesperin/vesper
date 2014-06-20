package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.spi.JavaParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class EclipseJavaParser implements JavaParser {
    private static final String             CLASS_NAME  = EclipseJavaParser.class.getName();
    private static final Logger             LOGGER      = Logger.getLogger(CLASS_NAME);
    private static final CompilationUnit    NOTHING     = null;

    private ASTParser astParser;

    /**
     * Constructs a new {@link EclipseJavaParser} object.
     */
    public EclipseJavaParser(){
        astParser   = ASTParser.newParser(AST.JLS4);

        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        astParser.setEnvironment(null, null, null, true);


        final Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
        astParser.setCompilerOptions(options);
    }

    @Override public CompilationUnit parseJava(Context context) {

        astParser.setUnitName(context.getSource().getName());

        LOGGER.fine("Parsing context: " + context);

        final String content = context.getContents();
        if(content == null || content.isEmpty()){
            throw new RuntimeException(
                    "Source file with " +
                            "no content"
            );
        }

        astParser.setSource(content.toCharArray());
        CompilationUnit unit;
        try {
            unit = (CompilationUnit) astParser.createAST(null);
            if(unit == null){
                LOGGER.severe("CompilationUnit is null");
                return NOTHING;
            }

            context.setCompilationUnit(unit);

        } catch (RuntimeException error){
            LOGGER.severe(
                    "Parser Error: "
                            + SourceLocation.createLocation(context.getSource())
                            + (error.getCause() != null
                            ? error.getCause().getLocalizedMessage() + "." : ".")
            );

            throw error;
        }

        return unit;
    }

}
