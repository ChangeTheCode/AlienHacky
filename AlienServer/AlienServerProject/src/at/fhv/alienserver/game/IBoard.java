package at.fhv.alienserver.game;

import at.fhv.alienserver.common.CoordinateContainer;
import at.fhv.alienserver.common.KickContainer;

import java.util.ArrayList;

/**
 * Created by Jim on 09.12.2016.
 */
public interface IBoard {

    public static Board getInstance(double start_point_x, double start_point_y , double left, double right, double top, double bottom){
        return null;
    }

    public ArrayList<CoordinateContainer> get_corner_coordinate();

    public void set_speed_kick_value (KickContainer new_kick);

    public KickContainer get_latest_kick();

}