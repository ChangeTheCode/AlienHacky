package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.CoordinateContainer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static at.fhv.alienserver.Main.POS_CONTAINER_SIZE;

/**
 * Moving Head Control class
 *
 * @author tri7484
 * @version 31.10.2016
 */
public class MHControl implements Runnable{
    @SuppressWarnings("SpellCheckingInspection")
    private BlockingQueue<CoordinateContainer> coordsFromCalc;
    private CoordinateContainer[] localCoordCopy;

    private DMX dmxPacket;
    private ESP esp = new ESP();

    private final byte offset_phi = (byte)270;
    private final byte offset_theta = (byte)135;
    private final double h = 2.7; //Height of the moving head's mounting point in meters; TODO: Move to config.file
    private final double mhOffsetX = 2.5; //Offset of the moving head relative to center of game coordinate system in x - direction
    private final double mhOffsetY = 0; //Offset of the moving head relative to center of game coordinate system in y - direction

    public MHControl(BlockingQueue<CoordinateContainer> q){
        this.coordsFromCalc = q;
    }

    public void run(){
        CoordinateContainer c;

//        while(true) {
            //In the insanely unlikely case of this thread being faster than the calculator, we wait until the calculator
            //is ready; TODO: Find a smarter solution
            while(coordsFromCalc.size() < POS_CONTAINER_SIZE){
                //just do nothing
            }

            //Retrieve all coordinates the calculator provided us with
            localCoordCopy = (CoordinateContainer[]) coordsFromCalc.toArray();

            //Let the calculator run again
            coordsFromCalc.clear();

            //Set up a DMX packet with default values
            dmxPacket = new DMX();

            //Transform the last point in our array to MH - Angles according to the notes taken earlier
            c = localCoordCopy[localCoordCopy.length - 1];
            c.x += mhOffsetX;
            c.y += mhOffsetY;

            dmxPacket.pan = (byte) (Math.atan(c.y / c.x) + offset_phi);
            if(c.x >= 0){
                dmxPacket.tilt = (byte) (Math.atan(Math.sqrt(c.x * c.x + c.y * c.y) / h) + offset_theta);
            } else {
                dmxPacket.tilt = (byte) ( (-1) * Math.atan(Math.sqrt(c.x * c.x + c.y * c.y) / h) + offset_theta);
            }

            //Set the moving head to the last point in our array
            try {
                esp.sendPackets(dmxPacket);
            } catch (IOException e) {
                System.err.println("Couldn't transmit ESP-packet for shit!");
                System.err.println(e.toString());
                e.printStackTrace();
            }
//        }
    }
}
