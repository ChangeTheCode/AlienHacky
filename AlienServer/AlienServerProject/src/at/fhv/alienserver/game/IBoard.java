package at.fhv.alienserver.game;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Kick_Container;

import java.util.ArrayList;

/**
 * Created by Jim on 09.12.2016.
 */
public interface IBoard {

    public static board getInstance(double start_point_x, double start_point_y , double left, double right, double top, double bottom){
        return null;
    }

    public ArrayList<CoordinateContainer> get_corner_coordinate();

    public void set_speed_kick_value (Kick_Container new_kick);

    public Kick_Container get_latest_kick();

}