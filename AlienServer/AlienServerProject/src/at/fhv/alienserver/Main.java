package at.fhv.alienserver;

import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.sockcomm.SockComm;

public class Main {

    public static void main(String[] args) {
        SockComm mySock = new SockComm();
        Calculator myCalc = new Calculator(mySock);

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
