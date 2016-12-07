package at.fhv.alienserver.game;

import at.fhv.alienserver.CoordinateContainer;
import sun.plugin.dom.core.CoreConstants;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Jim on 07.12.2016.
 */
public class board {

    private double _left_border;
    private double _top_border;
    private double _buttom_border;
    private double _right_border;

    private CoordinateContainer _start_point =null;

    private CoordinateContainer _speed_kick_value;
    private boolean _kick_speed_is_new = false;

    private static board ourInstance = new board();

    public static board getInstance() {
        return ourInstance;
    }
    //singleton
    private board() {
        getInstance();
        this._start_point = new CoordinateContainer(0.0,0.0, "start_position");
        this._speed_kick_value = new CoordinateContainer(-9999,-9999,"failed");
    }

    private board(double zero_point_x, double zero_point_y , double left, double right, double top, double bottom) {

        getInstance();

        this._start_point = new CoordinateContainer(zero_point_x, zero_point_y);
        this._buttom_border = bottom;
        this._top_border = top;
        this._right_border = right;
        this._left_border = left;
        this._speed_kick_value = new CoordinateContainer(-9999,-9999,"failed");
    }

    public CoordinateContainer get_start_point(){
        return this._start_point;
    }


    public ArrayList<CoordinateContainer> get_corner_coordinate(){
        ArrayList<CoordinateContainer> eges = new ArrayList<CoordinateContainer>();

        eges.add(new CoordinateContainer(this._left_border, this._top_border, "left_top_corner"));
        eges.add(new CoordinateContainer(this._left_border, this._buttom_border, "left_bottom_corner"));
        eges.add(new CoordinateContainer(this._right_border, this._top_border, "right_top_corner"));
        eges.add(new CoordinateContainer(this._right_border, this._buttom_border, "right_bottom_corner"));

        return eges;
    }


    //diese funktion soll vom Server aufgerufen werden.
    public void set_speed_kick_value (double speed_x, double speed_y, double speed_z){
        if(this._speed_kick_value != null){
            _speed_kick_value.x = speed_x;
            _speed_kick_value.y = speed_y;
        }else {
            this._speed_kick_value = new CoordinateContainer(speed_x, speed_y);
        }
        _kick_speed_is_new = true;
    }

    public CoordinateContainer get_speed_kick_value(){
        if(_speed_kick_value != null) {
            return this._speed_kick_value;
        }
        return new CoordinateContainer(-9999,-9999,"failed");
    }



    public void run(){
        CoordinateContainer _speed_kick = new CoordinateContainer(_speed_kick_value);

        while(true){
            if(_kick_speed_is_new){
                _speed_kick = new CoordinateContainer(_speed_kick_value);
                this._speed_kick_value.x = -9999;
                this._speed_kick_value.x = -9999;

                _kick_speed_is_new =false;
                //Todo call calculate points
            }

            // time einbauen und dann aufruf um Dmx calls zumachen !!


        }
    }

}
