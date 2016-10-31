package at.fhv.alienserver.calculator;

import static java.lang.Math.sqrt;

import at.fhv.alienserver.CoordinateContainer;
import at.fhv.alienserver.accelerationContainer;
import at.fhv.alienserver.sockcomm.SockComm;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

/**
 * Created by thomas on 18.10.16.
 */
public class Calculator implements Runnable {
    /*
     * The following vars give the parameters for the calculation in state space. Please note that as of now, these
     * values are not meant to be perfect (or let alone final), they merely serve as a placeholder until testing
     * with the physical unit can be picked up.
     */
    private final double A = -1.7; //Change of first derivative of state variable depending on the state variable itself
    private final double b = 1.5; //Change of the first derivative of state variable depending on input
    private final double c = 1; //Change of output depending on state variable
    private final double d = 0; //Change of output depending directly on the input
    private final double h = 0.01; //Step width of sim

    private SockComm sock;

    private int stubIteration = 0;
    private int iteration = 0;

    private BlockingQueue<CoordinateContainer> q;

    public Calculator(SockComm suppliedSock, BlockingQueue<CoordinateContainer> q){
        sock = suppliedSock;
        this.q = q;
    }

    private accelerationContainer getSenAcc(accelerationContainer arr){
        /*
        if(stubIteration < 80){
            arr[0] = 0;
            arr[1] = 0;
            arr[2] = -9.81;
            stubIteration++;
            return arr;
        } else if (stubIteration < 100){
            arr[0] = 3;
            arr[1] = 0;
            arr[2] = 3.5;
            stubIteration++;
            return arr;
        } else if(stubIteration < 140){
            arr[0] = 0;
            arr[1] = 0;
            arr[2] = -9.81;
            stubIteration++;
            return arr;
        } else if (stubIteration < 160){
            arr[0] = -3;
            arr[1] = 0;
            arr[2] = 5;
            stubIteration++;
            return arr;
        } else if (stubIteration >= 160){
            stubIteration = 0;
            arr[0] = 0;
            arr[1] = 0;
            arr[2] = -9.81;
            return arr;
        } else {
            return null;
        }
        */
        if(stubIteration < 50){
            arr.x = 5;
            arr.y = 0;
            arr.z = 4;
            stubIteration++;
            return arr;
        } else {
            arr.x = 0;
            arr.y = 0;
            arr.z = -9.81;
            stubIteration++;
            return arr;
        }
    }

    private int signum(double d){
        boolean bigger = 0 < d;
        boolean smaller = d < 0;
        if(!bigger && !smaller){
            return 0;
        } else if (bigger){
            return 1;
        } else{
            return -1;
        }
    }



    public void run(){
        PrintWriter writer;
        PrintWriter writer2;
        try {
            writer = new PrintWriter("calcOutput.txt", "UTF-8");
            writer2 = new PrintWriter("plotVals.csv", "UTF-8");
        } catch (Exception e){
            return;
        }
        //Arrays to hold acceleration, speed and position of the hacky sack
        double pos[] = new double[3];
        double speed[] = new double[3];
        double acc[] = new double[3];

        //Array to hold the acceleration values retrieved from the sock-unit
        accelerationContainer senAcc = new accelerationContainer();

        while(true){
            //senAcc = sock.getSenAcc(senAcc); TODO: Actually make this happen
            senAcc = getSenAcc(senAcc);

            //Maybe we have to use this to stop the sack when it gets kicked???
            /*
            if(senAcc[0] != 0 || senAcc[1] != 0 || senAcc[2] != -9.81){
                        acc[0] = 0;
                        acc[1] = 0;
                        acc[2] = 0;
                        speed[0] = 0;
                        speed[1] = 0;
                        speed[2] = 0;
             }
             */

            acc[0] = signum(acc[0]) * signum(A) * sqrt(A * A * speed[0] * speed[0]) + b * senAcc.x;
            acc[1] = signum(acc[1]) * signum(A) * sqrt(A * A * speed[1] * speed[1]) + b * senAcc.y;
            acc[2] = signum(acc[2]) * signum(A) * sqrt(A * A * speed[2] * speed[2]) + b * senAcc.z;

            speed[0] = speed[0] + acc[0] * h;
            speed[1] = speed[1] + acc[1] * h;
            speed[2] = speed[2] + acc[2] * h;

            pos[0] = pos[0] + c * speed[0] * h + d * senAcc.x;
            pos[1] = pos[1] + c * speed[1] * h + d * senAcc.y;
            pos[2] = pos[2] + c * speed[2] * h + d * senAcc.z;

            if(iteration % 2 == 0) {
                writer.println("Iteration #" + iteration);
                writer.println("Acc0 = " + acc[0] + "\tAcc1 = " + acc[1] + "\tAcc2 = " + acc[2]);
                writer.println("Speed0 = " + speed[0] + "\tSpeed1 = " + speed[1] + "\tspeed2 = " + speed[2]);
                writer.println("Pos0 = " + pos[0] + "\tPos1 = " + pos[1] + "\tPos2 = " + pos[2]);
                writer.println("SenAcc0 = " + senAcc.x + "\tSenAcc1 = " + senAcc.y + "\tSenAcc2 = " + senAcc.z);
                writer.println("---------------------------------");

                writer2.println(iteration + ";" + pos[0]);
            }

            iteration++;

            if(iteration > 40000 || pos[2] < 0){
                writer.flush();
                writer.close();
                writer2.flush();
                writer2.close();
                break;
            }
        }
    }
}
