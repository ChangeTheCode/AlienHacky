package at.fhv.alienserver.calibrators;

import at.fhv.alienserver.config.Config;
import at.fhv.alienserver.movingHead.MHControl;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by thomas on 13.12.16.
 */
public class MHCalibrator {

    private static boolean run = true;

    public static void main(String[] args) throws IOException{
        ArrayBlockingQueue<Character> characterQueue = new ArrayBlockingQueue<Character>(1000);

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

        System.out.println("Use the 'W' and 'S' keys to adjust pan until the MH points down vertically");
        System.out.println("Use the 'A' and 'D' keys to adjust tilt until the MH points down vertically");
        System.out.println("Press q finish calibrating pan and tilt");

        Config config = new Config();
        Character c;

        //First loop: set up angles to point mh down vertically
        while(true){
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if(c == null){
                    //Currently there's no character present, so just don't do anything
                } else if(c == 'w'){

                } else if(c == 's'){

                } else if(c == 'a'){

                } else if(c == 'd'){

                } else if(c == 'q'){
                    break;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occured :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }

        //Second loop: compare known angle to measured distance to find out the heigth of the mh
        while(true){

        }
    }

    public static void setRunning(boolean status){
        MHCalibrator.run = status;
    }

}
