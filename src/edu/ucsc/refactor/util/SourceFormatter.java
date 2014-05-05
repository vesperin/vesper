package edu.ucsc.refactor.util;

import edu.ucsc.refactor.spi.Formatter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

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
        Map<String, String> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();

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
            return document.get();
        }
    }

    @Override public String format(IDocument document) {
        return format(document.get());
    }
}
