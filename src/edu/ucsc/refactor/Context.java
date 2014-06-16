package edu.ucsc.refactor;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import edu.ucsc.refactor.internal.CompilationProblemException;
import edu.ucsc.refactor.util.Locations;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class Context {
    private final Source    file;

    private Location        scope;
    private CompilationUnit compilationUnit;
    private List<String>    syntaxRelatedProblems;



    private static Set<Integer> BLACK_LIST;
    static {
        Set<Integer> blackList = new HashSet<Integer>();
        blackList.add(IProblem.FieldRelated);
        blackList.add(IProblem.MethodRelated);
        blackList.add(IProblem.Internal);
        blackList.add(IProblem.ConstructorRelated);
        blackList.add(IProblem.IllegalPrimitiveOrArrayTypeForEnclosingInstance);
        blackList.add(IProblem.MissingEnclosingInstanceForConstructorCall);
        blackList.add(IProblem.MissingEnclosingInstance);
        blackList.add(IProblem.IncorrectEnclosingInstanceReference);
        blackList.add(IProblem.IllegalEnclosingInstanceSpecification);
        blackList.add(IProblem.CannotDefineStaticInitializerInLocalType);
        blackList.add(IProblem.OuterLocalMustBeFinal);
        blackList.add(IProblem.CannotDefineInterfaceInLocalType);
        BLACK_LIST = Collections.unmodifiableSet(blackList);
    }

    /**
     * Construct a new {@link Context} object.
     *
     * @param file {@link Source} object.
     */
    public Context(Source file){
        this.file                   = file;
        this.syntaxRelatedProblems  = Lists.newArrayList();
    }


    /**
     * Accepts a visitor
     *
     * @param visitor The ASTVisitor
     */
    public void accept(ASTVisitor visitor){
        getCompilationUnit().accept(visitor);
    }


    /**
     * Get the content of the source file.
     *
     * @return The source file's content.
     */
    public String getContents(){
        return getSource().getContents();
    }

    /**
     * Get the current scope (i.e., Location)
     * @return The {@code Location}
     */
    public Location getScope(){
        final Location currentScope = this.scope;
        if(currentScope == null){
            throw new IllegalStateException("Context's scope is null");
        }

        return currentScope;
    }

    /**
     * Gets a list of syntax related problems found during the compilation of
     * the {@code Source}.
     *
     * @return A list of syntax related problems or []
     */
    public List<String> getSyntaxRelatedProblems(){
        return syntaxRelatedProblems;
    }

    /**
     * Checks whether this context is malformed or not.
     *
     * @return {@code true} if the context is malformed; meaning that {@link #getSyntaxRelatedProblems()}
     * is non empty.
     */
    public boolean isMalformedContext(){
        return !getSyntaxRelatedProblems().isEmpty();
    }


    /**
     * Set the CompilationUnit that belongs to the content of the Source's file.
     *
     * @param compilationUnit The compilation unit.
     */
    public void setCompilationUnit(CompilationUnit compilationUnit) {
        if(compilationUnit == null){
            throw new IllegalArgumentException(
                    "setCompilationUnit() was given a null compilation unit."
            );
        }

        this.compilationUnit = compilationUnit;
        this.compilationUnit.setProperty(
                Source.SOURCE_FILE_PROPERTY,
                this.getSource()
        );

        addCompilationErrorIfExist(this.compilationUnit, this.syntaxRelatedProblems);
    }

    /**
     * Get the compilation unit that belongs to the content of the source file's file.
     *
     * @return The source file's compilation unit
     */
    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    /**
     * Locates a ASTNode in the {@code Source}.
     *
     * @param node The ASTNode to be located.
     * @return A {@code Location} in the {@code Source} where a ASTNode is found.
     */
    public Location locate(ASTNode node) {
        return Locations.locate(node);
    }

    /**
     * Gets the context's {@code Source}.
     *
     * @return The context's {@code Source}.
     */
    public Source getSource() {
        return file;
    }

    /**
     * Sets the context scope (if any).
     *
     * @param selection The {@link SourceSelection}
     */
    public void setScope(SourceSelection selection) {
        this.scope = selection.toLocation();
    }

    public static Context throwCompilationErrorIfExist(Context context){
        if(context.isMalformedContext()){
            final CompilationProblemException cpe = new CompilationProblemException();
            for(String problem : context.getSyntaxRelatedProblems()){
                cpe.cache(new Throwable(problem));
            }

            cpe.throwCachedException();
        }

        return context;
    }


    private static void addCompilationErrorIfExist(CompilationUnit unit, List<String> syntaxRelatedProblems) {
        final IProblem[] problems = unit.getProblems();
        if(problems.length > 0){
            for(IProblem each : problems){
                final boolean hasSyntaxProblem  = (each.getID() & IProblem.Syntax) != 0;

                if(each.isError() && (hasSyntaxProblem || inBlackList(each))){
                    final String message = buildMessage(each);
                    syntaxRelatedProblems.add(message);
                }
            }
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


    @Override public String toString() {
        return Objects.toStringHelper(getClass())
                .add("Source", getSource())
                .toString();
    }
}
