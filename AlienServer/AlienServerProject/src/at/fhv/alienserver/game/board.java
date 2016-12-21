
package at.fhv.alienserver.game;

import at.fhv.alienserver.common.*;
import at.fhv.alienserver.calculator.Calculator;
import at.fhv.alienserver.moving_head.MHControl;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jim on 07.12.2016.
 */
public class Board implements Runnable, IBoard{

    //TODO: Spielstand einbauen

    private Board() {}

    private static Thread _board_thread;

    private double _left_border;
    private double _top_border;
    private double _bottom_border;
    private double _right_border;

    private CoordinateContainer _start_point = null;

    private Kick_Container _latest_kick = null;

    private boolean _kick_speed_is_new = false;

    private static board ourInstance = new board();



    public boolean get_is_kick_speed_is_new() {
        return _kick_speed_is_new;
    }

    public static board getInstance(double start_point_x, double start_point_y , double left, double right, double top, double bottom) {
        ourInstance._start_point = new CoordinateContainer(start_point_x, start_point_y, Coordinate_Name.START_POINT);
        ourInstance._bottom_border = bottom;
        ourInstance._top_border = top;
        ourInstance._right_border = right;
        ourInstance._left_border = left;
        ourInstance._latest_kick = null;

        return ourInstance;
    }

    public CoordinateContainer get_start_point(){
        return this._start_point;
    }

    public ArrayList<CoordinateContainer> get_corner_coordinate(){
        ArrayList<CoordinateContainer> eges = new ArrayList<CoordinateContainer>();

        eges.add(new CoordinateContainer(this._left_border, this._top_border, Coordinate_Name.LEFT_TOP_CORNER ));
        eges.add(new CoordinateContainer(this._left_border, this._bottom_border, Coordinate_Name.RIGHT_TOP_CORNER));
        eges.add(new CoordinateContainer(this._right_border, this._top_border, Coordinate_Name.LEFT_BOTTOM_CORNER));
        eges.add(new CoordinateContainer(this._right_border, this._bottom_border, Coordinate_Name.RIGHT_BOTTOM_CORNER));

        return eges;
    }


    //diese funktion soll vom Server aufgerufen werden.
    public void set_speed_kick_value (Kick_Container new_kick){

        System.out.println("set Speed was called ");
        //Thread.currentThread().interrupt();

        if(this._latest_kick == null) {
            _latest_kick = new_kick;
        }
        this._latest_kick.setKick_direction_speed( _latest_kick.getKick_direction_speed());

        _kick_speed_is_new = true;
    }

    public Kick_Container get_latest_kick(){
        if(_latest_kick != null) {
            return this._latest_kick;
        }
        return null;
    }


    public void start () {
        System.out.println("Starting Board Thread" );
        if (_board_thread == null) {
            _board_thread = new Thread (this, "Board");
            _board_thread.start ();
        }
    }

    public void run(){
        //AccelerationContainer _speed_kick = new AccelerationContainer(_latest_kick.getKick_direction_speed());

        long last_move_time = 0;
        boolean out_of_range = false;
        MHControl my_DMX;

        try {
            my_DMX = new MHControl(2.0, false,true);
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        Calculator my_Calculator = new Calculator();
        ArrayList<CoordinateContainer> eges = get_corner_coordinate();

        my_Calculator.init_Calculator(eges.get(0), eges.get(1), eges.get(2), eges.get(3));

        CoordinateContainer next_pos = null;
        while(true){

            last_move_time = System.currentTimeMillis();

            next_pos = my_Calculator.get_position(last_move_time);

            while (last_move_time + 10 <= System.currentTimeMillis() || ! _kick_speed_is_new ){
                // check if there is a new kick event
                if(_kick_speed_is_new){

                    _kick_speed_is_new =false;
                    if(! my_Calculator.kick(_latest_kick)){
                        //TODo: SysOut
                    }

                    _latest_kick = null;

                    if(next_pos == null){
                        out_of_range = true;
                        break;
                    }

                    break; // jump out of the loop and start to get mh_control a new coordinate
                }

                // check, if you are still inside of the game Board
                if(out_of_range){
                    out_of_range = false;

                    do_game_over(my_DMX);
                    do_start_game(true, my_DMX);
                }
            }// end While

        }//end While(true)
    }


    private void do_game_over(MHControl dmx_control){
        dmx_control.move_to(get_start_point(), false, moving_head_color.PURPLE);
        long sys_time = System.currentTimeMillis();
        int repeat = 0;
        while (repeat <= 3 ){
            if((sys_time + 1000 ) >= System.currentTimeMillis()){
                dmx_control.set_light(true); // ToDo toggeln
                repeat ++;
                sys_time = System.currentTimeMillis();
            }
        }

    }

    private void do_start_game(boolean first_call, MHControl my_DMX){
        long sys_time = System.currentTimeMillis();
        int light_intensively  = 0;
        if(first_call) {
            System.out.println(" Started new game ");
            first_call = false;
            my_DMX.move_to(get_start_point(), false, moving_head_color.RED);

        }
        while(true) {
            if ((sys_time + 1000) >= System.currentTimeMillis()) {
                light_intensively ^= light_intensively;

                my_DMX.set_light( true);  // TOdo: toggeln
                sys_time = System.currentTimeMillis();
            }
            if (get_is_kick_speed_is_new()){
                return;
            }
        }
    }

}