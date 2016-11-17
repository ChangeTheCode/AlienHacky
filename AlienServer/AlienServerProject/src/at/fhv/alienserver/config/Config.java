package at.fhv.alienserver.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by thomas on 09.11.16.
 */
public class Config {
    /*
     * Functionality created with:
     * http://stackoverflow.com/questions/18280419/reading-properties-file-in-java
     * http://www.drdobbs.com/jvm/readwrite-properties-files-in-java/231000005
     */

    private static Properties prop;
    private static final String propFileName = "AlienServer.properties";

    public Config() throws IOException{
        prop = new Properties();
        InputStream in = new FileInputStream(new File(propFileName));
        prop.load(in);
    }

    public enum AlienServerProperties{
        quadrant1Limit,
        quadrant2Limit,
        quadrant3Limit,
        quadrant4Limit
    }

    private static final Map<AlienServerProperties, String> propertyEnum2propertyString = new HashMap<>();
    static {
        propertyEnum2propertyString.put(AlienServerProperties.quadrant1Limit, "Quadrant1Limit");
        propertyEnum2propertyString.put(AlienServerProperties.quadrant2Limit, "Quadrant2Limit");
        propertyEnum2propertyString.put(AlienServerProperties.quadrant3Limit, "Quadrant3Limit");
        propertyEnum2propertyString.put(AlienServerProperties.quadrant4Limit, "Quadrant4Limit");
    }

    public boolean  setProperty(AlienServerProperties property, String value) throws IOException{
        String propertyString = Config.propertyEnum2propertyString.get(property);

        prop.setProperty(propertyString, value);
        File f = new File(propFileName);
        OutputStream out = new FileOutputStream(f);
        prop.store(out, "AlienServer - Properties");

        return true;
    }

    public String getProperty(AlienServerProperties property) throws IOException{
        String propertyString = Config.propertyEnum2propertyString.get(property);
        return prop.getProperty(propertyString);
    }
}
