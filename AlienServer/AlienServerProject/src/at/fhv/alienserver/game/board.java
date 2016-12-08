package at.fhv.alienserver.game;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Kick_Container;

import java.util.ArrayList;

/**
 * Created by Jim on 07.12.2016.
 */
public class board implements Runnable{

    private static Thread _board_thread;

    private double _left_border;
    private double _top_border;
    private double _bottom_border;
    private double _right_border;

    private static Logic _game_rules;

    private CoordinateContainer _start_point =null;

    private CoordinateContainer _speed_kick_value;
    private boolean _kick_speed_is_new = false;

    private static board ourInstance = new board();

    public static board getInstance() {
        ourInstance._start_point = new CoordinateContainer(0.0,0.0, "start_position");
        ourInstance._speed_kick_value = new CoordinateContainer(-9999,-9999,"failed");

        ourInstance._start_point = new CoordinateContainer(0.0,0.0, "start_position");

        ourInstance._game_rules = null; // we can't config the rules without information about the gameboard

        return ourInstance;
    }

    public static board getInstance(double zero_point_x, double zero_point_y , double left, double right, double top, double bottom) {
        ourInstance._start_point = new CoordinateContainer(zero_point_x, zero_point_y);
        ourInstance._bottom_border = bottom;
        ourInstance._top_border = top;
        ourInstance._right_border = right;
        ourInstance._left_border = left;
        ourInstance._speed_kick_value = new CoordinateContainer(-9999,-9999,"failed");

        ourInstance._game_rules = Logic.getInstance();
        return ourInstance;
    }

    private board() {}

    public CoordinateContainer get_start_point(){
        return this._start_point;
    }

    public ArrayList<CoordinateContainer> get_corner_coordinate(){
        ArrayList<CoordinateContainer> eges = new ArrayList<CoordinateContainer>();

        eges.add(new CoordinateContainer(this._left_border, this._top_border, "left_top_corner"));
        eges.add(new CoordinateContainer(this._left_border, this._bottom_border, "left_bottom_corner"));
        eges.add(new CoordinateContainer(this._right_border, this._top_border, "right_top_corner"));
        eges.add(new CoordinateContainer(this._right_border, this._bottom_border, "right_bottom_corner"));

        return eges;
    }


    //diese funktion soll vom Server aufgerufen werden.
    public void set_speed_kick_value (Kick_Container new_kick, long start_time){
        CoordinateContainer coor_kick = new_kick.getKick_direction_speed();
        if(this._speed_kick_value != null){
            _speed_kick_value.x = coor_kick.x;
            _speed_kick_value.y = coor_kick.y;
        }else {
            this._speed_kick_value = new CoordinateContainer(coor_kick.x, coor_kick.y);
        }
        _kick_speed_is_new = true;
    }

    public CoordinateContainer get_speed_kick_value(){
        if(_speed_kick_value != null) {
            return this._speed_kick_value;
        }
        return new CoordinateContainer(-9999,-9999,"failed");
    }


    public void start () {
        System.out.println("Starting Board Thread" );
        if (_board_thread == null) {
            _board_thread = new Thread (this, "board");
            _board_thread.start ();
        }
    }

    public void run(){
        CoordinateContainer _speed_kick = new CoordinateContainer(_speed_kick_value);
        long last_move_time = 0;

        while(true){

            //order new coordinate form the calculator core.
            //TOdo last_move_coordinate = get_coordinate_of_time(System.currentTimeMillis());
            last_move_time = System.currentTimeMillis();

            while (last_move_time + 10 <= System.currentTimeMillis() || ! _kick_speed_is_new ){
                // check if there is a new kick event
                if(_kick_speed_is_new){
                    _speed_kick = new CoordinateContainer(_speed_kick_value);
                    this._speed_kick_value.x = -9999;
                    this._speed_kick_value.x = -9999;

                    _kick_speed_is_new =false;
                    //Todo call calculate points                            //means startpoint
                    // calculate( _speed_kick.start_time, _speed_kick.x, _speed_kick,y,_speed_kick,z, last_move_coordinate, geschwindigkeit? );
                    break; // jump out of the loop and start to get mh_control a new coordinate
                }

                // check, if you are still inside of the game board

            }

        }
    }

}
