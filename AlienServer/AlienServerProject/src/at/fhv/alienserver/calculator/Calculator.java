package at.fhv.alienserver.calculator;

import at.fhv.alienserver.AccelerationContainer;
import at.fhv.alienserver.CoordinateContainer;
import at.fhv.alienserver.SpeedContainer;
import at.fhv.alienserver.Tuple;
import at.fhv.alienserver.movingHead.MHControl;
import at.fhv.alienserver.sockcomm.SockComm;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

import static at.fhv.alienserver.Main.GLOBAL_SIM_ZERO_TIME;
import static at.fhv.alienserver.Main.mhThread;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;

//import static at.fhv.alienserver.Main.LOGGER;

//TODO: Add support for java-thread-interrupts to allow for kicks to happen, or think of another way of how to implement these

/**
 * Class implementing the ball flight simulation for the AlienHacky Project.
 * <p>
 * The class itself implements "Runnable" so it is supposed to be poured into a Thread object. The simulation itself
 * is based on a state space representation of a point mass (used as our hacky sack) and a forward euler solver. This
 * solver was chosen because it is very simple and needs to know very little data in order to properly function.
 *
 * @author tri7484
 * @version 1.11.2016
  */
public class Calculator implements Runnable {
    public boolean clearedList = false;

    /*
     * The following vars give the parameters for the calculation in state space. Please note that as of now, these
     * values are not meant to be perfect (or let alone final), they merely serve as a placeholder until testing
     * with the physical unit can be picked up.
     */

    /**
     * Change of first derivative of state variable depending on the state variable itself
     */
    private final double A = -1.7;
    /**
     * Change of the first derivative of state variable depending on input
     */
    private final double b = 1.5;
    /**
     * Change of output depending on state variable
     */
    private final double c = 1;
    /**
     * Change of output depending directly on the input
     */
    private final double d = 0;
    /**
     * Step width of simulation in seconds
     */
    private final double h = 0.01;

    private SockComm sock;
    private MHControl mhControl;

    private int stubIteration = 0;
    private int iteration = 0;

    private BlockingQueue<Tuple<CoordinateContainer, Long>> positionValuesDump;
    private Tuple<CoordinateContainer, Long>[] copyOfQueue;

    public Calculator(SockComm suppliedSock, MHControl mhControl, BlockingQueue<Tuple<CoordinateContainer, Long>> positionValuesDump){
        sock = suppliedSock;
        this.positionValuesDump = positionValuesDump;
        this.mhControl = mhControl;
    }

    private AccelerationContainer stub_getSenAcc(AccelerationContainer arr){
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
        if(stubIteration < 40){
            arr.x = 0;
            arr.y = 0;
            //arr.z = 0;
            stubIteration ++;
            return arr;
        } else if(stubIteration < 90){
            arr.x = 5;
            arr.y = 2;
            //arr.z = 4;
            stubIteration++;
            return arr;
        } else {
            arr.x = 0;
            arr.y = -4;
            //arr.z = -9.81;
            stubIteration++;
            return arr;
        }

    }

