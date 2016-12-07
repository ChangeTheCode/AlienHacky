package at.fhv.alienserver.calculator;

import at.fhv.alienserver.*;

import java.io.PrintWriter;
import java.util.ArrayList;

import static java.lang.Math.signum;
import static java.lang.Math.sqrt;

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
public class Calculator {
    /*
     * The following vars give the parameters for the calculation in state space. Please note that as of now, these
     * values are not meant to be perfect (or let alone final), they merely serve as a placeholder until testing
     * with the physical unit can be picked up.
     */

    /**
     * Change of first derivative of state variable depending on the state variable itself
     */
    //private final double A = -1.7;
    private final double A = -0;
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
     * Step width of simulation in seconds; don't make it too large ( > 0.05) as this could make the solver
     * numerically unstable. For those who don't know what that means: you don't want that happening!
     */
    private final double h = 0.01;

    public Calculator(){
    }

    public ArrayList<LongTuple<CoordinateContainer, SpeedContainer, AccelerationContainer, Long>> calculate(
            CoordinateContainer startPoint, SpeedContainer startSpeed, AccelerationContainer startAcc,
            AccelerationContainer senAcc, double seconds, long startTime)
    {

        PrintWriter writer;
        PrintWriter writer2;
        try {
            writer = new PrintWriter("./LogFiles/calcOutput_" + System.currentTimeMillis() + ".txt", "UTF-8");
            writer2 = new PrintWriter("./LogFiles/plotVals.csv_" + System.currentTimeMillis() + ".txt", "UTF-8");
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        int iteration = 0;

        ArrayList< LongTuple<CoordinateContainer, SpeedContainer, AccelerationContainer, Long> > positions = new ArrayList<>();

        CoordinateContainer pos = new CoordinateContainer(startPoint);
        SpeedContainer speed = new SpeedContainer(startSpeed);
        AccelerationContainer acc = new AccelerationContainer(startAcc);

        long currentSimulationTime = startTime;
        int targetIterations = new Double(seconds/h).intValue();

        while (iteration < targetIterations) {
            acc.x = signum(acc.x) * signum(A) * sqrt(A * A * speed.x * speed.x) + b * senAcc.x;
            acc.y = signum(acc.y) * signum(A) * sqrt(A * A * speed.y * speed.y) + b * senAcc.y;
            //acc.z = signum(acc.z) * signum(A) * sqrt(A * A * speed.z * speed.z) + b * senAcc.z;

            speed.x = speed.x + acc.x * h;
            speed.y = speed.y + acc.y * h;
            //speed.z = speed.z + acc.z * h;

            pos.x = pos.x + c * speed.x * h + d * senAcc.x;
            pos.y = pos.y + c * speed.y * h + d * senAcc.y;
            //pos.z = pos.z + c * speed.z * h + d * senAcc.z;

            currentSimulationTime += (1000 * h);

            writer.println("Iteration #" + iteration + "@ Simtime = " + currentSimulationTime);

            positions.add(new LongTuple<>(new CoordinateContainer(pos), new SpeedContainer(speed),
                    new AccelerationContainer(acc), currentSimulationTime ) );
            writer.println("PosX = " + pos.x + "\tPosY = " + pos.y /*+ "\tPosZ = " + pos.z*/);
            writer.println("SpeedX = " + speed.x + "\tSpeedY = " + speed.y /*+ "\tSpeedZ = " + speed.z*/);
            writer.println("AccX = " + acc.x + "\tAccY = " + acc.y /*+ "\tAccZ = " + acc.z*/);
            writer.println("SenAccX = " + senAcc.x + "\tSenAccY = " + senAcc.y /*+ "\tSenAccZ = " + senAcc.z*/);
            writer.println("---------------------------------");

            writer2.println(iteration + ";" + pos.x);

            iteration++;

        }

        return positions;
    }

    private boolean delta_acc(AccelerationContainer acc1, AccelerationContainer acc2, double threshold){
        /*|| Math.abs(acc1.z - acc2.z) > threshold*/
        return Math.abs(acc1.x - acc2.x) > threshold || Math.abs(acc1.y - acc2.y) > threshold;
    }
}
