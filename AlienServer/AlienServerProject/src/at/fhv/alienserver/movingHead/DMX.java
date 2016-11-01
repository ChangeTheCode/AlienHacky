package at.fhv.alienserver.movingHead;

/*
 * DMX Class
 *
 * @author	Ursus Schneider
 * @version	24.10.2016
 *
 */
public class DMX {

    public byte pan;
    public byte tilt;
    public byte fine_pan;
    public byte fine_tilt;
    public byte speed_pan_tilt;
    public byte color;
    public byte shutter;
    public byte dimmer;
    public byte gobo_wheel;
    public byte gobo_rotation;
    public byte special_functions;
    public byte build_in_functions;
    public int size;

    // constructor
    DMX () {
        //TODO: Check these first two values
        this.pan = (byte)270; //Set pointing straight to the ground
        this.tilt = (byte)135; //Set pointing straight to the ground
        this.fine_pan = 0;
        this.fine_tilt = 0;
        this.speed_pan_tilt = 0;
        this.color = 39; //red, TODO: Change to pink
        this.shutter = (byte)218; //on
        this.dimmer = (byte)255;
        this.gobo_wheel = 7; // open
        this.gobo_rotation = 0; // fixed position
        this.special_functions = 16; // no blackout
        this.build_in_functions = 0; // no not in use
        this.size = 12;
    }

    //TODO: Revise this function
    public byte getPan() {
        return pan;
    }

    public void setPan(double pan) {
        this.pan = (byte)(255.0 / 540.0 * pan);
    }

    //TODO: Revise this function
    public byte getTilt() {
        return tilt;
    }

    public void setTilt(double tilt) {
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
