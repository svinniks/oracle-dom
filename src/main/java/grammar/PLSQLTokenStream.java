package grammar;

import simplegrammar.ArrayListTokenStream;
import simplegrammar.GenericToken;
import simplegrammar.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static grammar.PLSQLTokenStream.ParseState.*;

/**
 * TokenStream, which parses a PL/SQL code.
 * <br>Implementation is based of the finite automata principles. Each state is processed with a separate procedure.
 * <br>The following tokens are produced:
 * <ul>
 * <li>IDENTIFIER</li>
 * <li>QUOTED_IDENTIFIER</li>
 * <li>LEFT_BRACKET</li>
 * <li>RIGHT_BRACKET</li>
 * <li>DOT</li>
 * <li>COMMA</li>
 * <li>SEMICOLON</li>
 * <li>PERCENTAGE</li>
 * <li>NUMBER_LITERAL</li>
 * <li>STRING_LITERAL (value without quotes)</li>
 * <li>LOGICAL_OPERATOR (&gt;, &lt;, &gt;=, &lt;=, =, !=)</li>
 * <li>ARITHMETIC_OPERATOR (+, -, *, /)</li>
 * <li>ASSIGNMENT (:=)</li>
 * <li>NAMED_NOTATION (=&gt;)
 * <li>SPACE (whitespace, linefeed and tab characters or their combination)</li>
 * <li>LONG_COMMENT (without &#47;* and *&#47;)</li>
 * <li>SHORT_COMMENT (without --)</li>
 * </ul>
 * @author s.vinniks
 */
public class PLSQLTokenStream extends ArrayListTokenStream {

    private final static Pattern DOCUMENTATION_PATTERN = Pattern.compile("(?s)\\*.*");
    private final static Set<String> LOGICAL_OPERATORS;

    static {

        LOGICAL_OPERATORS = new HashSet<>();
        LOGICAL_OPERATORS.add("AND");
        LOGICAL_OPERATORS.add("OR");
        LOGICAL_OPERATORS.add("NOT");

    }

    protected static enum ParseState {
        LF_IDENTIFIER,
        R_QUOTED_IDENTIFIER,
        R_IDENTIFIER,
        LF_LONG_COMMENT_START,
        R_LONG_COMMENT,
        LF_LONG_COMMENT_END,
        LF_SHORT_COMMENT_START,
        R_SHORT_COMMENT,
        LF_ASSIGNMENT_EQUATION,
        R_INTEGER_PART,
        R_DECIMAL_PART,
        R_STRING_LITERAL,
        LF_SECOND_QUOTE,
        LF_PARAM_NAMED_NOTATION,
        LF_CONCATENATION_OPERATOR,
        LF_LESS_COMPOUND,
        LF_MORE_COMPOUND,
        LF_NON_EQUAL
    }
    
    private final Reader source;
    private final Set<String> ignoredTokens;

    private ParseState state;
    private String tokenName;
    private StringBuilder tokenValueBuilder;

    public PLSQLTokenStream(Reader source, Set<String> ignoredTokens) throws IOException, ParseException {
        
        this.source = source;
        this.ignoredTokens = ignoredTokens;
        
        parseSource();

    }

    public PLSQLTokenStream(Reader source) throws IOException, ParseException {
        this(source, new HashSet<String>());
    }

    private void addToken(String value) {

        if (!ignoredTokens.contains(tokenName) && (value.length() > 0 || !tokenName.equals("SPACE")))
            addToken(new GenericToken(tokenName, value));

    }

    private void addToken() {
        
        String tokenValue = tokenValueBuilder.toString();

        if (tokenName.equals("IDENTIFIER") && LOGICAL_OPERATORS.contains(tokenValue)) {
            tokenName = "LOGICAL_OPERATOR";
            addToken(tokenValue);
        } else if (tokenName.equals("LONG_COMMENT") && DOCUMENTATION_PATTERN.matcher(tokenValue).matches()) {
            tokenName = "DOCUMENTATION";
            addToken(tokenValue.substring(1));
        } else if (tokenName.equals("LONG_COMMENT") || tokenName.equals("SHORT_COMMENT"))
            parseAnnotations(tokenValue);
        else
            addToken(tokenValue);

        tokenName = null;
        tokenValueBuilder = new StringBuilder();
        
    }

    private void parseAnnotations(String source) {

        try {
            addAll(new AnnotationTokenStream(new StringReader(source)));
        } catch (ParseException | IOException ex) {
            addToken(source);
        }

    }
    
