package at.fhv.alienserver;

import at.fhv.alienserver.Common.AccelerationContainer;
import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.LongTuple;
import at.fhv.alienserver.Common.SpeedContainer;
import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.movingHead.MHControl;

import java.io.PrintWriter;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * @author tri7484
 * @version 5.12.2016
 */
public class Main {
    public static void main(String[] args) throws Exception {
        boolean calcTest = true;
        boolean MHTest = false;

        if(calcTest) {
            //test
            Calculator myCalc =new Calculator();
            myCalc.init_Calculator(new CoordinateContainer(), new CoordinateContainer(),new CoordinateContainer(),new CoordinateContainer());

            double fulltime1 = 0.5;
            double fulltime2 = 0.9;
            double fulltime3 = 0.7;
            double fulltime4 = 1.0;
            double accelTime = 0.12;

            PrintWriter logger = new PrintWriter("plotVals.csv", "UTF-8");

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

            myList = myCalc.calculate(
                    tempTuple.getA(), tempTuple.getB(), tempTuple.getC(),
                    new AccelerationContainer(0, 0)
                    , fulltime1 - accelTime, tempTuple.getD());

            tempTuple = myList.get(myList.size() - 1);

            System.out.println("After kick 1!");
            System.out.println("Position = " + tempTuple.getA().toString());
            System.out.println("Speed = " + tempTuple.getB().toString());
            System.out.println("Acceleration = " + tempTuple.getC().toString());
            System.out.println("Final timestamp = " + tempTuple.getD() + "\n\n");
            logger.println(tempTuple.getA().getX() + ";" + tempTuple.getA().getY());

            ///////////////////////////////////////////////////////

            myList = myCalc.calculate(
                    tempTuple.getA(), new SpeedContainer(0, 0), new AccelerationContainer(0, 0),
                    new AccelerationContainer(0, -1.5),
                    accelTime, tempTuple.getD());

            tempTuple = myList.get(myList.size() - 1);

            myList = myCalc.calculate(
                    tempTuple.getA(), tempTuple.getB(), tempTuple.getC(),
                    new AccelerationContainer(0, 0)
                    , fulltime2 - accelTime, tempTuple.getD());

            tempTuple = myList.get(myList.size() - 1);

            System.out.println("After kick 2!");
            System.out.println("Position = " + tempTuple.getA().toString());
            System.out.println("Speed = " + tempTuple.getB().toString());
            System.out.println("Acceleration = " + tempTuple.getC().toString());
            System.out.println("Final timestamp = " + tempTuple.getD() + "\n\n");
            logger.println(tempTuple.getA().getX() + ";" + tempTuple.getA().getY());

            ///////////////////////////////////////////////////////

            myList = myCalc.calculate(
                    tempTuple.getA(), new SpeedContainer(0, 0), new AccelerationContainer(0, 0),
                    new AccelerationContainer(-2.5, 0),
                    accelTime, tempTuple.getD());

            tempTuple = myList.get(myList.size() - 1);

            myList = myCalc.calculate(
                    tempTuple.getA(), tempTuple.getB(), tempTuple.getC(),
                    new AccelerationContainer(0, 0)
                    , fulltime3 - accelTime, tempTuple.getD());

            tempTuple = myList.get(myList.size() - 1);

            System.out.println("After kick 3!");
            System.out.println("Position = " + tempTuple.getA().toString());
            System.out.println("Speed = " + tempTuple.getB().toString());
            System.out.println("Acceleration = " + tempTuple.getC().toString());
            System.out.println("Final timestamp = " + tempTuple.getD() + "\n\n");
            logger.println(tempTuple.getA().getX() + ";" + tempTuple.getA().getY());

            ///////////////////////////////////////////////////////

            myList = myCalc.calculate(
                    tempTuple.getA(), new SpeedContainer(0, 0), new AccelerationContainer(0, 0),
                    new AccelerationContainer(1.5, -1),
                    accelTime, tempTuple.getD());

            tempTuple = myList.get(myList.size() - 1);

            myList = myCalc.calculate(
                    tempTuple.getA(), tempTuple.getB(), tempTuple.getC(),
                    new AccelerationContainer(0, 0)
                    , fulltime4 - accelTime, tempTuple.getD());

            tempTuple = myList.get(myList.size() - 1);

            System.out.println("After kick 4!");
            System.out.println("Position = " + tempTuple.getA().toString());
            System.out.println("Speed = " + tempTuple.getB().toString());
            System.out.println("Acceleration = " + tempTuple.getC().toString());
            System.out.println("Final timestamp = " + tempTuple.getD() + "\n\n");
            logger.println(tempTuple.getA().getX() + ";" + tempTuple.getA().getY());

            ///////////////////////////////////////////////////////

            logger.flush();
            logger.close();
        }

        if(MHTest){
            long sleepieTime = 1000;
            MHControl head = new MHControl(3, false, true);
            CoordinateContainer testPosition;

            while(true) {
                testPosition = new CoordinateContainer(0, 0);
                head.setPosition(testPosition, false, false);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(1, 0);
                head.setPosition(testPosition, false, false);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(0, 1);
                head.setPosition(testPosition, false, false);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(-1, 0);
                head.setPosition(testPosition, false, false);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(0, -1);
                head.setPosition(testPosition, false, false);
                sleep(sleepieTime);
            }
        }
    }
}
