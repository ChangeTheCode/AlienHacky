package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.CoordinateContainer;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static at.fhv.alienserver.Main.POS_CONTAINER_SIZE;
import static java.lang.Thread.sleep;

/**
 * Moving Head Control class
 *
 * @author tri7484
 * @version 31.10.2016
 */
public class MHControl implements Runnable{
    @SuppressWarnings("SpellCheckingInspection")
    private BlockingQueue<CoordinateContainer> coordsFromCalc;
    private Object[] localCoordCopy;

    private DMX dmxPacket;
    private ESP esp = new ESP();

    private final double offset_phi = 180;
    private final double offset_theta = 135;
    private final double h = 2.4; //Height of the moving head's mounting point in meters; TODO: Move to config.file
    //TODO: Get this offset back to its real value of 1.35
    private final double mhOffsetX = 0; //Offset of the moving head relative to center of game coordinate system in x - direction
    private final double mhOffsetY = 0; //Offset of the moving head relative to center of game coordinate system in y - direction

    public MHControl(BlockingQueue<CoordinateContainer> q){
        this.coordsFromCalc = q;
    }

    public void run(){
        CoordinateContainer c = new CoordinateContainer();
        CoordinateContainer oldC = new CoordinateContainer();

        //This bullshit does not have a meaning! TODO: Remove again
        int testingDummy = 5;
        testingDummy++;

        do{
            /*
             * In the insanely unlikely case of this thread being faster than the calculator, we wait until the
             * calculator is ready; TODO: Find a smarter solution
             */
            while(coordsFromCalc.size() < POS_CONTAINER_SIZE){
                //just do nothing
            }

            /*
              Retrieve all coordinates the calculator provided us with
              Using the "toArray()" call here 'cause it does NOT empty the container --> calculator stays blocked
              This enables us to do some other stuff before the calc runs again or change the order things
             */
            localCoordCopy = coordsFromCalc.toArray();

            //
            /*
             * Set up a DMX packet with default values and a dummy packet to send along as well. This dummy packet
             * is required due to the set up with a second MH-X25 in U325, which likely contains some kind of error tho.
             */
            DMX dummy = new DMX();
            dmxPacket = new DMX();

            /*
             * First save the old position away and then grab the last point in the array in the array and interpret
             * it through the next statements.
             * The old position is remembered to enable us to calculate how far the MH has to move and therefor adjust
             * the waiting time at the end of the enclosing loop accordingly.
             */
            oldC = c;
            c = (CoordinateContainer)localCoordCopy[localCoordCopy.length - 1];
            c.x += mhOffsetX;
            c.y += mhOffsetY;

            //TODO: Implement exaggeration to make MH move faster
            double debug = Math.atan(c.y / c.x) * 180 / Math.PI + offset_phi;
            dmxPacket.setPan( debug );
            if(c.x >= 0){
                double debug2 = Math.atan(Math.sqrt(c.x * c.x + c.y * c.y) / h) * 180 / Math.PI + offset_theta;
                dmxPacket.setTilt(debug2);
            } else {
                dmxPacket.setTilt((-1) * Math.atan(Math.sqrt(c.x * c.x + c.y * c.y) / h) * 180 / Math.PI + offset_theta);
            }

            //Set the moving head to the last point in our array
            try {
                esp.sendPackets(dummy, dmxPacket);
            } catch (IOException e) {
                System.err.println("Couldn't transmit ESP-packet for shit!");
                System.err.println(e.toString());
                e.printStackTrace();
            }

            //Let the calculator PURPOSEFULLY run again
            coordsFromCalc.clear();

            /*
             * Wait to let the MH - X25 catch up with the software
             * TODO: Make this wait dependent on the distance to travel so as not to waste too much time
             */
            try {
                sleep(2500);
            } catch (Exception e){
                //Man just let me test :-(
            }
        } while (true); //Just let it run for now
    }
}
