package database;

import dom.CodeItem;
import grammar.PLSQLGrammar;
import grammar.PLSQLTokenStream;
import simplegrammar.*;

import java.io.IOException;
import java.io.StringReader;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

abstract public class CodeItemLoader<T extends CodeItem> extends DatabaseObjectLoader<T> {

    public final static Grammar PLSQL_GRAMMAR;
    public final static Set<String> PLSQL_IGNORED_TOKENS;

    static {

        PLSQL_GRAMMAR = new PLSQLGrammar();

        PLSQL_IGNORED_TOKENS = new HashSet<>();
        PLSQL_IGNORED_TOKENS.add("SPACE");
        PLSQL_IGNORED_TOKENS.add("SHORT_COMMENT");
        PLSQL_IGNORED_TOKENS.add("LONG_COMMENT");

    }

    protected String loadSource(Connection connection, String objectType, String owner, String objectName) throws SQLException {

        try (CallableStatement statement = connection.prepareCall(SQL.load("get_object_source.sql"))) {

            statement.setString(1, objectType);
            statement.setString(2, owner);
            statement.setString(3, objectName);
            statement.registerOutParameter(4, Types.CLOB);

            statement.execute();

            Clob sourceClob = statement.getClob(4);
            String source = sourceClob.getSubString(1, (int) sourceClob.length());
            sourceClob.free();

            return source;

        }

    }

    protected SyntaxTreeNode parseSource(String source, String rootRuleName) throws ParseException {

        TokenStream tokenStream;

        try {
            tokenStream = new PLSQLTokenStream(new StringReader(source), PLSQL_IGNORED_TOKENS);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error has occurred while reading token stream!", ex);
        }

        SyntaxTreeNode syntaxTree;

        try {
            syntaxTree = PLSQL_GRAMMAR.parse(tokenStream, rootRuleName);
        } catch (GrammarException ex) {
            throw new RuntimeException("Unexpected error has occurred while running grammar!", ex);
        }

        return syntaxTree;

    }

}
