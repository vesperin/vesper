package edu.ucsc.refactor.internal;

import edu.ucsc.refactor.Context;
import edu.ucsc.refactor.spi.JavaParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class EclipseJavaParser implements JavaParser {
    private static final String             CLASS_NAME  = EclipseJavaParser.class.getName();
    private static final Logger             LOGGER      = Logger.getLogger(CLASS_NAME);
    private static final CompilationUnit    NOTHING     = null;

    private static Set<Integer> BLACK_LIST;
    static {
        Set<Integer> blackList = new HashSet<Integer>();
        blackList.add(IProblem.FieldRelated);
        blackList.add(IProblem.MethodRelated);
        blackList.add(IProblem.Internal);
        blackList.add(IProblem.ConstructorRelated);
        BLACK_LIST = Collections.unmodifiableSet(blackList);
    }

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

    private static void throwCompilationErrorIfExist(CompilationUnit unit) {
        final IProblem[] problems = unit.getProblems();
        if(problems.length > 0){
            final CompilationProblemException exception = new CompilationProblemException();
            for(IProblem each : problems){
                final boolean hasSyntaxProblem          = (each.getID() & IProblem.Syntax) != 0;

                if(each.isError() && (hasSyntaxProblem || inBlackList(each))){
                    final String message = buildMessage(each);
                    exception.cache(new Throwable(message));
                }
            }

            exception.throwCachedException();
        }
    }

    private static boolean inBlackList(IProblem each){
        for(Integer eachID : BLACK_LIST){
            if((each.getID() & eachID) != 0){
                return true;
            }
        }

        return false;
    }

    private static String buildMessage(IProblem problem) {
        final int start     = problem.getSourceStart();
        final int end       = problem.getSourceEnd();
        final int line      = problem.getSourceLineNumber();
        final String msg    = problem.getMessage();

        return msg + ". Location(line=" + line + ", start=" + start + ", end=" + end + ").";
    }

}
