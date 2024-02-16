package pojo;

import ed.inf.adbs.lightdb.LightDB;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Catalog {
    private static final Catalog CATALOG = new Catalog();
    private String dbPath;
    private String outputPath;
    private String schemaPath;

    private Catalog(){
        InputStream inputStream = LightDB.class.getClassLoader().getResourceAsStream("properties.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            dbPath = properties.getProperty("db.data.relative-path");
            schemaPath = properties.getProperty("db.schema");
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

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }
}
