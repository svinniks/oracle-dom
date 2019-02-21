package database;

import dom.Table;
import simplegrammar.ParseException;

import java.sql.Connection;
import java.sql.SQLException;

public class TableLoader extends DatabaseObjectLoader<Table> {

    @Override
    protected Table load(Connection connection, String owner, String objectName) throws SQLException, ParseException {
        return new Table();
    }

    @Override
    public String getOracleObjectType() {
        return "TABLE";
    }
}