    private void lfIdentifier(char c) throws ParseException {
        
        if (Character.isLetter(c)) {
            
            addToken();
            
            tokenName = "IDENTIFIER";
            tokenValueBuilder.append(Character.toUpperCase(c));
            
            state = R_IDENTIFIER;
            
        } else if (c == '"') {
        
            addToken();
            
            tokenName = "QUOTED_IDENTIFIER";
            
            state = R_QUOTED_IDENTIFIER;
            
        } else if (Character.isWhitespace(c)) {
            
            tokenValueBuilder.append(c);
            
        } else if (c == '(') {
            
            addToken();
            
            tokenName = "LEFT_BRACKET";
            tokenValueBuilder.append('(');
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else if (c == ')') {
            
            addToken();
            
            tokenName = "RIGHT_BRACKET";
            tokenValueBuilder.append(')');
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else if (c == '%') {
            
            addToken();
            
            tokenName = "PERCENTAGE";
            tokenValueBuilder.append('%');
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else if (c == '.') {
            
            addToken();
            
            tokenName = "DOT";
            tokenValueBuilder.append('.');
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else if (c == ';') {
            
            addToken();
            
            tokenName = "SEMICOLON";
            tokenValueBuilder.append(';');
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else if (c == ',') {
            
            addToken();
            
            tokenName = "COMMA";
            tokenValueBuilder.append(',');
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else if (c == '/') {
            
            addToken();
            
            state = LF_LONG_COMMENT_START;
            
        } else if (c == '-') {
            
            addToken();
            
            state = LF_SHORT_COMMENT_START;
            
        } else if (c == ':') {
        
            addToken();
            
            state = LF_ASSIGNMENT_EQUATION;
            
        } else if (Character.isDigit(c)) {
            
            addToken();
            
            tokenName = "NUMBER_LITERAL";
            tokenValueBuilder.append(c);
            
            state = R_INTEGER_PART;
            
        } else if (c == '\'') {
            
            addToken();
            
            tokenName = "STRING_LITERAL";
            
            state = R_STRING_LITERAL;
            
        } else if (c == '=') {
            
            addToken();
            
            tokenName = "LOGICAL_OPERATOR";
            tokenValueBuilder.append('=');
            
            state = LF_PARAM_NAMED_NOTATION;
            
        } else if (c == '|') {
            
            addToken();
            
            state = LF_CONCATENATION_OPERATOR;
            
        } else if (c == '<') {
        
            addToken();
            
            tokenName = "LOGICAL_OPERATOR";
            tokenValueBuilder.append('<');
            
            state = LF_LESS_COMPOUND;
            
        } else if (c == '>') {
        
            addToken();
            
            tokenName = "LOGICAL_OPERATOR";
            tokenValueBuilder.append('>');
            
            state = LF_MORE_COMPOUND;
            
        } else if (c == '*' || c == '+') {
            
            addToken();
            
            tokenName = "ARITHMETIC_OPERATOR";
            tokenValueBuilder.append(c);
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            
        } else if (c == '!') {
            
            addToken();
            
            state = LF_NON_EQUAL;
            
        } else
            throw new ParseException("Unexpected character \"" + c + "\"!");
        
    }
    
    private void rQuotedIdentifier(char c) throws ParseException {
        
        if (c == '"') {
            
            if (tokenValueBuilder.length() == 0)
                throw new ParseException("Quoted identifier can't be NULL!");
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            
        } else
            tokenValueBuilder.append(c);
        
    }
    
    private void rIdentifier(char c) throws ParseException {
        
        if (String.valueOf(c).matches("[a-zA-Z0-9_\\$#]")) {
            
            if (tokenValueBuilder.length() >= 30)
                throw new ParseException("Identifier too long!");
            
            tokenValueBuilder.append(Character.toUpperCase(c));
            
        } else {
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void lfLongCommentStart(char c) {
        
        if (c == '*') {
        
            tokenName = "LONG_COMMENT";
            
            state = R_LONG_COMMENT;
            
        } else {
            
            tokenName = "ARITHMETIC_OPERATOR";
            tokenValueBuilder.append("/");
            addToken();
            
            tokenName = "SPACE";
                        
            state = LF_IDENTIFIER;
            
        }
              
        
    }
    
    private void rLongComment(char c) {
        
        if (c == '*') {
            
            state = LF_LONG_COMMENT_END;
            
        } else {
            
            tokenValueBuilder.append(c);
            
        }
        
    }
    
    private void lfLongCommentEnd(char c) {
        
        if (c == '/') {
        
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
        
        } else {
            
            tokenValueBuilder.append('*');
            
            state = R_LONG_COMMENT;
            
            rLongComment(c);
            
        }
        
    }
    
    private void lfShortCommentStart(char c) throws ParseException {
        
        if (c == '-') {
            
            tokenName = "SHORT_COMMENT";
            
            state = R_SHORT_COMMENT;
            
        } else {
            
            tokenName = "ARITHMETIC_OPERATOR";
            tokenValueBuilder.append('-');
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void rShortComment(char c) {
        
        if (c == '\n') {
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            
        } else
            
            tokenValueBuilder.append(c);
        
    }
    
    private void lfAssignmentEquation(char c) throws ParseException {
        
        if (c == '=') {
            
            tokenName = "ASSIGNMENT";
            tokenValueBuilder.append(":=");
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            
        } else
            
            throw new ParseException("Unexpected character \"" + c + "\"!");
                
    }
    
    private void rIntegerPart(char c) throws ParseException {
        
        if (Character.isDigit(c)) {
            
            tokenValueBuilder.append(c);
            
        } else if (c == '.') {
        
            tokenValueBuilder.append(c);
            
            state = R_DECIMAL_PART;
        
        } else {
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void rDecimalPart(char c) throws ParseException {
        
        if (Character.isDigit(c)) {
            
            tokenValueBuilder.append(c);
            
        } else {
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void rStringLiteral(char c) {
        
        if (c == '\'') {
            
            state = LF_SECOND_QUOTE;
            
        } else {
            
            tokenValueBuilder.append(c);
            
        }
        
    }
    
    private void lfSecondQuote(char c) throws ParseException {
        
        if (c == '\'') {
            
            tokenValueBuilder.append('\'');
            
            state = R_STRING_LITERAL;
            
        } else {
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void lfParamNamedNotation(char c) throws ParseException {
        
        if (c == '>') {
        
            tokenName = "NAMED_NOTATION";
            tokenValueBuilder.append(c);
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            
        } else {
        
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void lfConcatenationOperator(char c) throws ParseException {
        
        if (c == '|') {
            
            tokenName = "CONCATENATION_OPERATOR";
            tokenValueBuilder.append("||");
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            
        } else
            
            throw new ParseException("Unexpected character \"" + c + "\"!");
        
    }
    
    private void lfLessCompound(char c) throws ParseException {
        
        if (c == '>' || c == '=') {
            
            tokenValueBuilder.append(c);
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else {
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void lfMoreCompound(char c) throws ParseException {
        
        if (c == '=') {
            
            tokenValueBuilder.append(c);
            addToken();
            
            tokenName = "SPACE";
            state = LF_IDENTIFIER;
            
        } else {
            
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            lfIdentifier(c);
            
        }
        
    }
    
    private void lfNonEqual(char c) throws ParseException {
        
        if (c == '=') {
            
            tokenName = "LOGICAL_OPERATOR";
            tokenValueBuilder.append("!=");
            addToken();
            
            tokenName = "SPACE";
            
            state = LF_IDENTIFIER;
            
        } else
            
            throw new ParseException("Unexpected character \"" + c + "\"!");
        
    }
    
    private void parseSource() throws IOException, ParseException {
        
        tokenName = "SPACE";
        tokenValueBuilder = new StringBuilder();
        
        state = LF_IDENTIFIER;
        int chInt = source.read();

        while (chInt != -1) {

            char c = (char)chInt;

            if (state == LF_IDENTIFIER)
                lfIdentifier(c);
            else if (state == R_QUOTED_IDENTIFIER)
                rQuotedIdentifier(c);
            else if (state == R_IDENTIFIER)
                rIdentifier(c);
            else if (state == LF_LONG_COMMENT_START)
                lfLongCommentStart(c);
            else if (state == R_LONG_COMMENT)
                rLongComment(c);
            else if (state == LF_LONG_COMMENT_END)
                lfLongCommentEnd(c);
            else if (state == LF_SHORT_COMMENT_START)
                lfShortCommentStart(c);
            else if (state == R_SHORT_COMMENT)
                rShortComment(c);
            else if (state == LF_ASSIGNMENT_EQUATION)
                lfAssignmentEquation(c);
            else if (state == R_INTEGER_PART)
                rIntegerPart(c);
            else if (state == R_DECIMAL_PART)
                rDecimalPart(c);
            else if (state == R_STRING_LITERAL)
                rStringLiteral(c);
            else if (state == LF_SECOND_QUOTE)
                lfSecondQuote(c);
            else if (state == LF_PARAM_NAMED_NOTATION)
                lfParamNamedNotation(c);
            else if (state == LF_CONCATENATION_OPERATOR)
                lfConcatenationOperator(c);
            else if (state == LF_LESS_COMPOUND)
                lfLessCompound(c);
            else if (state == LF_MORE_COMPOUND)
                lfMoreCompound(c);
            else if (state == LF_NON_EQUAL)
                lfNonEqual(c);

            chInt = source.read();
            
        }
        
        if (state == R_IDENTIFIER || state == LF_IDENTIFIER || state == R_INTEGER_PART || state == R_DECIMAL_PART || state == R_SHORT_COMMENT || state == LF_SECOND_QUOTE
                || state == LF_LESS_COMPOUND || state == LF_MORE_COMPOUND || state == LF_PARAM_NAMED_NOTATION)
            addToken();
        else
            throw new ParseException("Unexpected end of the input!");
        
    }

}
