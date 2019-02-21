package database;

import dom.DOMBuilder;
import grammar.PLSQLGrammar;
import grammar.PLSQLTokenStream;
import simplegrammar.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.sql.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static database.DatabaseObjectResolver.Role.DBA;

public class DatabaseObjectResolver {

    public enum Role {
        OWNER,
        DBA
    }

    public final static Grammar PLSQL_GRAMMAR;
    public final static Set<String> PLSQL_IGNORED_TOKENS;

    static {

        PLSQL_GRAMMAR = new PLSQLGrammar();

        PLSQL_IGNORED_TOKENS = new HashSet<>();
        PLSQL_IGNORED_TOKENS.add("SPACE");
        PLSQL_IGNORED_TOKENS.add("SHORT_COMMENT");
        PLSQL_IGNORED_TOKENS.add("LONG_COMMENT");

    }

    private final Role role;

    public DatabaseObjectResolver(Role role) {
        this.role = role;
    }

    private String loadSql(String fileName) {

        InputStream inputStream = getClass().getResourceAsStream(String.format("/sql/%s", fileName));
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");

        return scanner.hasNext() ? scanner.next() : "";

    }

    private String loadCodeItemSource(Connection connection, String objectType, String owner, String objectName) throws SQLException {

        try (CallableStatement statement = connection.prepareCall(loadSql("get_object_source.sql"))) {

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

    private SyntaxTreeNode parseCodeItemSource(String objectType, String source) throws ParseException {

        TokenStream tokenStream;

        try {
            tokenStream = new PLSQLTokenStream(new StringReader(source), PLSQL_IGNORED_TOKENS);
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error has occurred while reading package token stream!", ex);
        }

        SyntaxTreeNode syntaxTree;
        String rootRuleName;

        if (objectType.equals("PACKAGE"))
            rootRuleName = "PACKAGE";
        else
            throw new RuntimeException(String.format("Unsupported code item type %s!", objectType));

        try {
            syntaxTree = PLSQL_GRAMMAR.parse(tokenStream, rootRuleName);
        } catch (GrammarException ex) {
            throw new RuntimeException("Unexpected error has occurred while running grammar!", ex);
        }

        return syntaxTree;

    }

    public Object loadPackageDOM(Connection connection, String owner, String objectName) throws ParseException, SQLException {

        String source = loadCodeItemSource(connection, "PACKAGE", owner, objectName);
        SyntaxTreeNode syntaxTree = parseCodeItemSource("PACKAGE", source);

        return DOMBuilder.buildPackage(syntaxTree);

    }

    public Object loadObjectDOM(Connection connection, String owner, String objectName) throws SQLException, DOMException, ParseException {

        String sql = String.format(
                "SELECT object_type FROM %s WHERE owner = ? AND object_name = ?",
                role == DBA ? "dba_objects" : "all_objects"
        );

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, owner);
            statement.setString(2, objectName);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    String objectType = resultSet.getString(1);

                    if (objectType.equals("PACKAGE"))
                        return loadPackageDOM(connection, owner, objectName);
                    else
                        throw new DOMException("Database object type %1 is not currently supported!", objectType);

                } else
                    throw new DOMException("Database object %s.%s could not be found!", owner, objectName);

            }

        }

    }

}
