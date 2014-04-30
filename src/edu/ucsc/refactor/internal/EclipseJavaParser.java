package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.spi.JavaParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Hashtable;
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

        Hashtable<String, String> options = new Hashtable<String, String>();
        options.put(JavaCore.COMPILER_SOURCE, "1.6");
        options.put(JavaCore.COMPILER_COMPLIANCE, "1.6");


        astParser.setCompilerOptions(options);
    }

    @Override public CompilationUnit parseJava(Context context) {

        astParser.setUnitName("vesper");

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

            // if the unit has problems, then fail fast
            throwCompilationErrorIfExist(unit);

            context.setCompilationUnit(unit);

        } catch (Throwable error){
            LOGGER.severe(
                    "Parser Error: "
                            + SourceLocation.createLocation(context.getSource())
                            + (error.getCause() != null
                            ? error.getCause().getLocalizedMessage() + "." : ".")
            );

            throw new RuntimeException(error);
        }

        return unit;
    }

    private static void throwCompilationErrorIfExist(CompilationUnit unit) {
        final IProblem[] problems = unit.getProblems();
        if(problems.length > 0){
            final CompilationProblemException exception = new CompilationProblemException();
            for(IProblem each : problems){
                if(each.isError() && (each.getID() & IProblem.Syntax) != 0){ // catch only syntax related issues
                    exception.cache(new Throwable(each.getMessage()));
                }
            }

            exception.throwCachedException();
        }
    }

}
