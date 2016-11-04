package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.CoordinateContainer;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import static at.fhv.alienserver.Main.POS_CONTAINER_SIZE;
import static java.lang.Math.abs;
import static java.lang.Thread.sleep;
import static java.lang.Thread.yield;

/**
 * This class wraps the controls for the MH-X25 moving head.
 * <p>
 * It implements "Runnable" and hence is intended to be poured into a Thread object. When running it tries to retrieve
 * a sequence of cartesian coordinates from a BlockingQueue (hint: if it doesn't find enough, its doing nothing) and
 * sets the MH-X25 according to this sequence of points.
 *
 * @author tri7484
 * @version 1.11.2016
 */
public class MHControl implements Runnable{
    @SuppressWarnings("SpellCheckingInspection")
    private BlockingQueue<CoordinateContainer> coordsFromCalc;
    private Object[] localCopyOfCoordinates;

    private ESP esp = new ESP();
    //TODO: Move all of the following to config.file
    /**
     * Offset of the phi - angle (Pan); chosen to have the MH-X25 point straight to the ground for point (0, 0)
     */
    private final double offset_phi = 180;
    /**
     * Offset of the theta - angle (Tilt); chosen to have the MH-X25 point straight to the ground for point (0, 0)
     */
    private final double offset_theta = 135;
    /**
     * Height of the moving head's mounting point in meters; measured at the tilt-turning-axis
     */
    private final double h = 2.4;
    /**
     * Offset of the moving head relative to center of game coordinate system in x - direction
     */
    //private final double mhOffsetX = 1.35;
    private final double mhOffsetX = 0;
    /**
     * Offset of the moving head relative to center of game coordinate system in y - direction
     */
    private final double mhOffsetY = 0;
    /**
     * This factor determines the conversion between the distance the MH - X25's motors have to travel and the number
     * of milliseconds the thread described by this class should sleep (i.e. wait) to allow the MH - X25 to complete
     * the motion.
     * <p>
     * Please note: this value is derived empirically and absolutely dependent on your hardware setup!
     * NOTE: With the current setup in U325 (1.11.2016) a value of about 300 is where movement of the light point along
     * the floor just starts lagging a tiny little bit; so this should be pretty close to the ideal waiting time.
     */
    private final double waitTimeConversionFactor = 150;

    public MHControl(BlockingQueue<CoordinateContainer> q){
        this.coordsFromCalc = q;
    }

    /**
     * Function to calculate the time for which the control sleeps to allow the MH-X25 completing its motion
     * <p>
     * With its two parameters the function determines how far the MH-X25 has to move and sets up the returned time
     * accordingly. More precisely it looks at the changes of pan and tilt angles, determines which one is greater in
     * absolute numbers. It then takes the greater value, calculates the natural logarithm of it and multiplies it
     * with an empirically determined factor.
     * <p>
     * The use of a logarithm was chosen to give small changes a bigger weight compared to bigger changes. The reason
     * behind this is the fact that the MH-X25 moves faster and accelerates harder when it has a bigger distance to
     * travel. Hence we need proportionally more waiting time for smaller changes.
     *
     * @param oldPacket The packet used to set the MH-X25's position before the currently used one.
     * @param newPacket The packet currently in use to set the MH-X25's position
     * @return Waiting time in milliseconds in "long" data format.
     */
    private long getTimeToSleep(DMX oldPacket, DMX newPacket){
        double deltaPan = newPacket.pan - oldPacket.pan;
        double deltaTilt = newPacket.tilt - oldPacket.tilt;
        double bigger = (abs(deltaPan) > abs(deltaTilt))? abs(deltaPan) : abs(deltaTilt);
        return (long)(Math.log(bigger)*waitTimeConversionFactor);
    }

    public void run(){
        CoordinateContainer c;
        //ArrayList<Long> waitTimes = new ArrayList<>();

        /*
         * Set up a DMX packet with default values and a dummy packet to send along as well. This dummy packet
         * is required due to the set up with a second MH-X25 in U325, which likely contains some kind of error though.
         */
        DMX dummy = new DMX();
        DMX dmxPacket = new DMX();
        DMX oldDmxPacket;
        DMX exaggeratedDmxPacket;

        do{
            /*
             * In the insanely unlikely case of this thread being faster than the calculator, we wait until the
             * calculator is ready; We do this to set the MH-X25 to a precise interval of points.
             */
            while(coordsFromCalc.size() < POS_CONTAINER_SIZE){
                yield();
            }

            /*
              Retrieve all coordinates the calculator provided us with
              Using the "toArray()" call here 'cause it does NOT empty the container --> calculator stays blocked
              This enables us to do some other stuff before the calc runs again or change the order things
             */
            localCopyOfCoordinates = coordsFromCalc.toArray();

            /*
             * Grab the last point in the array and interpret it through the next statements.
             */
            c = (CoordinateContainer) localCopyOfCoordinates[localCopyOfCoordinates.length - 1];
            c.x += mhOffsetX;
            c.y += mhOffsetY;

            /*
             * First we remember the old DMX - packet to later use it to determine the time we have to wait for the
             * MH-X25 to complete its motion.
             */
            oldDmxPacket = new DMX(dmxPacket);

            //On first iteration the next statement yields 0 / 0 --> NaN!!!!
            //TODO: Fix that crap
            //double debug = Math.atan(c.y / c.x) * 180 / Math.PI + offset_phi;
            dmxPacket.setPan( Math.atan(c.y / c.x) * 180 / Math.PI + offset_phi );
            if(c.x >= 0){
                //double debug2 = Math.atan(Math.sqrt(c.x * c.x + c.y * c.y) / h) * 180 / Math.PI + offset_theta;
                dmxPacket.setTilt( Math.atan(Math.sqrt(c.x * c.x + c.y * c.y) / h) * 180 / Math.PI + offset_theta );
            } else {
                dmxPacket.setTilt((-1) * Math.atan(Math.sqrt(c.x * c.x + c.y * c.y) / h) * 180 / Math.PI + offset_theta);
            }
            exaggeratedDmxPacket = DMX.getExaggeratedDmx(dmxPacket, oldDmxPacket);

            //Set the moving head to the last point in our array
            try {
                esp.sendPackets(dummy, exaggeratedDmxPacket);
                sleep(100);
                esp.sendPackets(dummy, dmxPacket);
            } catch (IOException e) {
                System.err.println("Couldn't transmit ESP-packet for shit!");
                System.err.println(e.toString());
                e.printStackTrace();
            } catch (InterruptedException e){
                //We got interrupted!!! WTF?!?! This wasn't supposed to happen in our applicaiton
                e.printStackTrace();
            }

            //Let the calculator PURPOSEFULLY run again
            coordsFromCalc.clear();

            try {
                //long debug3 = getTimeToSleep(oldDmxPacket, dmxPacket);
                //waitTimes.add(debug3);
                sleep( getTimeToSleep(oldDmxPacket, dmxPacket) );
            } catch (Exception e){
                //Man just let me test :-(
            }
        } while (true); //Just let it run for now
    }
}
