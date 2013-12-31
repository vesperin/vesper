package edu.ucsc.refactor.internal.changers;

import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.visitors.FieldDeclarationVisitor;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.AstUtil;
import edu.ucsc.refactor.util.Parameters;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.Map;

/**
 * Responsible for replacing a magic number with symbolic constant.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveMagicNumber  extends SourceChanger {

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.MAGIC_NUMBER);
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final ChangeBuilder changeBuilder = new ChangeBuilder(cause, this, parameters);
        return changeBuilder.build();
    }

    @Override protected Map<String, Parameter> defaultParameters() {
        return Parameters.newRandomConstantName();
    }

    static class ChangeBuilder {
        private final CauseOfChange cause;
        private final SourceChanger changer;
        private final Map<String, Parameter> parameters;

        ChangeBuilder(CauseOfChange cause,
                      SourceChanger changer, Map<String, Parameter> parameters){

            this.cause = cause;
            this.changer = changer;
            this.parameters = parameters;
        }

        Change build(){

            final NumberLiteral   literal      = (NumberLiteral) cause.getAffectedNodes().get(0);
            final TypeDeclaration literalClass = AstUtil.parent(TypeDeclaration.class, literal);

            final ASTRewrite      rewrite      = ASTRewrite.create(literalClass.getAST());

            String name  = (String) parameters.get(Parameters.PARAMETER_CONSTANT_NAME).getValue();
            String value = literal.getToken();

            if (!existingConstantExists(literalClass, name)) {
                createConstant(literalClass, rewrite, name, value);
            }

            replaceMagicNumberWithConstant(literal, rewrite, name);

            return buildSolution(literalClass);
        }


        static void createConstant(final TypeDeclaration literalClass, ASTRewrite rewrite,
                                   final String name,
                                   final String value) {
            AST ast = literalClass.getAST();

            final ListRewrite listRewrite = rewrite.getListRewrite(
                    literalClass,
                    TypeDeclaration.BODY_DECLARATIONS_PROPERTY
            );

            final VariableDeclarationFragment variable = ast.newVariableDeclarationFragment();

            variable.setName(ast.newSimpleName(name));
            variable.setInitializer(ast.newNumberLiteral(value));

            final FieldDeclaration field = ast.newFieldDeclaration(variable);
            field.setType(ast.newPrimitiveType(PrimitiveType.INT));

            //noinspection unchecked
            field.modifiers().addAll( // unchecked warning
                    ast.newModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL)
            );

            listRewrite.insertFirst(field, null);
        }


        static void replaceMagicNumberWithConstant(final NumberLiteral literal,
                                                   final ASTRewrite rewrite,
                                                   final String name) {
            SimpleName constantReference = literal.getAST().newSimpleName(name);
            rewrite.replace(literal, constantReference, null);
        }


        SourceChange buildSolution(TypeDeclaration literalClass){

            final Source code = Source.from(literalClass);
            final SourceChange change = new SourceChange(cause, changer, parameters);
            change.getDeltas().add(change.createDelta(code));
            return change;
        }

        private static boolean existingConstantExists(final TypeDeclaration literalClass, final
                                                      String name) {
            final FieldDeclarationVisitor visitor = new FieldDeclarationVisitor();
            literalClass.accept(visitor);
            return visitor.hasFieldName(name);
        }
    }
}
