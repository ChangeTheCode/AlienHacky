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

        System.out.println("What serial port should be used?\n");
        lineOfInput = inputReader.readLine();
        config.setProperty(Config.AlienServerProperties.com_port, lineOfInput);

        System.out.println("What value shall be used for simulation parameter A (enter blank for default -1.7\n");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_A, "-1.7");
        } else {
            config.setProperty(Config.AlienServerProperties.calculator_A, lineOfInput);
        }

        System.out.println("What value shall be used for simulation parameter b (enter blank for default 1.5\n");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_A, "1.5");
        } else {
            config.setProperty(Config.AlienServerProperties.calculator_A, lineOfInput);
        }

        System.out.println("What value shall be used for simulation parameter c (enter blank for default 1\n");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_A, "1");
        } else {
            config.setProperty(Config.AlienServerProperties.calculator_A, lineOfInput);
        }

        System.out.println("What value shall be used for simulation parameter d (enter blank for default 0\n");
        System.out.println("Warning: Any other value than zero will likely produce unwanted behaviour\n");
        lineOfInput = inputReader.readLine();
        if(lineOfInput.equals("")){
            config.setProperty(Config.AlienServerProperties.calculator_A, "0");
        } else {
            System.out.println("You nuts?\n");
            config.setProperty(Config.AlienServerProperties.calculator_A, lineOfInput);
        }
    }
}
