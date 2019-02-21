package database;

import dom.DatabaseObject;
import dom.Package;
import simplegrammar.ParseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DOM {

    private static final Map<Class<? extends DatabaseObject>, DatabaseObjectLoader<?>> LOADER_MAP;

    static {
        LOADER_MAP = new HashMap<>();
        LOADER_MAP.put(Package.class, new PackageLoader());
    }

    public static <T extends DatabaseObject> T load(Connection connection, Class<T> objectType, String owner, String objectName) throws SQLException, DOMException, ParseException {

        DatabaseObjectLoader loader = LOADER_MAP.get(objectType);

        if (loader == null)
            throw new DOMException("Unsupported DOM object class %1!", objectType.getSimpleName());

        try (PreparedStatement statement = connection.prepareStatement(SQL.load("get_object.sql"))) {

            statement.setString(1, loader.getObjectType());
            statement.setString(2, owner);
            statement.setString(3, objectName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next())
                    throw new DOMException("%s %s.%s could not be found!", objectType, owner, objectName);
            }

            return (T)loader.load(connection, owner, objectName);

        }

    }

}
