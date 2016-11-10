package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.CoordinateContainer;
import at.fhv.alienserver.config.Config;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 07.11.16.
 * TODO: Send initial packet to set the MH to position (0,0) (for reasons of beauty :-) )
 * TODO: Fix deadlock when queue overfills
 * TODO: Implement exception handling at character reading
 * TODO: Make continuous movement by holding down a key possible
 */
public class Calibrator {

    private static boolean run = true;

    public static void main(String[] args){

        //A queue for holding the pressed keys
        ArrayBlockingQueue<CoordinateContainer> coordinates = new ArrayBlockingQueue<>(1);

        //Get MH - Control up and running
        MHControl myMH = new MHControl(coordinates, 1);
        Thread mhThread = new Thread(myMH);
        mhThread.start();

        //A queue for holding the pressed keys
        ArrayBlockingQueue<Character> characterQueue = new ArrayBlockingQueue<>(1000);

        //Get a keylistener up and running
        JTextArea inputText = new JTextArea();
        JFrame guiFrame = new JFrame();
        guiFrame.getContentPane().setBackground(Color.BLACK);
        guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        guiFrame.setTitle("Input for calibrator");
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
        System.out.println("Close input window to terminate application");
        System.out.println("If you don't know what quadrants are or what WASD controls are: just go home...");

        Character c;
        CoordinateContainer newCoordinates = new CoordinateContainer(0, 0, 0);

        while(run) {
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if (c == null){
                    //Currently there's no character present, so just don't do anything
                } else if (c == 'w') {
                    newCoordinates.x += 0.1;
                    coordinates.put(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c == 's') {
                    newCoordinates.x -= 0.1;
                    coordinates.add(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c == 'd') {
                    newCoordinates.y += 0.1;
                    coordinates.add(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c == 'a') {
                    newCoordinates.y -= 0.1;
                    coordinates.add(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if(c == '1'){
                    System.out.println("Saved corner in quadrant 1");
                    Config.setProperty(Config.AlienServerProperties.quadrant1Limit, newCoordinates.toString());
                    c = null;
                } else if(c == '2'){
                    System.out.println("Saved corner in quadrant 2");
                    Config.setProperty(Config.AlienServerProperties.quadrant2Limit, newCoordinates.toString());
                    c = null;
                } else if(c == '3'){
                    System.out.println("Saved corner in quadrant 3");
                    Config.setProperty(Config.AlienServerProperties.quadrant3Limit, newCoordinates.toString());
                    c = null;
                } else if(c == '4'){
                    System.out.println("Saved corner in quadrant 4");
                    Config.setProperty(Config.AlienServerProperties.quadrant4Limit, newCoordinates.toString());
                    c = null;
                }
            } catch (InterruptedException e) {
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }
        /*
         * Note: this call to exit() can cause a race condition with the open input window and the listeners. In the
         * current state of the project, this race condition does not matter; should calibration problems occur in the
         * future, this might be the problem.
         */
        System.exit(0);
    }

    static void setRunning(boolean status){
        Calibrator.run = status;
    }
}
