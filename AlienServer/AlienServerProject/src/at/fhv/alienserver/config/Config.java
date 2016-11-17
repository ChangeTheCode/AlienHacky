package at.fhv.alienserver.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by thomas on 09.11.16.
 */
public class Config {
    private static Properties prop;
    private static final String propFileName = "AlienServer.properties";

    public Config(){
        prop = new Properties();
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

    public boolean  setProperty(AlienServerProperties property, String value){
        /*
        Used to revise: http://www.drdobbs.com/jvm/readwrite-properties-files-in-java/231000005
        TODO: test this function
        */
        String propertyString = Config.propertyEnum2propertyString.get(property);

        prop.setProperty(propertyString, value);
        try {
            File f = new File(propFileName);
            OutputStream out = new FileOutputStream(f);
            prop.store(out, ""); //Empty string can resolve a comment, however not needed for us; nevertheless included 'cause Java requires it. WTF...
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return false;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String getProperty(AlienServerProperties property) throws IOException{
        /*
        Made with: http://stackoverflow.com/questions/18280419/reading-properties-file-in-java
        See also: http://www.drdobbs.com/jvm/readwrite-properties-files-in-java/231000005
        Watch out for classpath ('cause of getClass() below)
        TODO: Test
         */
        InputStream in = getClass().getResourceAsStream(propFileName);

        prop.load(in);

        String propertyString = Config.propertyEnum2propertyString.get(property);

        return prop.getProperty(propertyString);
    }
}
