package at.fhv.alienserver.calculator;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Kick_Container;

/**
 * Created by Jim on 09.12.2016.
 */
public interface ICalculator {

    public void init_Calculator(CoordinateContainer top_left, CoordinateContainer top_right,
                                CoordinateContainer bottom_left, CoordinateContainer bottom_right);

    public boolean kick(Kick_Container kick);


    public CoordinateContainer get_position(long timestamp);

}
