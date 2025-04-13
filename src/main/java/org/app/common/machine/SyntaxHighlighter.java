package org.app.common.machine;

import lombok.Getter;

import java.util.List;

/**
 * A syntax highlighter implementation that extends the LanguageParser.
 * Demonstrates how to use the state machine for text processing and highlighting.
 */
public class SyntaxHighlighter {

    /**
     * Highlight styles for different token types
     */
    @Getter
    public enum HighlightStyle {
        NORMAL("normal"),
        KEYWORD("keyword"),
        IDENTIFIER("identifier"),
        STRING("string"),
        NUMBER("number"),
        COMMENT("comment"),
        OPERATOR("operator"),
        DELIMITER("delimiter");

        private final String cssClass;

        HighlightStyle(String cssClass) {
            this.cssClass = cssClass;
        }

    }

    /**
     * Maps token types to highlight styles
     */
    private static HighlightStyle getStyleForToken(LanguageParser.TokenType tokenType) {
        switch (tokenType) {
            case KEYWORD:
                return HighlightStyle.KEYWORD;
            case IDENTIFIER:
                return HighlightStyle.IDENTIFIER;
            case STRING:
                return HighlightStyle.STRING;
            case NUMBER:
                return HighlightStyle.NUMBER;
            case COMMENT:
                return HighlightStyle.COMMENT;
            case OPERATOR:
                return HighlightStyle.OPERATOR;
            case DELIMITER:
                return HighlightStyle.DELIMITER;
            default:
                return HighlightStyle.NORMAL;
        }
    }

    /**
     * Generate HTML with syntax highlighting for the given code
     */
    public static String highlightCode(String code) {
        // Parse the code into tokens
        List<LanguageParser.Token> tokens = LanguageParser.parse(code);

        // Generate HTML with highlighting
        StringBuilder html = new StringBuilder();
        html.append("<pre class=\"highlighted-code\">\n");

        // Track line and position
        int currentLine = 1;
        int currentPos = 0;

        // Start the first line
        html.append("<span class=\"line-number\">").append(currentLine).append("</span> ");

        for (LanguageParser.Token token : tokens) {
            // Handle line breaks and ensure line numbers
            if (token.getLine() > currentLine) {
                // Close current line and start new one
                html.append("\n");

                // Add all line numbers up to and including the token's line
                for (int i = currentLine + 1; i <= token.getLine(); i++) {
                    html.append("<span class=\"line-number\">").append(i).append("</span> ");
                    if (i < token.getLine()) {
                        html.append("\n");
                    }
                }

                currentLine = token.getLine();
                currentPos = 0;
            }

            // Add spaces to adjust horizontal position if needed
            if (token.getPosition() > currentPos) {
                html.append(" ".repeat(token.getPosition() - currentPos));
            }

            // Add the highlighted token
            HighlightStyle style = getStyleForToken(token.getType());
            html.append("<span class=\"").append(style.getCssClass()).append("\">")
                    .append(escapeHtml(token.getValue()))
                    .append("</span>");

            // Update position
            currentPos = token.getPosition() + token.getValue().length();
        }

        html.append("\n</pre>");
        return html.toString();
    }

    /**
     * Escape HTML special characters
     */
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Generate a complete HTML page with syntax highlighting and CSS
     */
    public static String generateHtmlPage(String code, String title) {

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>" + escapeHtml(title) + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: 'Courier New', monospace; background-color: #f5f5f5; }\n" +
                "        .highlighted-code { background-color: #2d2d2d; color: #d4d4d4; padding: 20px; border-radius: 5px; overflow: auto; }\n" +
                "        .line-number { color: #858585; display: inline-block; min-width: 2em; user-select: none; }\n" +
                "        .keyword { color: #569cd6; font-weight: bold; }\n" +
                "        .identifier { color: #9cdcfe; }\n" +
                "        .string { color: #ce9178; }\n" +
                "        .number { color: #b5cea8; }\n" +
                "        .comment { color: #6a9955; font-style: italic; }\n" +
                "        .operator { color: #d4d4d4; }\n" +
                "        .delimiter { color: #d4d4d4; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>" + escapeHtml(title) + "</h1>\n" +
                "    " + highlightCode(code) + "\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Demo of the syntax highlighter
     */
    public static void main(String[] args) {
        String sampleCode =
                "/**\n" +
                        " * Calculate the factorial of a number\n" +
                        " */\n" +
                        "function factorial(n) {\n" +
                        "    // Base case\n" +
                        "    if (n <= 1) {\n" +
                        "        return 1;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Recursive case\n" +
                        "    return n * factorial(n - 1);\n" +
                        "}\n" +
                        "\n" +
                        "// Test the function\n" +
                        "var result = factorial(5);\n" +
                        "console.log(\"The factorial of 5 is: \" + result);";

        String html = generateHtmlPage(sampleCode, "Factorial Function Example");

        // In a real application, you would write this to a file
        System.out.println(html);
    }
}