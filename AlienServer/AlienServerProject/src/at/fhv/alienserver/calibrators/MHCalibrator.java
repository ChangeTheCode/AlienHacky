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
 * Created by thomas on 13.12.16. Holds the method that calibrates the MH-X25
 * <p>
 * The MH is calibrated in four steps:
 * - First the head is calibrated so that it knows the offset values to point down vertically
 * - Second the light spot is moved to a specified distance by the user. Using the resulted angle of pan/tilt we can
 *  calculate the mounting height of the MH.
 * - Third the light spot is moved to a certain point by the user which is defined to have a global x - coordinate of 0.
 *  This is used know the MH's offset in x - direction respective to the logical center of the playing area.
 * - Fourth the previous step is repeated for the y - direction.
 */
class MHCalibrator implements ICalibrator{

    private boolean run = true;

    void execute() throws IOException{
        ArrayBlockingQueue<Character> characterQueue = new ArrayBlockingQueue<Character>(1000);

        MHControl mhc = new MHControl(2.0, false, false);

        //Get a keylistener up and running
        JTextArea inputText = new JTextArea();
        JFrame guiFrame = new JFrame();
        guiFrame.getContentPane().setBackground(Color.BLACK);
        guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        guiFrame.setTitle("Input for calibrators");
        guiFrame.setLocationRelativeTo(null);
        AlienWindowListener wListener = new AlienWindowListener(this);
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
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'q'){
                    String debug1 = String.valueOf(mhc.getCurrentPan());
                    config.setProperty(Config.AlienServerProperties.mh_offset_pan, debug1);
                    String debug2 = String.valueOf(mhc.getCurrentTilt());
                    config.setProperty(Config.AlienServerProperties.mh_offset_tilt, debug2);
                    break;
                } else {
                    c = null;
                }

                if(!run){
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
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
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
                } else {
                    c = null;
                }

                if(!run){
                    return;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }

        mhc = null;
        mhc = new MHControl(2.0, false, false);
        newCoordinates = null;
        newCoordinates = new CoordinateContainer();

        System.out.println("\nPlease move the light spot only in x direction so that it is at a global x value of 0");
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
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'q'){
                    /*
                     * Yes this calculation assumes that only the pan angle is used to move in x direction.
                     * This may not be universally true, but it is true for the current hardware setup in U325
                     * and we're running out of time.
                     */
                    config.setProperty(Config.AlienServerProperties.mh_offset_x, String.valueOf(newCoordinates.getX()));
                    break;
                } else {
                    c = null;
                }

                if(!run){
                    return;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }


        mhc = null;
        mhc = new MHControl(2.0, false, false);
        newCoordinates = null;
        newCoordinates = new CoordinateContainer();
        System.out.println("\nPlease move the light spot only in y direction so that it is at a global y value of 0");
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
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 's'){
                    newCoordinates.setX( newCoordinates.getX() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'a'){
                    newCoordinates.setY( newCoordinates.getY() + 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'd'){
                    newCoordinates.setY( newCoordinates.getY() - 0.01 );
                    mhc.move_to(new CoordinateContainer(newCoordinates), false);
                    c = null;
                } else if(c == 'q'){
                    /*
                     * Yes this calculation assumes that only the pan angle is used to move in x direction.
                     * This may not be universally true, but it is true for the current hardware setup in U325
                     * and we're running out of time.
                     */
                    config.setProperty(Config.AlienServerProperties.mh_offset_y, String.valueOf(newCoordinates.getY()));
                    break;
                } else {
                    c = null;
                }

                if(!run){
                    return;
                }
            } catch (InterruptedException e){
                System.out.println("Exception occurred :-O");
                e.printStackTrace();
                //TODO: Implement exception handling
            }
        }
        guiFrame.setVisible(false);
        guiFrame.dispose();
    }

    @Override
    public void setRunning(boolean status){
        this.run = status;
    }

}
