package org.app.common.machine;

import lombok.Getter;

import java.util.*;

/**
 * A language parser implementation using the State Machine framework.
 * This example shows how to use the state machine for parsing a simple language.
 */
public class LanguageParser {

    /**
     * Token types for the language
     */
    public enum TokenType {
        KEYWORD, IDENTIFIER, NUMBER, STRING, OPERATOR, DELIMITER, WHITESPACE, COMMENT, UNKNOWN
    }

    /**
     * Parser states
     */
    public enum ParserState {
        INITIAL,
        IDENTIFIER,
        NUMBER,
        DECIMAL_NUMBER,
        STRING,
        OPERATOR,
        DELIMITER,
        LINE_COMMENT,
        BLOCK_COMMENT,
        BLOCK_COMMENT_END,
        ERROR
    }

    /**
     * Parser events
     */
    public enum ParserEvent {
        LETTER,
        DIGIT,
        DOT,
        QUOTE,
        OPERATOR_CHAR,
        DELIMITER_CHAR,
        WHITESPACE,
        FORWARD_SLASH,
        ASTERISK,
        NEWLINE,
        OTHER
    }

    /**
     * Token class to hold token information
     */
    @Getter
    public static class Token {
        private final TokenType type;
        private final String value;
        private final int line;
        private final int position;

        public Token(TokenType type, String value, int line, int position) {
            this.type = type;
            this.value = value;
            this.line = line;
            this.position = position;
        }

        @Override
        public String toString() {
            return String.format("%s: '%s' at line %d, pos %d",
                    type, value, line, position);
        }
    }

    /**
     * Parser context to track parsing state and token building
     */
    public static class ParserContext {
        private final String input;
        private int currentPosition;
        private int currentLine;
        private int linePosition;
        @Getter
        private final List<Token> tokens;
        private final StringBuilder currentToken;
        private TokenType currentTokenType;
        private int tokenStartPos;
        private int tokenStartLine;
        private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

        static {
            // Define keywords
            String[] keywordsList = {"if", "else", "while", "for", "return", "function", "var", "const", "let"};
            for (String keyword : keywordsList) {
                KEYWORDS.put(keyword, TokenType.KEYWORD);
            }
        }

        public ParserContext(String input) {
            this.input = input;
            this.currentPosition = 0;
            this.currentLine = 1;
            this.linePosition = 0;
            this.tokens = new ArrayList<>();
            this.currentToken = new StringBuilder();
            this.currentTokenType = TokenType.UNKNOWN;
            this.tokenStartPos = 0;
            this.tokenStartLine = 1;
        }

        public char getCurrentChar() {
            if (currentPosition < input.length()) {
                return input.charAt(currentPosition);
            }
            return '\0';
        }

        public void advancePosition() {
            if (currentPosition < input.length()) {
                if ((int) input.charAt(currentPosition) == (int) '\n') {
                    currentLine++;
                    linePosition = 0;
                } else {
                    linePosition++;
                }
                currentPosition++;
            }
        }

        public boolean hasMoreChars() {
            return currentPosition < input.length();
        }

        public void startToken(TokenType type) {
            currentToken.setLength(0);
            currentTokenType = type;
            tokenStartPos = linePosition;
            tokenStartLine = currentLine;
        }

        public void appendToToken(char c) {
            currentToken.append(c);
        }

        public void finalizeToken() {
            if (currentToken.length() > 0) {
                String tokenValue = currentToken.toString();

                // Check if identifier is actually a keyword
                if (currentTokenType == TokenType.IDENTIFIER && KEYWORDS.containsKey(tokenValue)) {
                    tokens.add(new Token(TokenType.KEYWORD, tokenValue, tokenStartLine, tokenStartPos));
                } else {
                    tokens.add(new Token(currentTokenType, tokenValue, tokenStartLine, tokenStartPos));
                }

                currentToken.setLength(0);
            }
        }

    }

