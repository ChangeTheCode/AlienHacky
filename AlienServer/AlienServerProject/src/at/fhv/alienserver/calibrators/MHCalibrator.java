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
 */
public class MHCalibrator{

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

        System.out.println("Use the 'W' and 'S' keys to adjust x Position until the MH points down vertically");
        System.out.println("Use the 'A' and 'D' keys to adjust y Position until the MH points down vertically");
        System.out.println("Press q finish this step");

        Config config = new Config();
        Character c;
        CoordinateContainer newCoordinates = new CoordinateContainer(0,0);

        //First loop: set up angles to point mh down vertically
        while(true){
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if(c == null){
                    //Currently there's no character present, so just don't do anything
                } else if(c == 'w'){
                    newCoordinates.setX( newCoordinates.getX() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'q'){
                    config.setProperty(Config.AlienServerProperties.mh_offset_pan, String.valueOf(mhc.getCurrentPan()));
                    config.setProperty(Config.AlienServerProperties.mh_offset_tilt, String.valueOf(mhc.getCurrentTilt()));
                    break;
                }else if(!run){
                    return;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }

        //Now destroy the MHControl and get a new one to read in the new values
        mhc = null;
        mhc = new MHControl(2.0, false, false);
        newCoordinates = null;
        newCoordinates = new CoordinateContainer();

        System.out.println("\nPlease move the light spot so that it is at a point with coordinates 1|0");
        System.out.println("Assuming the center of the coordinate system being right below the MH");
        System.out.println("Press q to finish this step");
        //Second loop: compare known angle to measured distance to find out the height of the mh
        while(true){
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if(c == null){
                    //Currently there's no character present, so just don't do anything
                } else if(c == 'w'){
                    newCoordinates.setX( newCoordinates.getX() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'q'){
                    /*
                     * Yes this calculation assumes that only the pan angle is used to move in x direction.
                     * This may not be universally true, but it is true for the current hardware setup in U325
                     * and we're running out of time.
                     */
                    config.setProperty(Config.AlienServerProperties.mh_height,
                            String.valueOf( newCoordinates.getX()/Math.tan(mhc.getCurrentPan()) ) );
                    break;
                }else if(!run){
                    return;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }

        newCoordinates = null;
        newCoordinates = new CoordinateContainer();

        System.out.println("Please move the light spot only in x direction so that it is at a global x value of 1");
        System.out.println("Press q to finish this step");
        //Third loop: use input distance to find offset in x direction
        while(true){
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if(c == null){
                    //Currently there's no character present, so just don't do anything
                } else if(c == 'w'){
                    newCoordinates.setX( newCoordinates.getX() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'q'){
                    /*
                     * Yes this calculation assumes that only the pan angle is used to move in x direction.
                     * This may not be universally true, but it is true for the current hardware setup in U325
                     * and we're running out of time.
                     */
                    config.setProperty(Config.AlienServerProperties.mh_offset_x, String.valueOf(newCoordinates.getX()));
                    break;
                }else if(!run){
                    return;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }


        newCoordinates = null;
        newCoordinates = new CoordinateContainer();
        System.out.println("Please move the light spot only in y direction so that it is at a global x value of 1");
        System.out.println("Press q to finish this step");
        //Fourth loop: use input distance to find offset in y direction
        while(true){
            try {
                c = characterQueue.poll(100, TimeUnit.MILLISECONDS);
                //noinspection StatementWithEmptyBody
                if(c == null){
                    //Currently there's no character present, so just don't do anything
                } else if(c == 'w'){
                    newCoordinates.setX( newCoordinates.getX() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.setPosition(new CoordinateContainer(newCoordinates), false, false);
                    c = null;
                } else if(c == 'q'){
                    /*
                     * Yes this calculation assumes that only the pan angle is used to move in x direction.
                     * This may not be universally true, but it is true for the current hardware setup in U325
                     * and we're running out of time.
                     */
                    config.setProperty(Config.AlienServerProperties.mh_offset_y, String.valueOf(newCoordinates.getY()));
                    break;
                } else if(!run){
                    return;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }
    }

    public static void setRunning(boolean status){
        MHCalibrator.run = status;
    }

}
