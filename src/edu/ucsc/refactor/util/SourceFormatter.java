package edu.ucsc.refactor.util;

import edu.ucsc.refactor.spi.Formatter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class SourceFormatter implements Formatter {
    private static final Logger LOGGER  = Logger.getLogger(SourceFormatter.class.getName());

    /**
     * Constructs a new {@link SourceFormatter code formatter}.
     */
    public SourceFormatter(){}

    /**
     * Format java source code
     * @param code Code as string
     * @return formatted code as string
     */
    @SuppressWarnings("unchecked")
    @Override public String format(String code) {
        LOGGER.fine("Started formatting code.");

        String lineSeparator        = System.getProperty("line.separator");
        // unchecked warning
        Map<String, String> options = new HashMap<String, String>();
        options.put("org.eclipse.jdt.core.formatter.comment.insert_new_line_before_root_tags", "insert");
        options.put("org.eclipse.jdt.core.formatter.disabling_tag", "@formatter:off");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_annotation", "");

        options.put("org.eclipse.jdt.core.formatter.comment.insert_new_line_before_root_tags","insert");
        options.put("org.eclipse.jdt.core.formatter.disabling_tag","@formatter:off");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_annotation","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_type_parameters","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_type_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_type_arguments","insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_anonymous_type_declaration","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_case","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_brace_in_array_initializer","do not insert");
        options.put("org.eclipse.jdt.core.formatter.comment.new_lines_at_block_boundaries","true");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_cascading_method_invocation_with_arguments.count_dependent","16|-1|16");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_annotation_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_annotation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_before_field","0");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_while","do not insert");
        options.put("org.eclipse.jdt.core.formatter.use_on_off_tags","false");
        options.put("org.eclipse.jdt.core.formatter.wrap_prefer_two_fragments","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_annotation_type_member_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_before_else_in_if_statement","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_prefix_operator","do not insert");
        options.put("org.eclipse.jdt.core.formatter.keep_else_statement_on_same_line","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_ellipsis","insert");
        options.put("org.eclipse.jdt.core.formatter.comment.insert_new_line_for_parameter","do not insert");
        options.put("org.eclipse.jdt.core.formatter.wrap_comment_inline_tags","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_annotation_type_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.indent_breaks_compare_to_cases","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_at_in_annotation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_local_variable_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_multiple_fields","16");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_parameter","1040");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_expressions_in_array_initializer","16");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_type.count_dependent","1585|-1|1585");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_conditional_expression","80");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_for","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_multiple_fields.count_dependent","16|-1|16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_binary_operator","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_question_in_wildcard","do not insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_array_initializer","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_enum_constant","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_before_finally_in_try_statement","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_local_variable","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_before_catch_in_try_statement","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_while","insert");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_after_package","1");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_qualified_allocation_expression.count_dependent","16|4|80");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_method_declaration.count_dependent","16|4|48");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_type_parameters","insert");
        options.put("org.eclipse.jdt.core.formatter.continuation_indentation","2");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_enum_declaration.count_dependent","16|4|49");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_postfix_operator","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_method_invocation","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_arguments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_superinterfaces","do not insert");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_before_new_chunk","1");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_binary_operator","insert");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_before_package","0");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_cascading_method_invocation_with_arguments","16");
        options.put("org.eclipse.jdt.core.compiler.source","1.7");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_constructor_declaration.count_dependent","16|4|48");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_constant_arguments","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_constructor_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.comment.format_line_comments","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_arguments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_enum_declarations","insert");
        options.put("org.eclipse.jdt.core.formatter.join_wrapped_lines","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_block","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_explicit_constructor_call","16");
        options.put("org.eclipse.jdt.core.formatter.wrap_non_simple_local_variable_annotation","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_invocation_arguments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.align_type_members_on_columns","false");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_before_member_type","0");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_enum_constant","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_enum_constants.count_dependent","16|5|48");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_for","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_method_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_selector_in_method_invocation","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_switch","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_unary_operator","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_case","insert");
        options.put("org.eclipse.jdt.core.formatter.comment.indent_parameter_description","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_method_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_switch","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_enum_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_parameters","do not insert");
        options.put("org.eclipse.jdt.core.formatter.comment.clear_blank_lines_in_block_comment","false");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_type_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.lineSplit","100");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_if","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_selector_in_method_invocation.count_dependent","16|4|48");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_brackets_in_array_type_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_parenthesized_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_explicitconstructorcall_arguments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_constructor_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_before_first_class_body_declaration","0");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_method","insert");
        options.put("org.eclipse.jdt.core.formatter.indentation.size","4");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_method_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.enabling_tag","@formatter:on");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_enum_constant","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_package","1585");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_superclass_in_type_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_assignment","16");
        options.put("org.eclipse.jdt.core.compiler.problem.assertIdentifier","error");
        options.put("org.eclipse.jdt.core.formatter.tabulation.char","space");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_semicolon_in_try_resources","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_constructor_declaration_parameters","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_prefix_operator","do not insert");
        options.put("org.eclipse.jdt.core.formatter.indent_statements_compare_to_body","true");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_before_method","1");
        options.put("org.eclipse.jdt.core.formatter.wrap_outer_expressions_when_nested","true");
        options.put("org.eclipse.jdt.core.formatter.wrap_non_simple_type_annotation","true");
        options.put("org.eclipse.jdt.core.formatter.format_guardian_clause_on_one_line","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_for","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_field_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_cast","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_parameters_in_constructor_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_labeled_statement","insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_annotation_type_declaration","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_method_body","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_method_declaration","0");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_try","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_method_invocation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_bracket_in_array_allocation_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_enum_constant","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_annotation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_at_in_annotation_type_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_declaration_throws","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_if","do not insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_switch","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_declaration_throws","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_parenthesized_expression_in_return","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_annotation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_question_in_conditional","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_question_in_wildcard","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_try","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_bracket_in_array_allocation_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.comment.preserve_white_space_between_code_and_line_comments","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_parenthesized_expression_in_throw","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_type_arguments","do not insert");
        options.put("org.eclipse.jdt.core.compiler.problem.enumIdentifier","error");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_generic_type_arguments","16");
        options.put("org.eclipse.jdt.core.formatter.indent_switchstatements_compare_to_switch","true");
        options.put("org.eclipse.jdt.core.formatter.comment_new_line_at_start_of_html_paragraph","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_ellipsis","do not insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_block","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comment_prefix","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_for_inits","do not insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_method_declaration","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.compact_else_if","true");
        options.put("org.eclipse.jdt.core.formatter.wrap_non_simple_parameter_annotation","false");
        options.put("org.eclipse.jdt.core.formatter.wrap_before_or_operator_multicatch","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_array_initializer","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_for_increments","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_method","1585");
        options.put("org.eclipse.jdt.core.formatter.format_line_comment_starting_on_first_column","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_bracket_in_array_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_field","insert");
        options.put("org.eclipse.jdt.core.formatter.comment.indent_root_tags","true");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_enum_constant","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_enum_declarations","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_union_type_in_multicatch","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_explicitconstructorcall_arguments","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_switch","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_method_declaration_parameters","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_superinterfaces","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_allocation_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.tabulation.size","2");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_type_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_opening_brace_in_array_initializer","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_brace_in_block","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_enum_constant","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_arguments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_constructor_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_constructor_declaration_throws","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_if","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_method_invocation.count_dependent","16|5|80");
        options.put("org.eclipse.jdt.core.formatter.comment.clear_blank_lines_in_javadoc_comment","false");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_parameter.count_dependent","1040|-1|1040");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_constructor_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_assignment_operator","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_assignment_operator","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_package.count_dependent","1585|-1|1585");
        options.put("org.eclipse.jdt.core.formatter.indent_empty_lines","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_synchronized","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_paren_in_cast","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_declaration_parameters","insert");
        options.put("org.eclipse.jdt.core.formatter.force_if_else_statement_brace","true");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_block_in_case","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.number_of_empty_lines_to_preserve","3");
        options.put("org.eclipse.jdt.core.formatter.wrap_non_simple_package_annotation","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_method_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_catch","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_constructor_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_method_invocation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_bracket_in_array_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_qualified_allocation_expression","16");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_annotation.count_dependent","16|-1|16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_and_in_type_parameter","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_type","1585");
        options.put("org.eclipse.jdt.core.compiler.compliance","1.7");
        options.put("org.eclipse.jdt.core.formatter.continuation_indentation_for_array_initializer","2");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_brackets_in_array_allocation_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_at_in_annotation_type_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_allocation_expression","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_cast","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_unary_operator","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_new_anonymous_class","20");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_parameterized_type_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_local_variable.count_dependent","1585|-1|1585");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_anonymous_type_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.keep_empty_array_initializer_on_one_line","false");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_enum_declaration","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_field.count_dependent","1585|-1|1585");
        options.put("org.eclipse.jdt.core.formatter.keep_imple_if_on_one_line","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_constructor_declaration_parameters","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_closing_angle_bracket_in_type_parameters","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_labeled_statement","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_at_end_of_file_if_missing","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_for","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_parameterized_type_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_parameters_in_constructor_declaration.count_dependent","16|5|80");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_type_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_enum_declaration","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_binary_expression","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_while","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_type","insert");
        options.put("org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode","enabled");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_try","insert");
        options.put("org.eclipse.jdt.core.formatter.put_empty_statement_on_new_line","false");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_label","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_parameter","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_type_parameters","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_method_invocation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.comment.format_javadoc_comments","true");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_enum_constant","16");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_before_while_in_do_statement","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_enum_constant.count_dependent","16|-1|16");
        options.put("org.eclipse.jdt.core.formatter.comment.line_length","100");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_after_annotation_on_package","insert");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_between_import_groups","1");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_enum_constant_arguments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_semicolon","do not insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_constructor_declaration","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.number_of_blank_lines_at_beginning_of_method_body","0");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_conditional","insert");
        options.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_type_header","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_annotation_type_member_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.wrap_before_binary_operator","true");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_between_type_declarations","2");
        options.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_enum_declaration_header","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_synchronized","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_enum_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.indent_statements_compare_to_block","true");
        options.put("org.eclipse.jdt.core.formatter.join_lines_in_comments","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_question_in_conditional","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_multiple_field_declarations","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_compact_if","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_for_inits","insert");
        options.put("org.eclipse.jdt.core.formatter.indent_switchstatements_compare_to_cases","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_array_initializer","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_default","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_and_in_type_parameter","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_parens_in_constructor_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_before_imports","0");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_assert","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_field","1585");
        options.put("org.eclipse.jdt.core.formatter.comment.format_html","true");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_throws_clause_in_method_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_angle_bracket_in_type_parameters","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_bracket_in_array_allocation_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_anonymous_type_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_colon_in_conditional","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_angle_bracket_in_parameterized_type_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_for","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_expressions_in_array_initializer.count_dependent","16|5|80");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_postfix_operator","do not insert");
        options.put("org.eclipse.jdt.core.formatter.comment.format_source_code","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_synchronized","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_allocation_expression","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_constructor_declaration_throws","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_parameters_in_method_declaration","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_brace_in_array_initializer","do not insert");
        options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform","1.7");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_resources_in_try","80");
        options.put("org.eclipse.jdt.core.formatter.use_tabs_only_for_leading_indentations","false");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_annotation","16");
        options.put("org.eclipse.jdt.core.formatter.comment.format_header","true");
        options.put("org.eclipse.jdt.core.formatter.comment.format_block_comments","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_enum_constant","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_enum_constants","0");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_parenthesized_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_annotation_declaration_header","true");
        options.put("org.eclipse.jdt.core.formatter.insert_new_line_in_empty_block","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_parenthesized_expression","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_catch","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_multiple_local_declarations","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_superinterfaces_in_type_declaration.count_dependent","16|4|48");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_switch","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_comma_in_for_increments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_method.count_dependent","1585|-1|1585");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_method_invocation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_colon_in_assert","insert");
        options.put("org.eclipse.jdt.core.formatter.brace_position_for_type_declaration","end_of_line");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_array_initializer","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_between_empty_braces_in_array_initializer","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_binary_expression.count_dependent","16|-1|16");
        options.put("org.eclipse.jdt.core.formatter.wrap_non_simple_member_annotation","true");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_annotations_on_local_variable","1585");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_opening_paren_in_method_declaration","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_semicolon_in_for","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_explicit_constructor_call.count_dependent","16|5|80");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_paren_in_catch","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_parameterized_type_reference","do not insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_multiple_field_declarations","insert");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_closing_paren_in_annotation","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_generic_type_arguments.count_dependent","16|-1|16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_parameterized_type_reference","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_arguments_in_allocation_expression.count_dependent","16|5|80");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_method_invocation_arguments","insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_parameters_in_method_declaration.count_dependent","16|5|80");
        options.put("org.eclipse.jdt.core.formatter.comment.new_lines_at_javadoc_boundaries","true");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_after_imports","1");
        options.put("org.eclipse.jdt.core.formatter.blank_lines_between_import_groups","0");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_comma_in_multiple_local_declarations","insert");
        options.put("org.eclipse.jdt.core.formatter.indent_body_declarations_compare_to_enum_constant_header","true");
        options.put("org.eclipse.jdt.core.formatter.insert_space_after_semicolon_in_for","insert");
        options.put("org.eclipse.jdt.core.formatter.never_indent_line_comments_on_first_column","false");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_semicolon_in_try_resources","do not insert");
        options.put("org.eclipse.jdt.core.formatter.alignment_for_for_statement","16");
        options.put("org.eclipse.jdt.core.formatter.insert_space_before_opening_angle_bracket_in_type_arguments","do not insert");
        options.put("org.eclipse.jdt.core.formatter.never_indent_block_comments_on_first_column","false");
        options.put("org.eclipse.jdt.core.formatter.keep_then_statement_on_same_line", "false");

        // initialize the compiler settings to be able to format 1.6 code
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);

        int type   = CodeFormatter.K_COMPILATION_UNIT;

        int indent = 0;
        final CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);

        final TextEdit edit               = codeFormatter.format(
                type,
                code,
                0,
                code.length(),
                indent,
                lineSeparator
        );

        if (edit == null) { return code; } else {
            final IDocument document = new Document(code);
            try { edit.apply(document); } catch (Exception e) {
                return code;
            }

            LOGGER.fine("Code has been formatted.");
            return StringUtil.trimEnd(document.get(), '\n');
        }
    }

    @Override public String format(IDocument document) {
        return format(document.get());
    }
}
