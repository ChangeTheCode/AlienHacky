package at.fhv.alienserver.calculator;

import at.fhv.alienserver.Common.AccelerationContainer;
import at.fhv.alienserver.Common.CoordinateContainer;

/**
 * Created by Jim on 09.12.2016.
 */
public interface ICalculator {

    public void init_Calculator(CoordinateContainer top_left, CoordinateContainer top_right,
                                CoordinateContainer bottom_left, CoordinateContainer bottom_right);

    //Fixme: why does kick return a CoordinateContainer? Isn't it supposed to reinitialise the calculation in the background?
    //Fixme: why was kick supplied with a CoordinateContainer?
    public CoordinateContainer kick(long timestamp, AccelerationContainer kick );


    public CoordinateContainer get_position(long timestamp);

}
