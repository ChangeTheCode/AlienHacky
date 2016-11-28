package at.fhv.alienserver.sockcomm;

import at.fhv.alienserver.AccelerationContainer;
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

        /*
         * in case we have tough luck with timing, some acceleration values can get "overlooked".
         * This happens because the calc sometimes gets (and remains more or less) blocked when the queue is full and
         * the acceleration is only present for 100 ms in our example. So if these time windows fall together the
         * acceleration value is lost.
         * FIXME: Fix timing issue leading to loss of acc values (see comment above)
         */
        if(deltaTime < 100){
            return new Tuple<>(new AccelerationContainer(1, 0.3), GLOBAL_SIM_ZERO_TIME + deltaTime);
        } else if(deltaTime < 2000){
            return new Tuple<>(new AccelerationContainer(), GLOBAL_SIM_ZERO_TIME + deltaTime);
        } else if(deltaTime < 2100){
            return new Tuple<>(new AccelerationContainer(0, -1), GLOBAL_SIM_ZERO_TIME + deltaTime);
        } else if (deltaTime < 3000){
            return new Tuple<>(new AccelerationContainer(), GLOBAL_SIM_ZERO_TIME + deltaTime);
        } else if (deltaTime < 3100){
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
        System.out.println("SockComm started @:" + System.currentTimeMillis());
        System.out.flush();
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
