package at.fhv.alienserver.calibrators;

import at.fhv.alienserver.config.Config;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by thomas on 16.12.16.
 */
public class AskingCalibrator implements ICalibrator {
    private boolean run = true;

    @Override
    public void setRunning(boolean status) {
        this.run = status;
    }

    public AskingCalibrator(){
        /*Quite the boring ctor, my dear*/
    }

    public void execute() throws IOException{
        Config config = new Config();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        String lineOfInput;

        System.out.println("\nWhat serial port should be used?");
        lineOfInput = inputReader.readLine();
        config.setProperty(Config.AlienServerProperties.com_port, lineOfInput);

        System.out.println("\nWhat value shall be used for simulation parameter A (enter blank for default -1.7)");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_A, "-1.7");
        } else {
            config.setProperty(Config.AlienServerProperties.calculator_A, lineOfInput);
        }

        System.out.println("\nWhat value shall be used for simulation parameter b (enter blank for default 1.5)");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_b, "1.5");
        } else {
            config.setProperty(Config.AlienServerProperties.calculator_b, lineOfInput);
        }

        System.out.println("\nWhat value shall be used for simulation parameter c (enter blank for default 1)");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_c, "1");
        } else {
            config.setProperty(Config.AlienServerProperties.calculator_c, lineOfInput);
        }

        System.out.println("\nWhat value shall be used for simulation parameter d (enter blank for default 0");
        System.out.println("Warning: Any other value than zero will likely produce unwanted behaviour");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_d, "0");
        } else {
            System.out.println("You nuts?");
            config.setProperty(Config.AlienServerProperties.calculator_d, lineOfInput);
        }

        System.out.println("Thank you for your cooperation, you may now get a cup of tea or coffee; just as you desire");
    }
}
