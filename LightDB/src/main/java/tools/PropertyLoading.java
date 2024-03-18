package tools;

import ed.inf.adbs.lightdb.LightDB;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * load the settings in the property file
 */
public class PropertyLoading {
    public static Properties properties;
    public static String sqlPath;

    static{
        InputStream inputStream = LightDB.class.getClassLoader().getResourceAsStream("properties.properties");
        properties = new Properties();
        try {
            properties.load(inputStream);
            sqlPath = properties.getProperty("input-path");
            String dbPath = properties.getProperty("db.data.relative-path");
            String database = properties.getProperty("db.database");
            String schema = properties.getProperty("db.schema");
            String output = properties.getProperty("output-path");

            Catalog.getInstance().setSqlPath(sqlPath);
            Catalog.getInstance().setSchemaFile(schema);
            Catalog.getInstance().setDbPath(dbPath);
            Catalog.getInstance().setDatabase(database);
            Catalog.getInstance().setOutputPath(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
