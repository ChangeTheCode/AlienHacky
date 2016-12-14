package at.fhv.alienserver;

import at.fhv.alienserver.Common.AccelerationContainer;
import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Kick_Container;
import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.game.board;
import at.fhv.alienserver.movingHead.MHControl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;

import static java.lang.Thread.sleep;

/**
 * Main. The name says it all
 *
 * @author tri7484
 * @version 5.12.2016
 */
public class Main {
    public static void main(String[] args) throws Exception {
        boolean calcTest = false;
        boolean MHTest = false;
        boolean game_board_test =true;

        //noinspection ConstantConditions
        if(calcTest) {
            Calculator myCalc =new Calculator();
            myCalc.init_Calculator(new CoordinateContainer(1.5, -1), new CoordinateContainer(1.5,1),
                    new CoordinateContainer(-1.5,-1),new CoordinateContainer(-1.5,1));

            /*
            Points in time:
            0.5
            0.5 + 0.9 = 1.4
            0.5 + 0.9 + 0.7 = 2.1
            0.5 + 0.9 + 0.7 + 1.0 = 3.1
             */

            myCalc.kick( new Kick_Container(0, new AccelerationContainer(27.27,14.16) ) );
            CoordinateContainer test = myCalc.get_position(10);
            test= myCalc.get_position(500);

            myCalc.kick( new Kick_Container(500, new AccelerationContainer(0, -1.5)) );
            test = myCalc.get_position(1400);

            myCalc.kick( new Kick_Container(1400, new AccelerationContainer(-2.5, 0)) );
            test = myCalc.get_position(2100);

            myCalc.kick( new Kick_Container( 2100, new AccelerationContainer(1.5, -1) ) );
            test = myCalc.get_position(3100);

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

        if(game_board_test) {
            board game_board = board.getInstance(0, 0, 2.4, 2.4, 3, 3);
            game_board.start();
            //game_board.set_speed_kick_value(new Kick_Container(1222222, new AccelerationContainer(1, 1)), 1222222);


            double x;
            double y;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while(true){

                System.out.print("Gebe 2 zahlen ein als Schuss! x : y ");
                input = in.readLine();
                String[] number = input.split(":");


                for (int i = 0; i< number.length; i++){
                    System.out.print(number[i]);
                }
                x = Integer.parseInt(number[0]);
                y = Integer.parseInt(number[1]);
                game_board.set_speed_kick_value( new Kick_Container( System.currentTimeMillis(), new AccelerationContainer(x,y)));

            }

        }

    }
}
