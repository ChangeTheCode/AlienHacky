package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.CoordinateContainer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 07.11.16.
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
        // Did we seriously just need 12 lines of code to have a key-listener!?!?

        System.out.println("Use the WASD keys to move the light spot");
        System.out.println("Use the number keys 1 - 4 to set the corner in the respective quadrant");
        System.out.println("Close input window to terminate application");
        System.out.println("If you don't know what quadrants are or what WASD controls are: just go home...");

        Character c;
        CoordinateContainer newCoordinates = new CoordinateContainer(0, 0, 0);

        while(run) {
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                if(c == null) {
                    continue;
                }
                else if (c.charValue() == 'w') {
                    newCoordinates.x += 0.1;
                    //coordinates.add(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c.charValue() == 's') {
                    newCoordinates.x -= 0.1;
                    //coordinates.add(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c.charValue() == 'd') {
                    newCoordinates.y += 0.1;
                    //coordinates.add(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if (c.charValue() == 'a') {
                    newCoordinates.y -= 0.1;
                    //coordinates.add(newCoordinates);
                    System.out.println("Pressed " + c);
                    c = null;
                } else if(c.charValue() == '1'){
                    System.out.println("Saved corner in quadrant 1");
                    c = null;
                } else if(c.charValue() == '2'){
                    System.out.println("Saved corner in quadrant 2");
                    c = null;
                } else if(c.charValue() == '3'){
                    System.out.println("Saved corner in quadrant 3");
                    c = null;
                } else if(c.charValue() == '4'){
                    System.out.println("Saved corner in quadrant 4");
                    c = null;
                }
            } catch (InterruptedException e) {
                System.out.println("Exception occurred :-O");
                //TODO: Implement exception handling
            }
        }
        /*
         * TODO: Fix race condition! The message from the AlienWindowListener is sometimes not displayed because
         * there's a race here!
         */
        System.exit(0);
    }

    static void setRunning(boolean status){
        Calibrator.run = status;
    }
}
