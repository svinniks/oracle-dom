package grammar;

import simplegrammar.Grammar;
import simplegrammar.GrammarParseException;

import java.io.IOException;
import java.io.InputStreamReader;

public class AnnotationGrammar extends Grammar {

    public AnnotationGrammar() throws IOException, GrammarParseException {
        super();
        append(new InputStreamReader(getClass().getResourceAsStream("/annotations.grm")));
    }

}
