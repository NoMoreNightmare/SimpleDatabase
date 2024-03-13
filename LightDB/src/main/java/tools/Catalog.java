package tools;

import ed.inf.adbs.lightdb.LightDB;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Catalog {
    private static final Catalog CATALOG = new Catalog();
    private String dbPath;
    private String outputPath;
    private String schemaFile;
    private String sqlPath;

    private String database;
    private Catalog(){
        InputStream inputStream = LightDB.class.getClassLoader().getResourceAsStream("properties.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            dbPath = properties.getProperty("db.data.relative-path");
            schemaFile = properties.getProperty("db.schema");
            outputPath = properties.getProperty("output-path");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Catalog getInstance(){
        return CATALOG;
    }


    public String getDbPath() {
        return dbPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getSchemaFile() {
        return schemaFile;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setSchemaFile(String schemaPath) {
        this.schemaFile = schemaPath;
    }

    public String getSqlPath() {
        return sqlPath;
    }

    public void setSqlPath(String sqlPath) {
        this.sqlPath = sqlPath;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
}
