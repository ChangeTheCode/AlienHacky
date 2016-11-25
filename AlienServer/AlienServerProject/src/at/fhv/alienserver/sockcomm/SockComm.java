package at.fhv.alienserver.sockcomm;

import at.fhv.alienserver.AccelerationContainer;
import at.fhv.alienserver.CoordinateContainer;
import at.fhv.alienserver.Tuple;

import static at.fhv.alienserver.Main.GLOBAL_SIM_ZERO_TIME;

/**
 * Class for communication with AlienSock
 *
 * @author tri7484
 * @version 25.11.2016
 */


public class SockComm implements Runnable {
    public SockComm(){
        //Nothing of interest here ;-)
    }

    //getSenAcc --> get-sensor-acceleration-values
    public Tuple<AccelerationContainer, Long> getSenAcc(){
        Long deltaTime = System.currentTimeMillis() - GLOBAL_SIM_ZERO_TIME;

        if(deltaTime < 2){
            return new Tuple<>(new AccelerationContainer(1, 1), GLOBAL_SIM_ZERO_TIME + deltaTime);
        } else if (deltaTime < 3){
            return new Tuple<>(new AccelerationContainer(0, -2), GLOBAL_SIM_ZERO_TIME + deltaTime);
        } else if (deltaTime < 4){
            return new Tuple<>(new AccelerationContainer(-1, 0), GLOBAL_SIM_ZERO_TIME + deltaTime);
        } else {
            return new Tuple<>(new AccelerationContainer(), GLOBAL_SIM_ZERO_TIME + deltaTime);
        }

    }

    public void run(){
        /*
        The run() function lends itself to take the current acceleration value (if a kick happened, else just
        use (0, 0)) and store it in an internal container, until the calculator queries it.
         */
        int dummy = 0;
        int dubbel = 0;
        //Just random bullshit to keep the compiler from whining too much
        while(true){
            if(dubbel < 100 && dubbel >= 0){
                dubbel++;
                dummy++;
            } else if(dubbel > 100){
                dubbel--;
                dummy--;
            } else {
                //wtf just happened
            }
        }

    }
}
