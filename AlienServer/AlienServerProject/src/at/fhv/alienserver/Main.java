package at.fhv.alienserver;

import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.esp.ESP;
import at.fhv.alienserver.sockcomm.SockComm;

import java.util.concurrent.ArrayBlockingQueue;

public class Main {

    public static void main(String[] args) {
        ArrayBlockingQueue<CoordinateContainer> pointContainer = new ArrayBlockingQueue<CoordinateContainer>(50);

        SockComm mySock = new SockComm();
        Calculator myCalc = new Calculator(mySock, pointContainer);
        ESP myESP = new ESP(pointContainer);

        Thread sockThread = new Thread(mySock);
        Thread calcThread = new Thread(myCalc);


        sockThread.start();
        calcThread.start();

        try {
            sockThread.join();
            calcThread.join();
        } catch (InterruptedException e){
            System.out.println("Thread(s) got interrupted: " + e);
            e.printStackTrace();
            //In this very specific case we're screwed anyways so we can just kill our threads and exit the AlienServer

            //noinspection deprecation
            sockThread.stop();
            //noinspection deprecation
            calcThread.stop();
        }

    }
}
