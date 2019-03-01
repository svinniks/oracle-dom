package database;

import dom.CodeItem;
import grammar.PLSQLGrammar;
import grammar.PLSQLTokenizer;
import simplegrammar.*;

import java.sql.*;

abstract public class CodeItemLoader<T extends CodeItem> extends DatabaseObjectLoader<T> {

    public final static Grammar PLSQL_GRAMMAR = new PLSQLGrammar();

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

        PLSQLTokenizer tokenizer = new PLSQLTokenizer();
        tokenizer.ignoreTokens("SPACE", "SHORT_COMMENT", "LONG_COMMENT");
        tokenizer.parse(source);

        SyntaxTreeNode syntaxTree;

        try {
            syntaxTree = PLSQL_GRAMMAR.parse(tokenizer, rootRuleName);
        } catch (GrammarException ex) {
            throw new RuntimeException("Unexpected error has occurred while running grammar!", ex);
        }

        return syntaxTree;

    }

}
