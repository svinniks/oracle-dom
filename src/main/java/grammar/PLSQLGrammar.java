package grammar;

import simplegrammar.Grammar;
import simplegrammar.GrammarParseException;

import java.io.IOException;
import java.io.InputStreamReader;

public class PLSQLGrammar extends Grammar {

    public PLSQLGrammar() {

        super();

        try {
            append(new InputStreamReader(getClass().getResourceAsStream("/grammar/plsql.grm")));
            append(new InputStreamReader(getClass().getResourceAsStream("/grammar/annotations.grm")));
        } catch (Exception ex) {
            throw new RuntimeException("Critical error has occured while loading PLSQL grammar!", ex);
        }

    }

}
