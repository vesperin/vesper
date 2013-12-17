package edu.ucsc.refactor.internal.changers;

import com.google.common.base.Joiner;
import edu.ucsc.refactor.CauseOfChange;
import edu.ucsc.refactor.Change;
import edu.ucsc.refactor.Parameter;
import edu.ucsc.refactor.Source;
import edu.ucsc.refactor.internal.SourceChange;
import edu.ucsc.refactor.internal.visitors.FieldDeclarationVisitor;
import edu.ucsc.refactor.spi.Smell;
import edu.ucsc.refactor.spi.SourceChanger;
import edu.ucsc.refactor.util.AstUtil;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for replacing a magic number with symbolic constant.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class RemoveMagicNumber  extends SourceChanger {
    private static final String PARAMETER_CONSTANT_NAME = "Constant name";

    @Override public boolean canHandle(CauseOfChange cause) {
        return cause.getName().isSame(Smell.MAGIC_NUMBER);
    }

    @Override protected Change initChanger(CauseOfChange cause,
                                           Map<String, Parameter> parameters) {

        final ChangeBuilder changeBuilder = new ChangeBuilder(cause, this, parameters);
        return changeBuilder.build();
    }

    @Override protected Map<String, Parameter> defaultParameters() {
        final Map<String, Parameter> parameters = new HashMap<String, Parameter>();
        final Parameter constantNameParameter   = new Parameter(
                PARAMETER_CONSTANT_NAME,
                "CONSTANT_" + HumanNumber.formatRandomNumber()
        );

        constantNameParameter.getConstraints().add(
                new Parameter.Constraint(){
                    @Override public boolean isValid(Object value) {
                        return ((String) value).matches("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");
                    }
                }
        );

        parameters.put(PARAMETER_CONSTANT_NAME, constantNameParameter);

        return parameters;
    }

    /**
     * Turns numerical numbers into English-spoken strings, and then connects them
     * using {@code _}.
     */
    static class HumanNumber {
        static final String[] UNITS = {"zero", "one", "two", "three", "four", "five", "six",
                "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen",
                "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};

        static final String[] TENS = {"zero", "ten", "twenty", "thirty", "forty", "fifty",
                "sixty", "seventy", "eighty", "ninety"};

        static final String[] ORDERS = {"thousand", "million", "billion", "trillion",
                "quadrillion", "quintillion", "sextillion", "septillion", "octillion",
                "nonillion", "decillion", "undecillion", "duodecillion", "tredecillion",
                "quattuordecillion", "quindecillion", "sexdecillion", "septendecillion",
                "octodecillion", "novemdecillion", "vigintillion"};

        private HumanNumber(){}

        static String formatRandomNumber(){
            final int max = 100;
            final int min = 1;
            int r = min + (int) (Math.random() * (max-min));   // between 1 and 99
            final String result = format(r);
            final String[] split = result.split(" ");
            return Joiner.on("_").join(split).toUpperCase();
        }

        static String format(int input) {
            return format(String.valueOf(input));
        }

        static String format(String input) {
            if ((input.length() + 2) / 3 - 1 > ORDERS.length) {
                throw new IllegalArgumentException("Number too big.");
            }

            final StringBuilder result = new StringBuilder();
            int i = input.length();
            int order = -1;

            while (i >= 3) {
                int a = charToInt(input.charAt(i - 3));
                int b = charToInt(input.charAt(i - 2));
                int c = charToInt(input.charAt(i - 1));

                final String number = format(a, b, c);

                if (order >= 0 && !"".equals(number)) {
                    result.insert(0, " " + ORDERS[order]);
                }

                result.insert(0, number);

                if (order == -1 && i > 3 && a == 0 && (b != 0 || c != 0)) {
                    result.insert(0, " and ");
                } else if (i > 3 && (a != 0 || b != 0 || c != 0)) {
                    result.insert(0, ", ");
                }

                order++;
                i = i - 3;

            }

            if(i > 0){
                if (order >= 0) {
                    result.insert(0, " " + ORDERS[order]);
                }

                if (i == 2) {
                    result.insert(0, format(0, charToInt(input.charAt(0)), charToInt(input.charAt(1))));
                } else if (i == 1) {
                    result.insert(0, format(0, 0, charToInt(input.charAt(0))));
                }
            }

            return result.toString();

        }


        private static String format(int a, int b, int c) {
            String result = "";
            if (b == 1) {
                result = UNITS[10 + c];
            } else {
                if (c != 0) {
                    result = UNITS[c];
                }
                if (b >= 2) {
                    if (c != 0) {
                        result = " " + result;
                    }
                    result = TENS[b] + result;
                }
            }

            if (a != 0) {
                if (b != 0 || c != 0) {
                    result = " and " + result;
                }
                result = UNITS[a] + " hundred" + result;
            }
            return result;
        }


        private static int charToInt(char ch) {
            return ch - '0';
        }



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

            String name  = (String) parameters.get(PARAMETER_CONSTANT_NAME).getValue();
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
