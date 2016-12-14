package at.fhv.alienserver.calculator;

import at.fhv.alienserver.Common.*;

import java.awt.*;
import java.util.ArrayDeque;
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
     * The following vars give the parameters for the calculation in state space. Please keep in mind that as of now,
     * these values are not meant to be perfect (or let alone final), they merely serve as a placeholder until testing
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
     * Step width of simulation in seconds; don't make it too large ( > 0.05) as this could make the solver
     * numerically unstable. For those who don't know what that means: you don't want that happening!
     */
    private final double h = 0.01;

    private CoordinateContainer tl; // tl = top left
    private CoordinateContainer tr; // tr = top right
    private CoordinateContainer bl; // bl = bottom left
    private CoordinateContainer br; // br = bottom right

    private ArrayList< LongTuple<CoordinateContainer, SpeedContainer, AccelerationContainer, Long> > calcValues;
    //private ArrayDeque< LongTuple<CoordinateContainer, SpeedContainer, AccelerationContainer, Long> > calcValues;
    private Polygon playField;

    public Calculator(){
        calcValues = new ArrayList<>(0);
        calcValues.add( new LongTuple<>(new CoordinateContainer(0,0), new SpeedContainer(0,0),
                new AccelerationContainer(0,0), 0L) );

        playField = new Polygon();
    }

    /*
     * Used to create this function
     * http://stackoverflow.com/questions/15620590/polygons-with-double-coordinates
     * http://stackoverflow.com/questions/15958434/how-to-check-if-a-point-is-inside-a-polygon
     */

    private void calculate(AccelerationContainer senAcc) {
        /*
         * Used to create this function
         * http://stackoverflow.com/questions/15620590/polygons-with-double-coordinates
         * http://stackoverflow.com/questions/15958434/how-to-check-if-a-point-is-inside-a-polygon
         */
        /*
        TODO: In the special case of zero acc the calculate func here never terminates because we're always within the playing area
        Maybe think of a way we can handle this; e.g. plausibility check and ret false if it can't terminate
         */

        CoordinateContainer pos = calcValues.get( calcValues.size() - 1 ).getA();
        SpeedContainer speed = calcValues.get( calcValues.size() - 1 ).getB();
        AccelerationContainer acc = calcValues.get( calcValues.size() - 1 ).getC();
        AccelerationContainer locSenAcc = new AccelerationContainer(senAcc);

        long currentSimulationTime = calcValues.get( calcValues.size() - 1 ).getD();
        int iteration = 0;

        /*
         * During testing, the most accurate simulation results have been achieved, when the
         * acceleration from the alienSock was having an effect for 0.12 seconds. Hence we
         * reproduce this behaviour in the below code by setting the setting the acc value
         * from the sock to 0|0 after 0.12 seconds or respectively 0.12 / h iterations.
         */

        while (isInPlayfield(pos)) {
            acc.setX( signum(acc.getX()) * signum(A) * sqrt(A * A * speed.getX() * speed.getX()) + b * locSenAcc.getX() );
            acc.setY( signum(acc.getY()) * signum(A) * sqrt(A * A * speed.getY() * speed.getY()) + b * locSenAcc.getY() );
            //acc.z = signum(acc.z) * signum(A) * sqrt(A * A * speed.z * speed.z) + b * senAcc.z;

            speed.setX( speed.getX() + acc.getX() * h );
            speed.setY( speed.getY() + acc.getY() * h );
            //speed.z = speed.z + acc.z * h;

            pos.setX( pos.getX() + c * speed.getX() * h + d * locSenAcc.getX() );
            pos.setY( pos.getY() + c * speed.getY() * h + d * locSenAcc.getY() );
            //pos.z = pos.z + c * speed.z * h + d * senAcc.z;

            currentSimulationTime += (1000 * h);

            calcValues.add(new LongTuple<>(new CoordinateContainer(pos), new SpeedContainer(speed),
                    new AccelerationContainer(acc), currentSimulationTime) );

            if( iteration == (int)(0.14 / h) ){
                locSenAcc = new AccelerationContainer(0,0);
            }

//            writer.println("Iteration #" + iteration + "@ Simtime = " + currentSimulationTime);
//
//            writer.println("PosX = " + pos.getX() + "\tPosY = " + pos.getX() /*+ "\tPosZ = " + pos.z*/);
//            writer.println("SpeedX = " + speed.getY() + "\tSpeedY = " + speed.getY() /*+ "\tSpeedZ = " + speed.z*/);
//            writer.println("AccX = " + acc.getX() + "\tAccY = " + acc.getY() /*+ "\tAccZ = " + acc.z*/);
//            writer.println("SenAccX = " + senAcc.getX() + "\tSenAccY = " + senAcc.getY() /*+ "\tSenAccZ = " + senAcc.z*/);
//            writer.println("---------------------------------");
//
//            writer2.println(iteration + ";" + pos.getX());

            iteration++;

        }

    }

    private boolean delta_acc(AccelerationContainer acc1, AccelerationContainer acc2, double threshold){
        /*|| Math.abs(acc1.z - acc2.z) > threshold*/
        return Math.abs(acc1.getX() - acc2.getX()) > threshold || Math.abs(acc1.getY() - acc2.getY()) > threshold;
    }

    private boolean isInPlayfield(CoordinateContainer point){
        return playField.contains(point.getX()*1000, point.getY()*1000);
    }

    @Override
    public void init_Calculator(CoordinateContainer top_left, CoordinateContainer top_right,
                                CoordinateContainer bottom_left, CoordinateContainer bottom_right ) {
        this.tr = top_right;
        playField.addPoint( (int)(this.tr.getX() * 1000), (int)(this.tr.getY() * 1000) );

        this.tl = top_left;
        playField.addPoint( (int)(this.tl.getX() * 1000), (int)(this.tl.getY() * 1000) );

        this.bl = bottom_left;
        playField.addPoint( (int)(this.bl.getX() * 1000), (int)(this.bl.getY() * 1000) );

        this.br = bottom_right;
        playField.addPoint( (int)(this.br.getX() * 1000), (int)(this.br.getY() * 1000) );

    }

    @Override
    public boolean kick(Kick_Container kick) {
        //the following code isn't exactly easy to read.
        //we both know it, but we're gonna have to live with it

        //Trim the internal ArrayList
        int i = 0;
        int j = calcValues.size() - 1;
        while(calcValues.get(i).getD() < kick.getTimestamp() && i < j){
            i++;
        }

        //Drain the values that are still valid to a new ArrayList
        calcValues = new ArrayList<>( calcValues.subList(0, i+1) );

        //Reset the calculation and let it run
        //Fixme: Make use of the kick timestamp or think how it can be integrated in the calcValues
        calculate(kick.getKick_direction_speed() );

        return true;
    }

    @Override
    public CoordinateContainer get_position(long timestamp) {
        int i = 0;
        while (i < calcValues.size()){
            if(calcValues.get(i).getD() >= timestamp){
                break;
            }
            i++;
        }
        try{
            return calcValues.get(i).getA();
        } catch (IndexOutOfBoundsException e){
            return null;
        }

    }
}
