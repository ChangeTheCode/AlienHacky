package at.fhv.alienserver;

import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.movingHead.MHControl;
import at.fhv.alienserver.sockcomm.SockComm;

import java.util.concurrent.ArrayBlockingQueue;


public class Main {
    //TODO: Check again if controls to the moving head happen in accordance to handwritten notes and the txt-file

    public static final int POS_CONTAINER_SIZE = 20;

    public static void main(String[] args) {
        ArrayBlockingQueue<CoordinateContainer> pointContainer = new ArrayBlockingQueue<>(POS_CONTAINER_SIZE);

        SockComm mySock = new SockComm();
        Calculator myCalc = new Calculator(mySock, pointContainer);
        MHControl mhControl = new MHControl(pointContainer);

        Thread sockThread = new Thread(mySock);
        Thread calcThread = new Thread(myCalc);
        Thread mhThread = new Thread(mhControl);

        sockThread.start();
        calcThread.start();
        mhThread.start();

        try {
            sockThread.join();
            calcThread.join();
            mhThread.join();
        } catch (InterruptedException e){
            System.out.println("Thread(s) got interrupted: " + e);
            e.printStackTrace();
            //In this very specific case we're screwed anyways so we can just kill our threads and exit the AlienServer

            //noinspection deprecation
            sockThread.stop();
            //noinspection deprecation
            calcThread.stop();
            //noinspection deprecation
            mhThread.stop();
        }

    }
}
