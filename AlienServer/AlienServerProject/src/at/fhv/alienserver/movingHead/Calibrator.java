package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Tuple;
import at.fhv.alienserver.config.Config;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 07.11.16.
 * NICETOHAVE: Implement exception handling at character reading
 * NICETOHAVE: Make the system drop packets that come in while a key is being held down (to accomplish that the MH doesn't continue to move after the button has been released)
 */
public class Calibrator {

    private static boolean run = true;

    public static void main(String[] args){

        //A queue for holding the pressed keys
        ArrayBlockingQueue<Tuple<CoordinateContainer, Long>> coordinates = new ArrayBlockingQueue<>(1);

        //Get MH - Control up and running
        /*MHControl myMH = null;
        try {
            myMH = new MHControl(coordinates, 1);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
        Thread mhThread = new Thread(myMH);
        mhThread.start();*/

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
        CoordinateContainer newCoordinates = new CoordinateContainer(0, 0);

        try {
            coordinates.put(new Tuple<>(new CoordinateContainer(0, 0), Long.MAX_VALUE));
        } catch(InterruptedException e){
            e.printStackTrace();
        }

        Config config = null;
        try {
            config = new Config();
        } catch(IOException e){
            System.out.println("Config utility working with \'AlienServer.properties\' file could not be loaded. Is the file there?");
            e.printStackTrace();
            System.exit(-1);
        }

        while(run) {
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if (c == null){
                    //Currently there's no character present, so just don't do anything
                } else if (c == 'w') {
                    newCoordinates.x += 0.1;
                    coordinates.put(new Tuple<>(newCoordinates, Long.MAX_VALUE));
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c == 's') {
                    newCoordinates.x -= 0.1;
                    coordinates.put(new Tuple<>(newCoordinates, Long.MAX_VALUE));
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c == 'd') {
                    newCoordinates.y += 0.1;
                    coordinates.put(new Tuple<>(newCoordinates, Long.MAX_VALUE));
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c == 'a') {
                    newCoordinates.y -= 0.1;
                    coordinates.put(new Tuple<>(newCoordinates, Long.MAX_VALUE));
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c == 'r'){
                    String outputString = config.getProperty(Config.AlienServerProperties.quadrant1Limit);
                    System.out.println("Tried to read a property; got: ");
                    System.out.println(outputString);
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
            } catch (IOException e){
                System.out.println("File AlienServer.properties not found. Was it moved or renamed");
                e.printStackTrace();
            }
        }
        /*
         * NOTE: this call to exit() can cause a race condition with the open input window and the listeners. In the
         * current state of the project, this race condition does not matter; should calibration problems occur in the
         * future, this might be the problem.
         */
        System.exit(0);
    }

    static void setRunning(boolean status){
        Calibrator.run = status;
    }
}
