package at.fhv.alienserver.calibrators;

import java.io.IOException;

/**
 * Created by thomas on 13.12.16. Holds the main-method that, when started, guides the user through the calibration
 * process of the hardware of the AlienHacky project.
 */
public class Calibration {

    public static void main(String[] args){
        MHCalibrator mhCalib = new MHCalibrator();
        PlayingAreaCalibrator playCalib = new PlayingAreaCalibrator();
        AskingCalibrator askCalib = new AskingCalibrator();

        try {
            mhCalib.execute();
        } catch (IOException e){
            System.err.println("IO Error while calibrating Moving Head");
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            playCalib.execute();
        } catch (IOException e) {
            System.err.println("IO Error while calibrating Playing area dimensions");
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

        try{
            askCalib.execute();
        } catch (IOException e){
            System.err.println("IO Error while while retrieving calibration values from user");
            System.err.println(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

    }

}
