package grammar;

import simplegrammar.ParseException;
import simplegrammar.PatternTokenizer;
import simplegrammar.Token;

import java.util.HashSet;
import java.util.Set;

public class PLSQLTokenizer extends PatternTokenizer {

    private final static Set<String> LOGICAL_OPERATORS;

    static {
        LOGICAL_OPERATORS = new HashSet<>();
        LOGICAL_OPERATORS.add("AND");
        LOGICAL_OPERATORS.add("OR");
        LOGICAL_OPERATORS.add("NOT");
    }

    public PLSQLTokenizer() throws ParseException {

        addTokenPattern("SPACE", "\\s+");
        addTokenPattern("LOGICAL_OPERATOR", "([Aa][Nn][Dd]|[Oo][Rr]|[Nn][Oo][Tt])");
        addTokenPattern("IDENTIFIER","([A-Za-z][A-Za-z0-9_#\\$]*)");
        addTokenPattern("QUOTED_IDENTIFIER","\"([A-Za-z][A-Za-z0-9_#\\$]*)\"");
        addTokenPattern("NUMBER_LITERAL","([0-9]+)");
        addTokenPattern("NUMBER_LITERAL","([0-9]+\\.[0-9]+)");
        addTokenPattern("NUMBER_LITERAL","(([0-9]+\\.))[^\\.]");
        addTokenPattern("NUMBER_LITERAL","(\\.[0-9]+)");
        addTokenPattern("RANGE","(\\.\\.)");
        addTokenPattern("STRING_LITERAL","'([^']|'')*'");
        addTokenPattern("SEMICOLON","(;)");
        addTokenPattern("ASSIGNMENT","(:=)");
        addTokenPattern("LEFT_BRACKET","(\\()");
        addTokenPattern("RIGHT_BRACKET","(\\))");
        addTokenPattern("DOT","(\\.)");
        addTokenPattern("COMMA","(,)");
        addTokenPattern("CONCATENATION","(\\|\\|)");
        addTokenPattern("PERCENTAGE","(%)");
        addTokenPattern("SHORT_COMMENT","--(.*?)\\r?\\n");
        addTokenPattern("LONG_COMMENT","/\\*(.*?)\\*/");
        addTokenPattern("NAMED_NOTATION","(=>)");
        addTokenPattern("LOGICAL_OPERATOR","(!=|<>|>=|<=|<|>|=)");
        addTokenPattern("ARITHMETIC_OPERATOR","(\\+|\\-|\\*|\\/)");

    }

    @Override
    public void addToken(Token token) {

        if (token.getName().equals("IDENTIFIER") || token.getName().equals("LOGICAL_OPERATOR")) {

            token.setValue(token.getValue().toUpperCase());
            super.addToken(token);

        } else if (token.getName().equals("SHORT_COMMENT") || token.getName().equals("LONG_COMMENT")) {

            AnnotationTokenizer annotationTokenizer = new AnnotationTokenizer();
            annotationTokenizer.ignoreTokens("ANNOTATION_SPACE");

            int tokenLine = token.getLine();
            int tokenPosition = token.getPosition();

            // Compensate -- or /*
            for (int i = 0; i <= 1; i++)
                if (token.getSource().charAt(i) == '\n') {
                    tokenLine++;
                    tokenPosition = 1;
                } else
                    tokenPosition++;

            try {
                annotationTokenizer.parse(token.getValue(), tokenLine, tokenPosition);
                addAll(annotationTokenizer);
            } catch (ParseException ex) {
                ex.printStackTrace();
                super.addToken(token);
            }

        } else
            super.addToken(token);


    }
}