    /**
     * Maps a character to a parser event
     */
    private static ParserEvent charToEvent(char c) {
        if (Character.isLetter(c) || c == '_') {
            return ParserEvent.LETTER;
        } else if (Character.isDigit(c)) {
            return ParserEvent.DIGIT;
        } else if (c == '.') {
            return ParserEvent.DOT;
        } else if (c == '"' || c == '\'') {
            return ParserEvent.QUOTE;
        } else if (c == '+' || c == '-' || c == '*' || c == '=' || c == '<' || c == '>' || c == '!') {
            return ParserEvent.OPERATOR_CHAR;
        } else if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == ';' || c == ',') {
            return ParserEvent.DELIMITER_CHAR;
        } else if (Character.isWhitespace(c)) {
            return c == '\n' ? ParserEvent.NEWLINE : ParserEvent.WHITESPACE;
        } else if (c == '/') {
            return ParserEvent.FORWARD_SLASH;
        } else if (c == '*') {
            return ParserEvent.ASTERISK;
        } else {
            return ParserEvent.OTHER;
        }
    }

    /**
     * Parse the input string into tokens using the state machine
     */
    public static List<Token> parse(String input) {
        StateMachine<ParserState, ParserEvent, ParserContext> parser =
                new StateMachine<>(ParserState.INITIAL);

        ParserContext context = new ParserContext(input);

        // Configure state machine transitions
        configureStateMachine(parser);

        // Process each character
        while (context.hasMoreChars()) {
            char c = context.getCurrentChar();
            ParserEvent event = charToEvent(c);

            // Fire event based on current character
            parser.fireEvent(event, context);

            // Move to next character
            context.advancePosition();
        }

        // Finalize any remaining token
        context.finalizeToken();

        return context.getTokens();
    }

    /**
     * Configure the state machine with all transitions for the language parser
     */
    private static void configureStateMachine(StateMachine<ParserState, ParserEvent, ParserContext> parser) {
        // Initial state transitions
        parser.addTransition(ParserEvent.LETTER, ParserState.INITIAL, ParserState.IDENTIFIER,
                        (from, to, ctx) -> {
                            ctx.startToken(TokenType.IDENTIFIER);
                            ctx.appendToToken(ctx.getCurrentChar());
                        })

                .addTransition(ParserEvent.DIGIT, ParserState.INITIAL, ParserState.NUMBER,
                        (from, to, ctx) -> {
                            ctx.startToken(TokenType.NUMBER);
                            ctx.appendToToken(ctx.getCurrentChar());
                        })

                .addTransition(ParserEvent.QUOTE, ParserState.INITIAL, ParserState.STRING,
                        (from, to, ctx) -> {
                            ctx.startToken(TokenType.STRING);
                            ctx.appendToToken(ctx.getCurrentChar());
                        })

                .addTransition(ParserEvent.OPERATOR_CHAR, ParserState.INITIAL, ParserState.OPERATOR,
                        (from, to, ctx) -> {
                            ctx.startToken(TokenType.OPERATOR);
                            ctx.appendToToken(ctx.getCurrentChar());
                        })

                .addTransition(ParserEvent.DELIMITER_CHAR, ParserState.INITIAL, ParserState.DELIMITER,
                        (from, to, ctx) -> {
                            ctx.startToken(TokenType.DELIMITER);
                            ctx.appendToToken(ctx.getCurrentChar());
                            ctx.finalizeToken();
                        })

                .addTransition(ParserEvent.FORWARD_SLASH, ParserState.INITIAL, ParserState.OPERATOR,
                        (from, to, ctx) -> {
                            ctx.startToken(TokenType.OPERATOR);
                            ctx.appendToToken(ctx.getCurrentChar());
                        })

                .addTransition(ParserEvent.WHITESPACE, ParserState.INITIAL, ParserState.INITIAL);

        // Identifier state transitions
        parser.addTransition(ParserEvent.LETTER, ParserState.IDENTIFIER, ParserState.IDENTIFIER,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()))

                .addTransition(ParserEvent.DIGIT, ParserState.IDENTIFIER, ParserState.IDENTIFIER,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()))

                .addGlobalTransition(ParserEvent.WHITESPACE, ParserState.INITIAL,
                        (from, to, ctx) -> {
                            // Only finalize token if we were building one
                            if (from != ParserState.INITIAL && from != ParserState.LINE_COMMENT
                                    && from != ParserState.BLOCK_COMMENT && from != ParserState.BLOCK_COMMENT_END) {
                                ctx.finalizeToken();
                            }
                        });

        // Number state transitions
        parser.addTransition(ParserEvent.DIGIT, ParserState.NUMBER, ParserState.NUMBER,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()))

                .addTransition(ParserEvent.DOT, ParserState.NUMBER, ParserState.DECIMAL_NUMBER,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()));

        // Decimal number state transitions
        parser.addTransition(ParserEvent.DIGIT, ParserState.DECIMAL_NUMBER, ParserState.DECIMAL_NUMBER,
                (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()));

        // String state transitions
        parser.addTransition(ParserEvent.QUOTE, ParserState.STRING, ParserState.INITIAL,
                (from, to, ctx) -> {
                    ctx.appendToToken(ctx.getCurrentChar());
                    ctx.finalizeToken();
                });

        // Add any character to string except quote
        for (ParserEvent event : ParserEvent.values()) {
            if (event != ParserEvent.QUOTE) {
                parser.addTransition(event, ParserState.STRING, ParserState.STRING,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()));
            }
        }

        // Operator state transitions - most operators are single-character
        parser.addGlobalTransition(ParserEvent.DELIMITER_CHAR, ParserState.INITIAL,
                (from, to, ctx) -> {
                    if (from != ParserState.STRING && from != ParserState.LINE_COMMENT
                            && from != ParserState.BLOCK_COMMENT && from != ParserState.BLOCK_COMMENT_END) {
                        ctx.finalizeToken();
                        ctx.startToken(TokenType.DELIMITER);
                        ctx.appendToToken(ctx.getCurrentChar());
                        ctx.finalizeToken();
                    } else if (from == ParserState.STRING) {
                        ctx.appendToToken(ctx.getCurrentChar());
                    }
                });

        // Forward slash handling (for comments and division operator)
        parser.addTransition(ParserEvent.FORWARD_SLASH, ParserState.OPERATOR, ParserState.LINE_COMMENT,
                        (from, to, ctx) -> {
                            // Change from division operator to line comment
                            ctx.currentToken.setLength(0);
                            ctx.startToken(TokenType.COMMENT);
                            ctx.appendToToken('/');
                            ctx.appendToToken('/');
                        })

                .addTransition(ParserEvent.ASTERISK, ParserState.OPERATOR, ParserState.BLOCK_COMMENT,
                        (from, to, ctx) -> {
                            // Change from division operator to block comment
                            ctx.currentToken.setLength(0);
                            ctx.startToken(TokenType.COMMENT);
                            ctx.appendToToken('/');
                            ctx.appendToToken('*');
                        });

        // Any other character finalizes the operator
        parser.addGlobalTransition(ParserEvent.OPERATOR_CHAR, ParserState.OPERATOR,
                (from, to, ctx) -> {
                    if (from == ParserState.OPERATOR) {
                        ctx.appendToToken(ctx.getCurrentChar());
                        ctx.finalizeToken();
                    } else if (from != ParserState.STRING && from != ParserState.LINE_COMMENT
                            && from != ParserState.BLOCK_COMMENT && from != ParserState.BLOCK_COMMENT_END) {
                        ctx.finalizeToken();
                        ctx.startToken(TokenType.OPERATOR);
                        ctx.appendToToken(ctx.getCurrentChar());
                    } else if (from == ParserState.STRING) {
                        ctx.appendToToken(ctx.getCurrentChar());
                    }
                });

        // Line comment transitions
        parser.addTransition(ParserEvent.NEWLINE, ParserState.LINE_COMMENT, ParserState.INITIAL,
                (from, to, ctx) -> {
                    ctx.finalizeToken();
                });

        // Add any character to line comment except newline
        for (ParserEvent event : ParserEvent.values()) {
            if (event != ParserEvent.NEWLINE) {
                parser.addTransition(event, ParserState.LINE_COMMENT, ParserState.LINE_COMMENT,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()));
            }
        }

        // Block comment transitions
        parser.addTransition(ParserEvent.ASTERISK, ParserState.BLOCK_COMMENT, ParserState.BLOCK_COMMENT_END,
                (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()));

        parser.addTransition(ParserEvent.FORWARD_SLASH, ParserState.BLOCK_COMMENT_END, ParserState.INITIAL,
                (from, to, ctx) -> {
                    ctx.appendToToken(ctx.getCurrentChar());
                    ctx.finalizeToken();
                });

        // Any other character in block comment end returns to block comment
        for (ParserEvent event : ParserEvent.values()) {
            if (event != ParserEvent.FORWARD_SLASH) {
                parser.addTransition(event, ParserState.BLOCK_COMMENT_END, ParserState.BLOCK_COMMENT,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()));
            }
        }

        // Add any character to block comment except asterisk
        for (ParserEvent event : ParserEvent.values()) {
            if (event != ParserEvent.ASTERISK) {
                parser.addTransition(event, ParserState.BLOCK_COMMENT, ParserState.BLOCK_COMMENT,
                        (from, to, ctx) -> ctx.appendToToken(ctx.getCurrentChar()));
            }
        }

        // Add debugging listener
        parser.addListener("debugListener", new StateMachine.StateMachineListener<ParserState, ParserEvent, ParserContext>() {
            @Override
            public void onTransitionDeclined(ParserState state, ParserEvent event, ParserContext context) {
                // If transition is declined, finalize current token and go back to initial state
                // This is a safety mechanism for error recovery
                if (state != ParserState.INITIAL && state != ParserState.ERROR) {
                    context.finalizeToken();
                    parser.reset(ParserState.INITIAL);
                }
            }

            @Override
            public void onError(ParserState state, ParserEvent event, ParserContext context, Exception exception) {
                System.err.println("Error in parser: " + exception.getMessage());
                parser.reset(ParserState.ERROR);
            }
        });
    }

    /**
     * Demo of the parser using some sample code
     */
    public static void main(String[] args) {
        String sampleCode =
                "function calculateSum(a, b) {\n" +
                        "    // This is a line comment\n" +
                        "    let result = a + b;\n" +
                        "    \n" +
                        "    /* This is a block comment\n" +
                        "       spanning multiple lines */\n" +
                        "    \n" +
                        "    if (result > 100) {\n" +
                        "        return \"Large sum: \" + result;\n" +
                        "    } else {\n" +
                        "        return \"Small sum: \" + result;\n" +
                        "    }\n" +
                        "}\n";

        List<Token> tokens = parse(sampleCode);

        System.out.println("Parsed " + tokens.size() + " tokens:");
        for (Token token : tokens) {
            System.out.println(token);
        }

        // Group tokens by type for statistics
        Map<TokenType, Integer> tokenCounts = new EnumMap<>(TokenType.class);
        for (Token token : tokens) {
            tokenCounts.put(token.getType(),
                    tokenCounts.getOrDefault(token.getType(), 0) + 1);
        }

        System.out.println("\nToken statistics:");
        for (Map.Entry<TokenType, Integer> entry : tokenCounts.entrySet()) {
            System.out.printf("%s: %d tokens\n", entry.getKey(), entry.getValue());
        }
    }
}
