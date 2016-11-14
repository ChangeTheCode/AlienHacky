package at.fhv.alienserver.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by thomas on 09.11.16.
 */
public class Config {
    private static boolean initialised = false;
    private static Properties prop;
    private static OutputStream out;
    private static InputStream in;

    private static void initialise() throws IOException{
        Config.prop = new Properties();
        Config.out = new FileOutputStream("config.properties");
        Config.in = new FileInputStream("config.properties");
        /*Config.propertyEnum2propertyString = new HashMap<AlienServerProperties, String>(){

        };*/
        Config.initialised = true;
    }

    private static void teardown(){
        Config.out = null;
        Config.in = null;
        Config.prop = null;
        /*Config.propertyEnum2propertyString = null;*/
        Config.initialised = false;
    }

    private Config(){
        //we're building a Singleton here!
    }

    private static final Map<AlienServerProperties, String> propertyEnum2propertyString = new HashMap<>();
    static {
        propertyEnum2propertyString.put(AlienServerProperties.quadrant1Limit, "Quadrant1Limit");
        propertyEnum2propertyString.put(AlienServerProperties.quadrant2Limit, "Quadrant2Limit");
        propertyEnum2propertyString.put(AlienServerProperties.quadrant3Limit, "Quadrant3Limit");
        propertyEnum2propertyString.put(AlienServerProperties.quadrant4Limit, "Quadrant4Limit");
    }

    public enum AlienServerProperties{
        quadrant1Limit,
        quadrant2Limit,
        quadrant3Limit,
        quadrant4Limit
    }

    public static boolean  setProperty(AlienServerProperties property, String value){
        boolean returnValue = true;

        if(!Config.initialised){
            try {
                Config.initialise();
                returnValue = true;
            } catch (IOException e){
                e.printStackTrace();
                Config.teardown();
                returnValue = false;
            }
        }

        String propertyString = Config.propertyEnum2propertyString.get(property);

        try {
            prop.setProperty(propertyString, value);
            prop.store(Config.out, null);
            returnValue = true;
        } catch (IOException e){
            Config.teardown();
            returnValue = false;
        }

        return returnValue;
    }

    public static String getProperty(AlienServerProperties property) {
        if(!Config.initialised){
            try {
                Config.initialise();
            } catch (IOException e){
                e.printStackTrace();
                Config.teardown();
                return null;
            }
        }

        //TODO: Test the getProperty functionality
        String propertyString = Config.propertyEnum2propertyString.get(property);

        return prop.getProperty(propertyString);
    }
}
