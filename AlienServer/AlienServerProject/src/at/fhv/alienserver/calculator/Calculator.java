package at.fhv.alienserver.calculator;

import at.fhv.alienserver.Common.AccelerationContainer;
import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.LongTuple;
import at.fhv.alienserver.Common.SpeedContainer;

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
public class Calculator implements ICalculator{
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

    private CoordinateContainer tl; // tl = top left
    private CoordinateContainer tr; // tr = top right
    private CoordinateContainer bl; // bl = bottom left
    private CoordinateContainer br; // br = bottom right

    public Calculator(){
        //empty object
    }

    private Calculator(CoordinateContainer tl, CoordinateContainer tr, CoordinateContainer bl, CoordinateContainer br){
        this.tl = tl;
        this.tr = tr;
        this.bl = bl;
        this.br = br;

        /*
         * Looka here:
         * http://stackoverflow.com/questions/15620590/polygons-with-double-coordinates
         * http://stackoverflow.com/questions/15958434/how-to-check-if-a-point-is-inside-a-polygon
         */
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
            acc.setX( signum(acc.getX()) * signum(A) * sqrt(A * A * speed.getX() * speed.getX()) + b * senAcc.getX() );
            acc.setY( signum(acc.getY()) * signum(A) * sqrt(A * A * speed.getY() * speed.getY()) + b * senAcc.getY() );
            //acc.z = signum(acc.z) * signum(A) * sqrt(A * A * speed.z * speed.z) + b * senAcc.z;

            speed.setX( speed.getX() + acc.getX() * h );
            speed.setY( speed.getY() + acc.getY() * h );
            //speed.z = speed.z + acc.z * h;

            pos.setX( pos.getX() + c * speed.getX() * h + d * senAcc.getX() );
            pos.setY( pos.getY() + c * speed.getY() * h + d * senAcc.getY() );
            //pos.z = pos.z + c * speed.z * h + d * senAcc.z;

            currentSimulationTime += (1000 * h);

            writer.println("Iteration #" + iteration + "@ Simtime = " + currentSimulationTime);

            positions.add(new LongTuple<>(new CoordinateContainer(pos), new SpeedContainer(speed),
                    new AccelerationContainer(acc), currentSimulationTime ) );
            writer.println("PosX = " + pos.getX() + "\tPosY = " + pos.getX() /*+ "\tPosZ = " + pos.z*/);
            writer.println("SpeedX = " + speed.getY() + "\tSpeedY = " + speed.getY() /*+ "\tSpeedZ = " + speed.z*/);
            writer.println("AccX = " + acc.getX() + "\tAccY = " + acc.getY() /*+ "\tAccZ = " + acc.z*/);
            writer.println("SenAccX = " + senAcc.getX() + "\tSenAccY = " + senAcc.getY() /*+ "\tSenAccZ = " + senAcc.z*/);
            writer.println("---------------------------------");

            writer2.println(iteration + ";" + pos.getX());

            iteration++;

        }

        return positions;
    }

    private boolean delta_acc(AccelerationContainer acc1, AccelerationContainer acc2, double threshold){
        /*|| Math.abs(acc1.z - acc2.z) > threshold*/
        return Math.abs(acc1.getX() - acc2.getX()) > threshold || Math.abs(acc1.getY() - acc2.getY()) > threshold;
    }

    @Override
    public void init_Calculator(CoordinateContainer top_left, CoordinateContainer top_right, CoordinateContainer bottom_left, CoordinateContainer bottom_right) {
        this.tr = top_right;
        this.tl = top_left;
        this.bl = bottom_left;
        this.br = bottom_right;
    }

    @Override
    public boolean kick(long timestamp, CoordinateContainer kick) {

        return false;
    }

    @Override
    public CoordinateContainer get_position(long timestamp) {

        return null;
    }
}
