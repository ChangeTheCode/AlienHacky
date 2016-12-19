package at.fhv.alienserver.Console;

import at.fhv.alienserver.game.*;

/**
 * Created by Jim on 15.12.2016.
 */
public interface IConsole {

    //function for configuration
    public boolean configuration_game_board();


    // function for game board
    public boolean start_game();

    public boolean stop_game();

    public Score get_score();

}
