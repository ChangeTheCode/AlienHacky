package at.fhv.alienserver.calibrators;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.config.Config;
import at.fhv.alienserver.movingHead.MHControl;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 13.12.16.
 *
 * The main function in this class serves the purpose of defining the corners of the playing area.
 */
public class PlayingAreaCalibrator {
    private static boolean run = true;

    public static void main(String[] args) throws IOException {
        //A queue for holding the pressed keys
        ArrayBlockingQueue<Character> characterQueue = new ArrayBlockingQueue<>(1000);

        MHControl mhc = new MHControl(2.0, false, false);


        //Get a keylistener up and running
        JTextArea inputText = new JTextArea();
        JFrame guiFrame = new JFrame();
        guiFrame.getContentPane().setBackground(Color.BLACK);
        guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        guiFrame.setTitle("Input for calibrators");
        guiFrame.setLocationRelativeTo(null);
        AlienWindowListener wListener = new AlienWindowListener();
        guiFrame.addWindowListener(wListener);
        AlienKeyListener kListener = new AlienKeyListener(characterQueue);
        inputText.addKeyListener(kListener);
        guiFrame.add(inputText, BorderLayout.NORTH);
        guiFrame.setSize(700,200);
        guiFrame.setVisible(true);
        // Did we seriously just need 12 lines of code to have a key-listener?!?!

        System.out.println("Use the WASD keys to move the light spot");
        System.out.println("Use the number keys 1 - 4 to set the corner in the respective quadrant");
        System.out.println("Close input window to terminate application and save values");

        Character c;
        CoordinateContainer newCoordinates = new CoordinateContainer(0, 0);

        Config config = new Config();

        while(run) {
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if (c == null){
                    //Currently there's no character present, so just don't do anything
                } else if (c == 'w') {
                    newCoordinates.setX( newCoordinates.getX() + 0.1 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if (c == 's') {
                    newCoordinates.setX( newCoordinates.getX() - 0.1 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if (c == 'd') {
                    newCoordinates.setY( newCoordinates.getY() + 0.1 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                    newCoordinates.setY( newCoordinates.getY() - 0.1 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == '1'){
                    System.out.println("Saved corner in quadrant 1");
                    config.setProperty(Config.AlienServerProperties.quadrant1Limit, newCoordinates.toString());
                    c = null;
                } else if(c == '2'){
                    System.out.println("Saved corner in quadrant 2");
                    config.setProperty(Config.AlienServerProperties.quadrant2Limit, newCoordinates.toString());
                    c = null;
                } else if(c == '3'){
                    System.out.println("Saved corner in quadrant 3");
                    config.setProperty(Config.AlienServerProperties.quadrant3Limit, newCoordinates.toString());
                    c = null;
                } else if(c == '4'){
                    System.out.println("Saved corner in quadrant 4");
                    config.setProperty(Config.AlienServerProperties.quadrant4Limit, newCoordinates.toString());
                    c = null;
                }
            } catch (InterruptedException e) {
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }
    }

    public static void setRunning(boolean status){
        PlayingAreaCalibrator.run = status;
    }

}
