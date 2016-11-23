package at.fhv.alienserver.sockcomm;

import at.fhv.alienserver.AccelerationContainer;

/**
 * Class for communication with AlienSock
 *
 * @author tri7484
 * @version 31.10.2016
 */

//TODO: Just implement anything here!!!

public class SockComm implements Runnable {
    public SockComm(){
        //Nothing of interest here ;-)
    }

    //getSenAcc --> get-sensor-acceleration-values
    public AccelerationContainer getSenAcc(){
        //TODO: magically acquire the current values of acceleration imposed on the hacky sack and return them
        return new AccelerationContainer();
    }

    public void run(){
        //this is the place for magic to happen :-)
    }
}
