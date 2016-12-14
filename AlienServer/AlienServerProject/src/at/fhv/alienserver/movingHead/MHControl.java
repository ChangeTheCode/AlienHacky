package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.moving_head_color;

import java.io.IOException;
import java.util.LinkedList;

import static java.lang.Math.abs;
import static java.lang.Thread.sleep;

/**
 * This class wraps the controls for the MH-X25 moving head.
 *
 * @author tri7484
 * @version 1.11.2016
 */
public class MHControl implements IMH_Controller{
    private ESP esp = new ESP();

    //FIXME: Implement reading of constants from config in ctor
    /**
     * Offset of the Pan - angle; chosen to have the MH-X25 point straight to the ground for point (0, 0)
     */
    private final double offset_pan = 95; //90 degrees plus a slight deviation due to non ideal mounting
    //private final double offset_pan = 180;
    /**
     * Offset of the Tilt - angle; chosen to have the MH-X25 point straight to the ground for point (0, 0)
     */
    private final double offset_tilt = 43;
    /**
     * Height of the moving head's mounting point in meters; measured at the tilt-turning-axis
     */
    private final double h = 2.57;
    /**
     * Offset of the moving head relative to center of game coordinate system in x - direction
     */
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

    /**
     * A DMX used to store the last used packet. Needed to determine the old angles if the caller wishes for self
     * triggered sleeping.
     */
    private DMX oldPacket;

    private double exaggerationFactor;
    private int xMirrorFactor;
    private int yMirrorFactor;

    public double getExaggerationFactor() {
        return exaggerationFactor;
    }

    public void setExaggerationFactor(double exaggerationFactor) {
        this.exaggerationFactor = exaggerationFactor;
    }

    public MHControl(double exaggerationFactor, boolean mirrorX, boolean mirrorY) throws IOException {
        this.exaggerationFactor = exaggerationFactor;
        if(mirrorX){
            xMirrorFactor = -1;
        } else {
            xMirrorFactor = 1;
        }

        if(mirrorY){
            yMirrorFactor = -1;
        } else {
            yMirrorFactor = 1;
        }

        DMX packet = new DMX();
        //The following three lines set the MH-X25 to position (0,0)
        packet.setPan(offset_pan);
        packet.setTilt(offset_tilt);

        oldPacket = new DMX(packet);
        oldPosition = new CoordinateContainer(0,0);

        esp.sendPackets(packet);
    }

    @Override
    public void move_to(CoordinateContainer position, boolean exaggerate) {
        _move_to_compendious(position, exaggerate, moving_head_color.PINK);
    }

    @Override
    public void move_to(CoordinateContainer position, boolean exaggerate, moving_head_color color) {
        _move_to_compendious(position, exaggerate, color);
    }

    private void _move_to_compendious(CoordinateContainer position, boolean exaggerate, moving_head_color color){
        LinkedList<DMX> packets = new LinkedList<>();

        DMX dmxPacket = new DMX();
        DMX exaggeratedDmxPacket;

        position.setX( position.getX() + mhOffsetX );
        position.setY( position.getY() + mhOffsetY );

        position.setX( position.getX() * xMirrorFactor );
        position.setY( position.getY() * yMirrorFactor );

        dmxPacket.setPan(Math.atan(position.getX() / h) * 180 / Math.PI + offset_pan);
        dmxPacket.setTilt(Math.atan(position.getY() / Math.sqrt(position.getX() * position.getX() + h * h)) * 180 / Math.PI + offset_tilt);

        //Resolve the supplied colour value to a numeric value that can be fed to the MH-X25
        dmxPacket.color = (byte) color.getValue();

        packets.add(dmxPacket);

        if(exaggerate) {
            exaggeratedDmxPacket = getExaggeratedPacket(position, oldPosition, this.exaggerationFactor);

            //Change lighting to make use of exaggerated packet visible; adds no functionality, just visualisation
            exaggeratedDmxPacket.shutter = (byte) 0;
            exaggeratedDmxPacket.dimmer = (byte) 0;

            packets.addFirst(exaggeratedDmxPacket);
        }

        oldPosition = new CoordinateContainer(position);
        oldPacket = new DMX(dmxPacket);

        try {
            for (DMX packet : packets) {
                esp.sendPackets(packet);
                if(packets.size() > 1) {
                    sleep(150);
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't transmit ESP-packet");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void set_light(Boolean on) {
        DMX packet = new DMX(oldPacket);

        if(on) {
            packet.shutter = (byte) 218; //on
            packet.dimmer = (byte) 255;
        } else {
            packet.shutter = (byte) 0; //off
            packet.dimmer = (byte) 0;
        }

        try {
            esp.sendPackets(packet);
        } catch(IOException e){
            System.err.println("Couldn't transmit ESP-packet");
            e.printStackTrace();
        }

        oldPacket = new DMX(packet);
    }

    private DMX getExaggeratedPacket(CoordinateContainer pos, CoordinateContainer oldPos, double factor){
        DMX packet = new DMX();
        CoordinateContainer tempC = new CoordinateContainer(pos);

        double deltaX = pos.getX() - oldPos.getX();
        double deltaY = pos.getY() - oldPos.getY();

        deltaX *= factor;
        deltaY *= factor;

        tempC.setX( tempC.getX() + deltaX );
        tempC.setY( tempC.getY() + deltaY );

        packet.setPan(Math.atan(tempC.getX() / h) * 180 / Math.PI + offset_pan);
        packet.setTilt(Math.atan(tempC.getY() / Math.sqrt(tempC.getX() * tempC.getX() + h * h)) * 180 / Math.PI + offset_tilt);

        return packet;
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
        int deltaPan = newPacket.pan - oldPacket.pan;
        int deltaTilt = newPacket.tilt - oldPacket.tilt;
        int bigger = (abs(deltaPan) > abs(deltaTilt))? abs(deltaPan) : abs(deltaTilt);
        bigger = (bigger < 1)? bigger + 1 : bigger;
        System.out.println("Bigger = " + bigger);
        return (long)(log2(bigger) * 350);
    }

    private int log2(int bits){
        /*
        Created with: http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers#3305710
         */
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }

    public double getCurrentPan(){
        DMX tempPacket = new DMX( oldPacket );
        return tempPacket.getPan();
    }

    public double getCurrentTilt(){
        DMX tempPacket = new DMX( oldPacket );
        return tempPacket.getTilt();
    }

    public double getCurrentX(){
        CoordinateContainer temp = new CoordinateContainer(oldPosition);
        return temp.getX();
    }

    public double getCurrentY(){
        CoordinateContainer temp = new CoordinateContainer(oldPosition);
        return temp.getY();
    }
}
