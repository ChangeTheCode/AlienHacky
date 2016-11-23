package at.fhv.alienserver.movingHead;

/**
 * DMX Class
 * <p>
 * This class primarily serves as a data container (similar to a C-Struct) inside its package. It holds all the DMX
 * parameters as byte variables (which are initialised in the ctor); it furthermore features a copy constructor and
 * a function that creates an "exaggerated" DMX packet to speed up the MH-X25's motion since it moves faster the faster
 * it should move.
 *
 * @author	Ursus Schneider
 * @version	1.11.2016
 *
 */
class DMX {

    byte pan;
    byte tilt;
    byte fine_pan;
    byte fine_tilt;
    byte speed_pan_tilt;
    byte color;
    byte shutter;
    byte dimmer;
    byte gobo_wheel;
    byte gobo_rotation;
    byte special_functions;
    byte build_in_functions;
    int size;

    // constructor
    DMX() {
        this.pan = 0; //Set pointing straight to the ground
        this.tilt = 0; //Set pointing straight to the ground
        this.fine_pan = 0;
        this.fine_tilt = 0;
        this.speed_pan_tilt = 0;
        this.color = 13; //pink ;-)
        this.shutter = (byte)218; //on
        this.dimmer = (byte)255;
        this.gobo_wheel = 7; // open
        this.gobo_rotation = 0; // fixed position
        this.special_functions = 16; // no blackout
        this.build_in_functions = 0; // no not in use
        this.size = 12;
    }

    DMX(DMX source){
        this.pan = source.pan;
        this.tilt = source.tilt;
        this.fine_pan = source.fine_pan;
        this.fine_tilt = source.fine_tilt;
        this.speed_pan_tilt = source.speed_pan_tilt;
        this.color = source.color;
        this.shutter = source.shutter;
        this.dimmer = source.shutter;
        this.gobo_wheel = source.gobo_wheel;
        this.gobo_rotation = source.gobo_rotation;
        this.special_functions = source.special_functions;
        this.build_in_functions = source.build_in_functions;
        this.size = source.size;
    }

    //TODO: Fix this function; after passing through it the debugger sometimes shows values of -1 for pan / tilt

    /**
     * Function to create a DMX packet with pan and tilt values set to max
     * <p>
     * This function uses its parameters to determine whether pan and tilt should increase or decrease and creates a
     * DMX instance with pan and tilt set to either maximum or minimum (depending in which direction they should move).
     * In other words: it "exaggerates" the motion to the limit. This helps to increase motion speed of the MH-X25
     * since it moves faster and accelerates harder if it has to move greater distances.
     * <p>
     * The additions or respectively subtractions of 1 at the size comparisons serve the suppression of numeric noise.
     *
     * @param source The source packet from which the new packet should be constructed.
     * @param sourcePredecessor The source packets predecessor; supplied to allow telling if pan / tilt should increase or decrease.
     * @return DMX instance with pan and tilt set to maximum or minimum as needed.
     */
    static DMX getExaggeratedDmx(DMX source, DMX sourcePredecessor){
        DMX newPacket = new DMX(source);

        //Max = 540; Min = 0
        if(source.pan > (sourcePredecessor.pan + 1)){
            newPacket.setPan(540);
        } else if(source.pan < (sourcePredecessor.pan - 1)){
            newPacket.setPan(0);
        }

        //Max = 270; Min = 0
        if(source.tilt > (sourcePredecessor.tilt + 1)){
            newPacket.setTilt(270);
        } else if(source.tilt < (sourcePredecessor.tilt - 1)){
            newPacket.setTilt(0);
        }

        return newPacket;
    }

    /**
     * This function returns the stored pan in degrees
     * <p>
     * Internally this function maps the numeric range of an unsigned byte (0 to 255) to possible range for the pan
     * angle(0 to 540).
     *
     * @return Stored pan in degrees
     */
    double getPan() {
        return this.pan / 255.0 * 540.0;
    }

    /**
     * This function stores the desired pan in a DMX packet / instance
     * <p>
     * Internally it maps the possible range of 0 to 540 degrees to the numeric range of an unsigned byte (0 to 255).
     *
     * @param pan The pan to be set in degrees; must be in range [0, 540]; if not unspecified things will occur.
     */
    void setPan(double pan) {
        this.pan = (byte)(255.0 / 540.0 * pan);
    }

    /**
     * This function returns the stored tilt in degrees
     * <p>
     * Internally this function maps the numeric range of an unsigned byte (0 to 255) to possible range for the tilt
     * angle(0 to 270).
     *
     * @return Stored tilt in degrees
     */
    double getTilt() {
        return this.tilt / 255.0 * 270.0;
    }

    /**
     * This function stores the desired tilt in a DMX packet / instance
     * <p>
     * Internally it maps the possible range of 0 to 270 degrees to the numeric range of an unsigned byte (0 to 255).
     *
     * @param tilt The tilt to be set in degrees; must be in range [0, 270]; if not unspecified things will occur.
     */
    void setTilt(double tilt) {
        this.tilt = (byte)(255.0 / 270.0 * tilt);
    }
//
//    public byte getFine_pan() {
//        return fine_pan;
//    }
//
//    public void setFine_pan(byte fine_pan) {
//        this.fine_pan = fine_pan;
//    }
//
//    public byte getFine_tilt() {
//        return fine_tilt;
//    }
//
//    public void setFine_tilt(byte fine_tilt) {
//        this.fine_tilt = fine_tilt;
//    }
//
//    public byte getSpeed_pan_tilt() {
//        return speed_pan_tilt;
//    }
//
//    public void setSpeed_pan_tilt(byte speed_pan_tilt) {
//        this.speed_pan_tilt = speed_pan_tilt;
//    }
//
//    public byte getColor() {
//        return color;
//    }
//
//    public void setColor(byte color) {
//        this.color = color;
//    }
//
//    public byte getShutter() {
//        return shutter;
//    }
//
//    public void setShutter(byte shutter) {
//        this.shutter = shutter;
//    }
//
//    public byte getDimmer() {
//        return dimmer;
//    }
//
//    public void setDimmer(byte dimmer) {
//        this.dimmer = dimmer;
//    }
//
//    public byte getGobo_wheel() {
//        return gobo_wheel;
//    }
//
//    public void setGobo_wheel(byte gobo_wheel) {
//        this.gobo_wheel = gobo_wheel;
//    }
//
//    public byte getGobo_rotation() {
//        return gobo_rotation;
//    }
//
//    public void setGobo_rotation(byte gobo_rotation) {
//        this.gobo_rotation = gobo_rotation;
//    }
//
//    public byte getSpecial_functions() {
//        return special_functions;
//    }
//
//    public void setSpecial_functions(byte special_functions) {
//        this.special_functions = special_functions;
//    }
//
//    public byte getBuild_in_functions() {
//        return build_in_functions;
//    }
//
//    public void setBuild_in_functions(byte build_in_functions) {
//        this.build_in_functions = build_in_functions;
//    }
//
//    public int getSize() {
//        return size;
//    }
//
//    public void setSize(int size) {
//        this.size = size;
//    }


}
