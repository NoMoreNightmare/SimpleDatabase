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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
