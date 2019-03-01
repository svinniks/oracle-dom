package grammar;

import simplegrammar.ParseException;
import simplegrammar.PatternTokenizer;
import simplegrammar.Token;

public class AnnotationTokenizer extends PatternTokenizer {

    public AnnotationTokenizer() {

        addTokenPattern("ANNOTATION_SPACE", "\\s+");
        addTokenPattern("ANNOTATION_NAME", "(@([A-Za-z][A-Za-z0-9_]*))");
        addTokenPattern("ANNOTATION_IDENTIFIER", "([A-Za-z][A-Za-z0-9_]*)");
        addTokenPattern("ANNOTATION_EQUATION", "=");
        addTokenPattern("ANNOTATION_COMMA", ",");
        addTokenPattern("ANNOTATION_LEFT_BRACKET", "\\(");
        addTokenPattern("ANNOTATION_RIGHT_BRACKET", "\\)");
        addTokenPattern("ANNOTATION_LEFT_CURLY_BRACKET", "\\{");
        addTokenPattern("ANNOTATION_RIGHT_CURLY_BRACKET", "\\}");
        addTokenPattern("ANNOTATION_NUMBER_LITERAL", "([0-9]+(?:\\.[0-9]+)?)");
        addTokenPattern("ANNOTATION_STRING_LITERAL", "\"((?:\\\\.|[^\"])*+)\"");

    }

    @Override
    public void addToken(Token token) {

        if (token.getName().equals("ANNOTATION_NAME") || token.getName().equals("ANNOTATION_IDENTIFIER"))
            token.setValue(token.getValue().toUpperCase());
        else if (token.getName().equals("ANNOTATION_STRING_LITERAL"))
            token.setValue(token.getValue().replace("\\\"", "\""));

        super.addToken(token);

    }

    @Override
    public void parse(CharSequence source, int startLine, int startPosition) throws ParseException {
        addToken(new Token("ANNOTATION_START"));
        super.parse(source, startLine, startPosition);
        addToken(new Token("ANNOTATION_END"));
    }
}
