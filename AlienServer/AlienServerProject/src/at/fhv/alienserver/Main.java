package at.fhv.alienserver;

import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.movingHead.MHControl;
import at.fhv.alienserver.sockcomm.SockComm;
import sun.rmi.runtime.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class holding the main routine.
 * <p>
 * The main routine instantiates the other necessary components of the AlienServer, assigns Thread-Objects to them,
 * starts these and then waits until they all exit.
 *
 * @author tri7484
 * @version 1.11.2016
 */
public class Main {
    /**
     * A constant used to hold the number of positions stored in the according container before the calculator-thread
     * gets blocked again.
     * <p>
     * Number is saved in this awkward way because the author couldn't find a replacement for the good old #define.
     */
    public static final int POS_CONTAINER_SIZE = 20;
    public static long GLOBAL_SIM_ZERO_TIME = 0;
    //public final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException{
        /*Handler[] handlers = LOGGER.getHandlers();
        for(Handler handler : handlers){
            LOGGER.removeHandler(handler);
        }
        Handler newHandler = new FileHandler("ServerLog");
        LOGGER.addHandler(newHandler);*/

        //ArrayBlockingQueue<CoordinateContainer> pointContainer = new ArrayBlockingQueue<>(10000);
        LinkedBlockingQueue< Tuple<CoordinateContainer, Long> > pointContainer = new LinkedBlockingQueue<>(10000);
        //LOGGER.log(Level.INFO, "Instantiating utility classes");
        SockComm mySock = new SockComm();
        MHControl mhControl = new MHControl(pointContainer, POS_CONTAINER_SIZE);
        Calculator myCalc = new Calculator(mySock, mhControl, pointContainer);

        //LOGGER.log(Level.INFO, "Instantiating utility class threads");
        Thread sockThread = new Thread(mySock);
        Thread calcThread = new Thread(myCalc);
        Thread mhThread = new Thread(mhControl);

        sockThread.start();
        GLOBAL_SIM_ZERO_TIME = System.currentTimeMillis();
        calcThread.start();
        mhThread.start();

        try {
            //LOGGER.log(Level.INFO, "Joining threads");
            sockThread.join();
            calcThread.join();
            mhThread.join();
        } catch (InterruptedException e){
            System.out.println("Thread(s) got interrupted: " + e);
            e.printStackTrace();
            /*
             * Yes, I know that using the "stop()" call is considered deprecated but in this very specific case the
             * threads are definitely not meant to be interrupted so we're screwed anyways
             * and hence we can just kill our threads and exit the AlienServer
             */

            //noinspection deprecation
            sockThread.stop();
            //noinspection deprecation
            calcThread.stop();
            //noinspection deprecation
            mhThread.stop();
        }

    }
}
