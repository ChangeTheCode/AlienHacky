package at.fhv.alienserver;

import at.fhv.alienserver.calculator.Calculator;

import java.util.ArrayList;

/**
 * @author tri7484
 * @version 5.12.2016
 */
public class Main {
    public static void main(String[] args) {

        Calculator myCalc = new Calculator();
        double fulltime = 1.28;
        double accelTime = 0.12;

        ArrayList<LongTuple<CoordinateContainer, SpeedContainer, AccelerationContainer, Long>> myList = myCalc.calculate(
                new CoordinateContainer(0, 0), new SpeedContainer(0, 0), new AccelerationContainer(0, 0),
                /*
                 * TODO: The acceleration values that we feed here are the ones from a sensor, however with x and y
                 * swapped and the signs swapped. The signs have to be swapped because of the mounting of the board
                 * on a shoe; x and y have to swapped because %% &/%$§/§(% i$%t "$&3)= ! (or in clear: i've got no clue!)
                 */
                new AccelerationContainer(27.27, 14.16)
                , accelTime, 0L
        );

        LongTuple<CoordinateContainer, SpeedContainer, AccelerationContainer, Long> tempTuple = myList.get(myList.size() - 1);

        System.out.println("Acceleration from foot ended!");
        System.out.println("Position = " + tempTuple.a.toString());
        System.out.println("Speed = " + tempTuple.b.toString());
        System.out.println("Acceleration = " + tempTuple.c.toString());
        System.out.println("Final timestamp = " + tempTuple.d + "\n\n");

        myList = myCalc.calculate(
                tempTuple.a, tempTuple.b, tempTuple.c,
                new AccelerationContainer(0,0)
                , fulltime - accelTime, tempTuple.d);

        tempTuple = myList.get(myList.size() - 1);

        System.out.println("Sim ended!");
        System.out.println("Position = " + tempTuple.a.toString());
        System.out.println("Speed = " + tempTuple.b.toString());
        System.out.println("Acceleration = " + tempTuple.c.toString());
        System.out.println("Final timestamp = " + tempTuple.d);
    }
}
