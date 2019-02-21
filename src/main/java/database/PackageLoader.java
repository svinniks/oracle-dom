package database;

import dom.DOMBuilder;
import dom.Package;
import simplegrammar.ParseException;
import simplegrammar.SyntaxTreeNode;

import java.sql.Connection;
import java.sql.SQLException;

public class PackageLoader extends CodeItemLoader<Package> {

    @Override
    protected Package load(Connection connection, String owner, String objectName) throws SQLException, ParseException {

        String source = loadSource(connection, "PACKAGE", owner, objectName);
        SyntaxTreeNode syntaxTree = parseSource(source, "PACKAGE");

        return DOMBuilder.buildPackage(syntaxTree);

    }

    @Override
    public String getOracleObjectType() {
        return "PACKAGE";
    }
}
