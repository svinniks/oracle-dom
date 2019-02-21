package grammar;

import simplegrammar.Grammar;
import simplegrammar.GrammarParseException;

import java.io.IOException;
import java.io.InputStreamReader;

public class PLSQLGrammar extends Grammar {

    public PLSQLGrammar() throws IOException, GrammarParseException {
        super();
        append(new InputStreamReader(getClass().getResourceAsStream("/plsql.grm")));
        append(new InputStreamReader(getClass().getResourceAsStream("/annotations.grm")));
    }

}
