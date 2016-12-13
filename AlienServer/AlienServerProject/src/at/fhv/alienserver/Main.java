package at.fhv.alienserver;

import at.fhv.alienserver.Common.AccelerationContainer;
import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Kick_Container;
import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.movingHead.MHControl;
import static java.lang.Thread.sleep;

/**
 * Main. The name says it all
 *
 * @author tri7484
 * @version 5.12.2016
 */
public class Main {
    public static void main(String[] args) throws Exception {
        boolean calcTest = true;
        boolean MHTest = false;

        //noinspection ConstantConditions
        if(calcTest) {
            Calculator myCalc =new Calculator();
            myCalc.init_Calculator(new CoordinateContainer(1.5, -1), new CoordinateContainer(1.5,1),
                    new CoordinateContainer(-1.5,-1),new CoordinateContainer(-1.5,1));

            myCalc.kick( new Kick_Container(25, new AccelerationContainer(1,1) ) );
            CoordinateContainer test = myCalc.get_position(70);
            System.out.println(test.toString());
        }

        //noinspection ConstantConditions
        if(MHTest){
            long sleepieTime = 1000;
            MHControl head = new MHControl(2, false, false);
            CoordinateContainer testPosition;

            //noinspection InfiniteLoopStatement
            while(true) {
                testPosition = new CoordinateContainer(0, 0);
                head.move_to(testPosition, true);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(1, 0);
                head.move_to(testPosition, true);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(0, 1);
                head.move_to(testPosition, true);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(-1, 0);
                head.move_to(testPosition, true);
                sleep(sleepieTime);

                testPosition = new CoordinateContainer(0, -1);
                head.move_to(testPosition, true);
                sleep(sleepieTime);
            }
        }
    }
}
