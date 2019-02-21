package grammar;

import simplegrammar.ArrayListTokenStream;
import simplegrammar.GenericToken;
import simplegrammar.ParseException;

import java.io.IOException;
import java.io.Reader;

import static grammar.AnnotationTokenStream.State.*;

public class AnnotationTokenStream extends ArrayListTokenStream {

    public enum State {
        LF_ANNOTATION,
        LF_TOKEN,
        LF_ANNOTATION_NAME,
        R_STRING_VALUE,
        R_IDENTIFIER,
        R_ANNOTATION_NAME,
        R_ESCAPED,
        R_INTEGER,
        R_DECIMAL
    }

    private final Reader source;

    private State state;
    private char ch;
    StringBuilder tokenBuilder;

    public AnnotationTokenStream(Reader source) throws IOException, ParseException {
        this.source = source;
        parse();
    }

    private void lfAnnotation() throws ParseException {

        if (ch == '@')
            state = LF_ANNOTATION_NAME;
        else if (!Character.isWhitespace(ch))
            throw new ParseException(String.format("Unexpected character \"%c\"!", ch));

    }

    private void lfToken() throws ParseException {

        if (ch == '@')
            state = LF_ANNOTATION_NAME;
        else if (ch == '"') {
            tokenBuilder = new StringBuilder();
            state = R_STRING_VALUE;
        } else if (Character.isLetter(ch)) {
            tokenBuilder = new StringBuilder().append(Character.toUpperCase(ch));
            state = R_IDENTIFIER;
        } else if (Character.isDigit(ch) || ch == '-') {
            tokenBuilder = new StringBuilder().append(ch);
            state = R_INTEGER;
        } else if (ch == '.') {
            tokenBuilder = new StringBuilder().append(ch);
            state = R_DECIMAL;
        } else if (ch == '(') {
            addToken(new GenericToken("ANNOTATION_LEFT_BRACKET", "("));
            state = LF_TOKEN;
        } else if (ch == ')') {
            addToken(new GenericToken("ANNOTATION_RIGHT_BRACKET", ")"));
            state = LF_TOKEN;
        } else if (ch == '{') {
            addToken(new GenericToken("ANNOTATION_LEFT_CURLY_BRACKET", "{"));
            state = LF_TOKEN;
        } else if (ch == '}') {
            addToken(new GenericToken("ANNOTATION_RIGHT_CURLY_BRACKET", "}"));
            state = LF_TOKEN;
        }else if (ch == '=')
            addToken(new GenericToken("ANNOTATION_EQUATION", "="));
        else if (ch == ',') {
            addToken(new GenericToken("ANNOTATION_COMMA", ","));
            state = LF_TOKEN;
        } else if (!Character.isWhitespace(ch))
            throw new ParseException(String.format("Unexpected character \"%c\"!", ch));

    }

    private void lfAnnotationName() throws ParseException {

        if (Character.isLetter(ch)) {
            tokenBuilder = new StringBuilder().append(Character.toUpperCase(ch));
            state = R_ANNOTATION_NAME;
        } else
            throw new ParseException(String.format("Unexpected character \"%c\"!", ch));

    }

    private void rAnnotationName() throws ParseException {

        if (Character.isLetterOrDigit(ch) || ch == '_')
            tokenBuilder.append(Character.toUpperCase(ch));
        else if (Character.isWhitespace(ch)) {
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
            state = LF_TOKEN;
        } else if (ch == '(') {
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_LEFT_BRACKET", "("));
            state = LF_TOKEN;
        } else if (ch == ')') {
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_RIGHT_BRACKET", ")"));
            state = LF_TOKEN;
        } else if (ch == '{') {
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_LEFT_CURLY_BRACKET", "{"));
            state = LF_TOKEN;
        } else if (ch == '}') {
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_RIGHT_CURLY_BRACKET", "}"));
            state = LF_TOKEN;
        }else if (ch == '=') {
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_EQUATION", "="));
            state = LF_TOKEN;
        } else if (ch == ',') {
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_COMMA", ","));
            state = LF_TOKEN;
        } else
            throw new ParseException(String.format("Unexpected character \"%c\"!", ch));

    }

    private void rStringValue() {

        if (ch == '\\')
            state = R_ESCAPED;
        else if (ch == '"') {
            addToken(new GenericToken("ANNOTATION_STRING_LITERAL", tokenBuilder.toString()));
            state = LF_TOKEN;
        } else
            tokenBuilder.append(ch);

    }