    public void run(){
        System.out.println("Calc started @:" + System.currentTimeMillis());
        System.out.flush();

        long currentTime;
        CoordinateContainer coordinateBuffer;

        PrintWriter writer;
        PrintWriter writer2;
        PrintWriter writer3;
        try {
            writer = new PrintWriter("calcOutput.txt", "UTF-8");
            writer2 = new PrintWriter("plotVals.csv", "UTF-8");
            writer3 = new PrintWriter("FinalQueue.txt", "UTF-8");
        } catch (Exception e){
            return;
        }

        CoordinateContainer pos = new CoordinateContainer();
        SpeedContainer speed = new SpeedContainer();
        AccelerationContainer acc = new AccelerationContainer();

        Tuple<AccelerationContainer, Long> senAcc = new Tuple<>(new AccelerationContainer(), 0L);
        AccelerationContainer oldSenAcc;

        Long currentSimulationTime = GLOBAL_SIM_ZERO_TIME;

        while(true){

            oldSenAcc = new AccelerationContainer( senAcc.a );
            senAcc = sock.getSenAcc();

            if(delta(senAcc.a, oldSenAcc, 0.3)){ /*TODO: This part here works with empiric value; test and verify*/
                /*
                 * Here we check if the accelerations delivered by the sensor have changed significantly. If so we have
                 * a kick event (or something with similar effects on the system). This means we have to trash the
                 * calculated values in positionValuesDump which no longer apply (remember we've been calculating into
                 * the future) and restart calculations from there.
                 */
                mhControl.pause();
                System.out.println("Trimming List");
                System.out.flush();
                clearedList = true;
                currentSimulationTime = GLOBAL_SIM_ZERO_TIME;
                currentTime = System.currentTimeMillis();
                for(Tuple<CoordinateContainer, Long> element : positionValuesDump){
                    if(element.b > currentTime){
                        positionValuesDump.remove(element);
                    } else {
                        if(element.b > currentSimulationTime){
                            currentSimulationTime = element.b;
                        }
                    }
                }
                mhControl.resume();
            }

            /*
             * we can always increase the simulation time by 10. If the queue was not trimmed (i.e. normal operation)
             * we simply go to the next step. In case the queue was trimmed, we determined the highest valid time value
             * in the for(...) loop above; however we are going the calculate the next value AFTER that one now.
             */
            currentSimulationTime += 10;


            //Maybe we have to use this to stop the sack when it gets kicked???
            /*
            if(senAcc[0] != 0 || senAcc[1] != 0 || senAcc[2] != -9.81){
                        acc[0] = 0;
                        acc[1] = 0;
                        //acc[2] = 0;
                        speed[0] = 0;
                        speed[1] = 0;
                        //speed[2] = 0;
             }
             */

            acc.x = signum(acc.x) * signum(A) * sqrt(A * A * speed.x * speed.x) + b * senAcc.a.x;
            acc.y = signum(acc.y) * signum(A) * sqrt(A * A * speed.y * speed.y) + b * senAcc.a.y;
            //acc.z = signum(acc.z) * signum(A) * sqrt(A * A * speed.z * speed.z) + b * senAcc.z;

            speed.x = speed.x + acc.x * h;
            speed.y = speed.y + acc.y * h;
            //speed.z = speed.z + acc.z * h;

            pos.x = pos.x + c * speed.x * h + d * senAcc.a.x;
            pos.y = pos.y + c * speed.y * h + d * senAcc.a.y;
            //pos.z = pos.z + c * speed.z * h + d * senAcc.z;

            if(iteration % 2 == 0) {
                writer.println("Iteration #" + iteration + "@ Simtime = " + (GLOBAL_SIM_ZERO_TIME + (iteration * 10)) );
                if(clearedList){
                    writer.println("List was trimmed");
                    clearedList = false;
                }
                writer.println("PosX = " + pos.x + "\tPosY = " + pos.y /*+ "\tPosZ = " + pos.z*/);
                writer.println("SpeedX = " + speed.x + "\tSpeedY = " + speed.y /*+ "\tSpeedZ = " + speed.z*/);
                writer.println("AccX = " + acc.x + "\tAccY = " + acc.y /*+ "\tAccZ = " + acc.z*/);
                writer.println("SenAccX = " + senAcc.a.x + "\tSenAccY = " + senAcc.a.y /*+ "\tSenAccZ = " + senAcc.z*/);
                writer.println("---------------------------------");

                writer2.println(iteration + ";" + pos.x);
            }

            iteration++;

            try {
                positionValuesDump.put(  new Tuple<>(pos, currentSimulationTime)  );
                //LOGGER.log(Level.INFO, "Appended a new position {0}", pos.toString());
            } catch (InterruptedException e){
                System.err.println("Our calculator thread got interrupted, which clearly wasn't supposed to happen :-(");
                e.printStackTrace();
            }

            if((iteration % 1000) == 0){
                System.out.println("Iteration# " + iteration);
                System.out.flush();
            }

            if (iteration > 15000){
                writer.flush();
                writer.close();
                writer2.flush();
                writer2.close();

                System.out.println("calculator finished");
                //LOGGER.log(Level.INFO, "Exiting calculator due to max number of iterations reached");
                //break;
                mhThread.stop();
                int i = 0;
                for(Tuple<CoordinateContainer, Long> tup : positionValuesDump){
                    writer3.println("Element# " + i);
                    writer3.println("PosX = " + tup.a.x + "\tPosY = " + tup.a.y);
                    writer3.println("Time = " + tup.b);
                    i++;
                }
                writer3.flush();
                writer3.close();

                System.exit(0);
            }

//            if(iteration > 40000 || pos.z < 0){
//                writer.flush();
//                writer.close();
//                writer2.flush();
//                writer2.close();
//                break;
//            }
        }
    }

    private boolean delta(AccelerationContainer acc1, AccelerationContainer acc2, double threshold){
        /*|| Math.abs(acc1.z - acc2.z) > threshold*/
        return Math.abs(acc1.x - acc2.x) > threshold || Math.abs(acc1.y - acc2.y) > threshold;
    }
}
