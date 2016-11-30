package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.CoordinateContainer;
import at.fhv.alienserver.Tuple;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

//import static at.fhv.alienserver.Main.LOGGER;
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
    private BlockingQueue<Tuple<CoordinateContainer, Long>> coordinatesFromCalculator;
    private Object[] localCopyOfCoordinatesArray;
    private LinkedList<CoordinateContainer> localCopyOfCoordinates;
    /**
     * Integer that determines for what queue size the MH - Control will wait to be available. This directly sets which
     * number of coordinates is actually set with the MH - X25 (e.g. every 20th position)
     */
    private int QUEUE_SIZE;

    private ESP esp = new ESP();
    //TODO: Move all of the following to config.file
    /**
     * Offset of the Pan - angle; chosen to have the MH-X25 point straight to the ground for point (0, 0)
     */
    private final double offset_pan = 270;
    //private final double offset_pan = 180;
    /**
     * Offset of the Tilt - angle; chosen to have the MH-X25 point straight to the ground for point (0, 0)
     */
    private final double offset_tilt = 135 + 90;
    //private final double offset_tilt = 135;
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
    private final int waitTimeConversionFactor = 300;

    /**
     * This boolean is used as a flag to signal the Runnable in this class, if it should be run or not.
     * <p>
     * The use of such a flag became necessary when it was obvious that sometimes during the execution of the AlienServer
     * position setting of the MH - X25 should pause and all available computation power should be spent on internal
     * data processing.
     */
    private boolean run = true;

    public MHControl(BlockingQueue<Tuple<CoordinateContainer, Long>> q, int QUEUE_SIZE) throws IOException {
        this.coordinatesFromCalculator = q;
        this.QUEUE_SIZE = QUEUE_SIZE;
        DMX packet = new DMX();
        packet.setPan(offset_pan);
        packet.setTilt(offset_tilt);
        esp.sendPackets(packet);
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
        bigger = (bigger < 1)? bigger + 1 : bigger;
        return (long)(Math.log(bigger)*waitTimeConversionFactor);
    }

    public void run(){
        CoordinateContainer c = new CoordinateContainer();
        CoordinateContainer oldC;

        LinkedList<DMX> packets = new LinkedList<>();
        //ArrayList<Long> waitTimes = new ArrayList<>();

        /*
         * Set up a DMX packet with default values and a dummy packet to send along as well. This dummy packet
         * is required due to the set up with a second MH-X25 in U325, which likely contains some kind of error though.
         */
        //DMX dummy = new DMX();
        DMX dmxPacket = new DMX();
        DMX oldDmxPacket;
        DMX exaggeratedDmxPacket;

        long currentTime = 0L;
        long sleepingTime = 0L;
        Tuple <CoordinateContainer, Long> localTupleBuffer = null; //Init with null needed for loop logic below

        double direction;
        double oldDirection;
        PrintWriter writer_consumed = null;
        PrintWriter writer_encountered = null;
        int iterationNumber = 0;
        try {
            writer_consumed = new PrintWriter("ConsumedCoordinates.txt", "UTF-8");
            writer_encountered = new PrintWriter("EncounteredCoordinates.txt", "UTF-8");
        } catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("MHControl started @:" + System.currentTimeMillis());
        System.out.flush();
        do{
            if(run) {
                packets.clear();
                //Reset here; nulling needed for logic reasons in loop below
                localTupleBuffer = null;

    //            coordinatesFromCalculator.drainTo(localCopyOfCoordinates, QUEUE_SIZE);
    //            localCopyOfCoordinatesArray = localCopyOfCoordinates.toArray();
    //
    //            /*
    //             * Grab the last point in the array and interpret it through the next statements.
    //             */
    //            oldC = new CoordinateContainer(c);
    //            c = (CoordinateContainer) localCopyOfCoordinatesArray[localCopyOfCoordinatesArray.length - 1];
    //            c.x += mhOffsetX;
    //            c.y += mhOffsetY;

                //Search the current position and grab the coordinates
                //The current solution is arguably not beautiful, but was used for reasons of time constraints
                //NOTE:Problem here???? --> As of latest knowledge: in MHControl.java the problem manifests itself between here and line 193
                currentTime = System.currentTimeMillis();
                do {
                    if(coordinatesFromCalculator.peek() != null){
                        //we have a value --> evaluate it below
                        localTupleBuffer = coordinatesFromCalculator.poll();
                        writer_encountered.println("Iteration# " + iterationNumber + "@ simTime = " + localTupleBuffer.b);
                        writer_encountered.println("With time delta = " + (localTupleBuffer.b - currentTime));
                        writer_encountered.println("X = " + localTupleBuffer.a.x + "\tY= " + localTupleBuffer.a.y);
                        writer_encountered.println("---------------------------------------------------------------");
                        writer_encountered.flush();
                        if(localTupleBuffer.b >= currentTime){
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        //peek == null
                        if(localTupleBuffer == null){
                            //we don't have any value yet, just keep trying
                            continue;
                        } else {
                            //we have no new value in queue, just a local one. Just use it and hope for the best
                            //LOGGER.log(Level.INFO, "Picked the last available packet to position MH @ {0}", localTupleBuffer.a.toString());
                            break;
                        }
                    }
                } while (true);

                oldC = new CoordinateContainer(c);
                c = localTupleBuffer.a;
                //System.out.println("In MHControl: Read X = " + localTupleBuffer.a.x + " Y = " + localTupleBuffer.a.y);
                //System.out.flush();
                //NOTE: In the next two lines, the debugger shows the absurdly big values for c.x; so the error has to be before this line!
                //c.x += mhOffsetX;
                c.x = c.x + (new Double(mhOffsetX)).longValue();
                //c.y += mhOffsetY;
                c.y = c.y + (new Double(mhOffsetY)).longValue();


                /*
                 * First we remember the old DMX - packet to later use it to determine the time we have to wait for the
                 * MH-X25 to complete its motion.
                 */
                oldDmxPacket = new DMX(dmxPacket);

                //NOTE: If the distance gets too far from the center (~100m) the MH "overspins" above horizontal position
                //NICETOHAVE: find out why and compensate; doesn't really matter though, as our distances aren't that great
                dmxPacket.setPan(Math.atan(c.x / h) * 180 / Math.PI + offset_pan);
                dmxPacket.setTilt(Math.atan(c.y / Math.sqrt(c.x * c.x + h * h)) * 180 / Math.PI + offset_tilt);

                packets.add(dmxPacket);

                /*
                 * At this place we use oldC and c to determine, how much the angle of movement has changed. If it has
                 * changed more than a certain threshold, we use an exaggerated-packet. This calculation can be set up
                 * using the DMX packets (with pan and tilt) themselves, however this is not done at the moment since the
                 * hardware setup is known to change in the future (as of 7.11.2016); once the final setup is used, we can
                 * change the calculation and optimise variable usage.
                 * NICETOHAVE: Change the calculation to used oldDmxPacket and dmxPacket.
                 */
                direction = Math.atan(c.y / c.x);
                oldDirection = Math.atan(oldC.y / oldC.x);
                if (abs(direction - oldDirection) > 10) {
                    //LOGGER.log(Level.INFO, "Using an exaggerated packet to get to {0}", c.toString());
                    exaggeratedDmxPacket = DMX.getExaggeratedDmx(dmxPacket, oldDmxPacket);
                    //Change lighting to make use of exaggerated packet visible
                    exaggeratedDmxPacket.shutter = (byte)0;
                    exaggeratedDmxPacket.dimmer = (byte)0;
                    packets.addFirst(exaggeratedDmxPacket);
                }

                //Set the moving head to the last point in our array
                try {
                    for (DMX packet : packets) {
                        esp.sendPackets(packet);
                        sleep(100);
                    }
                } catch (IOException e) {
                    System.err.println("Couldn't transmit ESP-packet for shit!");
                    System.err.println(e.toString());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Used to clear coordinates with the old architecture, now it's the calc's job. So don't do it here!
                //coordinatesFromCalculator.clear();

                try {
                    //long debug3 = getTimeToSleep(oldDmxPacket, dmxPacket);
                    //waitTimes.add(debug3);
                    //LOGGER.log(Level.INFO, "Sleeping");
                    sleepingTime = getTimeToSleep(oldDmxPacket, dmxPacket);
                    sleep(sleepingTime);
                } catch (Exception e) {
                    //Man just let me test :-(
                    e.printStackTrace();
                }

                //Write log file
                writer_consumed.println("Iteration# " + iterationNumber + " @ wallClockTime = " + currentTime);
                writer_consumed.println("@ simTime = " + localTupleBuffer.b +" with time delta = " + (localTupleBuffer.b - currentTime));
                writer_consumed.println("Resulted sleeping time = " + sleepingTime);
                writer_consumed.println("X (from c) = " + c.x + "\tY (from c) = " + c.y);
                writer_consumed.println("X = " + localTupleBuffer.a.x + "\tY= " + localTupleBuffer.a.y);
                writer_consumed.println("---------------------------------------------------------------");
                writer_consumed.flush();

                iterationNumber++;
            } else {
                yield();
            }
        } while (true); //Just let it run for now
    }

    public void pause(){
        this.run = false;
    }

    public void resume(){
        this.run = true;
    }
}