    private void rEscaped() {

        tokenBuilder.append(ch);
        state = R_STRING_VALUE;

    }

    private void rIdentifier() throws ParseException {

        if (Character.isLetterOrDigit(ch) || ch == '_')
            tokenBuilder.append(Character.toUpperCase(ch));
        else if (Character.isWhitespace(ch)) {
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
            state = LF_TOKEN;
        } else if (ch == '(') {
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_LEFT_BRACKET", "("));
            state = LF_TOKEN;
        } else if (ch == ')') {
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_RIGHT_BRACKET", ")"));
            state = LF_TOKEN;
        } else if (ch == '{') {
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_LEFT_CURLY_BRACKET", "{"));
            state = LF_TOKEN;
        } else if (ch == '}') {
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_RIGHT_CURLY_BRACKET", "}"));
            state = LF_TOKEN;
        }else if (ch == '=') {
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_EQUATION", "="));
            state = LF_TOKEN;
        } else if (ch == ',') {
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_COMMA", ","));
            state = LF_TOKEN;
        } else
            throw new ParseException(String.format("Unexpected character \"%c\"!", ch));

    }

    private void rInteger() throws ParseException {

        if (ch == '.') {
            tokenBuilder.append(ch);
            state = R_DECIMAL;
        } else
            rDecimal();

    }

    private void rDecimal() throws ParseException {

        if (Character.isDigit(ch))
            tokenBuilder.append(ch);
        else if (Character.isWhitespace(ch)) {
            addToken(new GenericToken("ANNOTATION_NUMBER_LITERAL", tokenBuilder.toString()));
            state = LF_TOKEN;
        } else if (ch == '(') {
            addToken(new GenericToken("ANNOTATION_NUMBER_LITERAL", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_LEFT_BRACKET", "("));
            state = LF_TOKEN;
        } else if (ch == ')') {
            addToken(new GenericToken("ANNOTATION_NUMBER_LITERAL", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_RIGHT_BRACKET", ")"));
            state = LF_TOKEN;
        } else if (ch == '{') {
            addToken(new GenericToken("ANNOTATION_NUMBER_LITERAL", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_LEFT_CURLY_BRACKET", "{"));
            state = LF_TOKEN;
        } else if (ch == '}') {
            addToken(new GenericToken("ANNOTATION_NUMBER_LITERAL", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_RIGHT_CURLY_BRACKET", "}"));
            state = LF_TOKEN;
        }else if (ch == '=') {
            addToken(new GenericToken("ANNOTATION_NUMBER_LITERAL", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_EQUATION", "="));
            state = LF_TOKEN;
        } else if (ch == ',') {
            addToken(new GenericToken("ANNOTATION_NUMBER_LITERAL", tokenBuilder.toString()));
            addToken(new GenericToken("ANNOTATION_COMMA", ","));
            state = LF_TOKEN;
        } else
            throw new ParseException(String.format("Unexpected character \"%c\"!", ch));

    }

    private void parse() throws IOException, ParseException {

        addToken(new GenericToken("ANNOTATION_START"));

        state = LF_ANNOTATION;
        int chInt = source.read();

        while (chInt != -1) {

            ch = (char) chInt;

            if (state == LF_ANNOTATION)
                lfAnnotation();
            else if (state == LF_TOKEN)
                lfToken();
            else if (state == LF_ANNOTATION_NAME)
                lfAnnotationName();
            else if (state == R_ANNOTATION_NAME)
                rAnnotationName();
            else if (state == R_STRING_VALUE)
                rStringValue();
            else if (state == R_ESCAPED)
                rEscaped();
            else if (state == R_IDENTIFIER)
                rIdentifier();
            else if (state == R_INTEGER)
                rInteger();
            else if (state == R_DECIMAL)
                rDecimal();

            chInt = source.read();

        }

        if (state == R_ANNOTATION_NAME)
            addToken(new GenericToken("ANNOTATION_NAME", tokenBuilder.toString()));
        else if (state == R_IDENTIFIER)
            addToken(new GenericToken("ANNOTATION_IDENTIFIER", tokenBuilder.toString()));
        else if (state != LF_TOKEN)
            throw new ParseException("Unexpected end of the input in annotations!");

        addToken(new GenericToken("ANNOTATION_END"));

    }

}
