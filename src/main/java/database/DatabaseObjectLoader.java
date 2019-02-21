package database;

import dom.DatabaseObject;
import simplegrammar.ParseException;

import java.sql.Connection;
import java.sql.SQLException;

abstract public class DatabaseObjectLoader<T extends DatabaseObject> {
    abstract protected T load(Connection connection, String owner, String objectName) throws SQLException, ParseException;
    abstract public String getObjectType();
}
