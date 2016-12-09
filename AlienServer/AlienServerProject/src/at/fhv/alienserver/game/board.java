package at.fhv.alienserver.game;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.Coordinate_Name;
import at.fhv.alienserver.Common.Kick_Container;
import at.fhv.alienserver.Common.moving_head_color;

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

    private CoordinateContainer _start_point = null;

    private CoordinateContainer _next_kick_value;

    public boolean get_is_kick_speed_is_new() {
        return _kick_speed_is_new;
    }

    public void set_kick_speed_is_new(boolean _kick_speed_is_new) {
        this._kick_speed_is_new = _kick_speed_is_new;
    }

    private boolean _kick_speed_is_new = false;

    private static board ourInstance = new board();


    public static board getInstance(double start_point_x, double start_point_y , double left, double right, double top, double bottom) {
        ourInstance._start_point = new CoordinateContainer(start_point_x, start_point_y, Coordinate_Name.START_POINT);
        ourInstance._bottom_border = bottom;
        ourInstance._top_border = top;
        ourInstance._right_border = right;
        ourInstance._left_border = left;
        ourInstance._next_kick_value = null;

        return ourInstance;
    }

    private board() {}

    public CoordinateContainer get_start_point(){
        return this._start_point;
    }

    public ArrayList<CoordinateContainer> get_corner_coordinate(){
        ArrayList<CoordinateContainer> eges = new ArrayList<CoordinateContainer>();

        eges.add(new CoordinateContainer(this._left_border, this._top_border, Coordinate_Name.LEFT_TOP_CORNER ));
        eges.add(new CoordinateContainer(this._left_border, this._bottom_border, Coordinate_Name.LEFT_TOP_CORNER));
        eges.add(new CoordinateContainer(this._right_border, this._top_border, Coordinate_Name.RIGHT_BOTTOM_CORNER));
        eges.add(new CoordinateContainer(this._right_border, this._bottom_border, Coordinate_Name.RIGHT_BOTTOM_CORNER));

        return eges;
    }


    //diese funktion soll vom Server aufgerufen werden.
    public void set_speed_kick_value (Kick_Container new_kick, long start_time){
        CoordinateContainer coor_kick = new_kick.getKick_direction_speed();
        if(this._next_kick_value != null) {
            _next_kick_value = null;
        }
        this._next_kick_value = new CoordinateContainer(coor_kick.x, coor_kick.y);

        _kick_speed_is_new = true;
    }

    public CoordinateContainer get_next_kick_value(){
        if(_next_kick_value != null) {
            return this._next_kick_value;
        }
        return null;
    }


    public void start () {
        System.out.println("Starting Board Thread" );
        if (_board_thread == null) {
            _board_thread = new Thread (this, "board");
            _board_thread.start ();
        }
    }

    public void run(){
        CoordinateContainer _speed_kick = new CoordinateContainer(_next_kick_value);
        long last_move_time = 0;
        boolean out_of_range = false;

        while(true){

            //order new coordinate form the calculator core.
            //TOdo last_move_coordinate = get_coordinate_of_time(System.currentTimeMillis());
            last_move_time = System.currentTimeMillis();

            while (last_move_time + 10 <= System.currentTimeMillis() || ! _kick_speed_is_new ){
                // check if there is a new kick event
                if(_kick_speed_is_new){
                    _next_kick_value = null;

                    _kick_speed_is_new =false;
                    //Todo call calculate points
                    CoordinateContainer next_pos = null; // calculate( _speed_kick.start_time, _speed_kick.x, _speed_kick,y,_speed_kick,z, last_move_coordinate, geschwindigkeit? );
                    if(next_pos == null){
                        out_of_range = true;
                        break;
                    }

                    break; // jump out of the loop and start to get mh_control a new coordinate
                }

                // check, if you are still inside of the game board
                if(out_of_range){
                    out_of_range = false;

                    do_game_over();
                    do_start_game(true);
                }

            }

        }
    }


    void do_game_over(){
        // move_to(start_point, moving_head_color.PURPLE);
        long sys_time = System.currentTimeMillis();
        int repeat = 0;
        while (repeat <= 3 ){
            if((sys_time + 1000 ) >= System.currentTimeMillis()){
                //set_intensive_of_light(0);
                repeat ++;
                sys_time = System.currentTimeMillis();
            }
        }

    }

    void do_start_game(boolean first_call){
        long sys_time = System.currentTimeMillis();
        int light_intensively  = 0;
        if(first_call) {
            System.out.println(" Started new game ");
            first_call = false;
            //move_to(start_point, moving_head_color.RED);

        }
        while(true) {
            if ((sys_time + 1000) >= System.currentTimeMillis()) {
                light_intensively ^= light_intensively;
                //set_intensive_of_light( light_intensively *100); // 0* 100 = 0 and 1*100 = 100, the value is %
                sys_time = System.currentTimeMillis();
            }
            if (get_is_kick_speed_is_new()){
                return;
            }
        }
    }

}
