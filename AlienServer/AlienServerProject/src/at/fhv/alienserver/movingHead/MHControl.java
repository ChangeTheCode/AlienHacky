package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Tuple;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.sleep;

/**
 * This class wraps the controls for the MH-X25 moving head.
 *
 * @author tri7484
 * @version 1.11.2016
 */
public class MHControl {
    private BlockingQueue<Tuple<CoordinateContainer, Long>> coordinatesFromCalculator;
    /**
     * Integer that determines for what queue size the MH - Control will wait to be available. This directly sets which
     * number of coordinates is actually set with the MH - X25 (e.g. every 20th position)
     */
    private ESP esp = new ESP();

    //FIXME: Move all of the following to config.file
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
     * A coordinate container used to store the last position that we had to drive to. Needed to determine the direction
     * if the caller wishes to have an exaggerated packet.
     */
    private CoordinateContainer oldPosition;

    private double exaggerationFactor;

    public double getExaggerationFactor() {
        return exaggerationFactor;
    }

    public void setExaggerationFactor(double exaggerationFactor) {
        this.exaggerationFactor = exaggerationFactor;
    }

    public MHControl(double exaggerationFactor) throws IOException {
        this.exaggerationFactor = exaggerationFactor;
        DMX packet = new DMX();
        //The following three lines set the MH-X25 to position (0,0)
        packet.setPan(offset_pan);
        packet.setTilt(offset_tilt);
        esp.sendPackets(packet);
    }

    public void setPosition(CoordinateContainer position, boolean exaggerate){
        LinkedList<DMX> packets = new LinkedList<>();

        DMX dmxPacket = new DMX();
        DMX exaggeratedDmxPacket;

        position.x = position.x + mhOffsetX;
        position.y = position.y + mhOffsetY;

        dmxPacket.setPan(Math.atan(position.x / h) * 180 / Math.PI + offset_pan);
        dmxPacket.setTilt(Math.atan(position.y / Math.sqrt(position.x * position.x + h * h)) * 180 / Math.PI + offset_tilt);

        packets.add(dmxPacket);

        if(exaggerate) {
            exaggeratedDmxPacket = getExaggeratedPacket(position, oldPosition, this.exaggerationFactor);
            //Change lighting to make use of exaggerated packet visible
            exaggeratedDmxPacket.shutter = (byte) 0;
            exaggeratedDmxPacket.dimmer = (byte) 0;
            packets.addFirst(exaggeratedDmxPacket);
        }

        oldPosition = new CoordinateContainer(position);

        try {
            for (DMX packet : packets) {
                esp.sendPackets(packet);
                sleep(20);
            }
        } catch (IOException e) {
            System.err.println("Couldn't transmit ESP-packet for shit!");
            System.err.println(e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private DMX getExaggeratedPacket(CoordinateContainer pos, CoordinateContainer oldPos, double factor){
        DMX packet = new DMX();
        CoordinateContainer tempC = new CoordinateContainer(pos);

        double deltaX = pos.x - oldPos.x;
        double deltaY = pos.y - oldPos.y;

        deltaX *= factor;
        deltaY *= factor;

        tempC.x += deltaX;
        tempC.y += deltaY;

        packet.setPan(Math.atan(tempC.x / h) * 180 / Math.PI + offset_pan);
        packet.setTilt(Math.atan(tempC.y / Math.sqrt(tempC.x * tempC.x + h * h)) * 180 / Math.PI + offset_tilt);

        return packet;
    }

}
