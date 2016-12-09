package at.fhv.alienserver.Common;

/**
 * Created by Jim on 08.12.2016.
 */
public class Kick_Container {

    public long getTimestamp_of_kick() {
        return timestamp_of_kick;
    }

    public void setTimestamp_of_kick(long timestamp_of_kick) {
        this.timestamp_of_kick = timestamp_of_kick;
    }

    public CoordinateContainer getKick_direction_speed() {
        if(kick_direction_speed instanceof CoordinateContainer)
            return kick_direction_speed;
        else{
            return null;
        }
    }

    public void setKick_direction_speed(CoordinateContainer kick_direction_speed) {
        this.kick_direction_speed = kick_direction_speed;
    }

    private long timestamp_of_kick = 0;
    private CoordinateContainer kick_direction_speed = null;

    public  Kick_Container(long timestamp, CoordinateContainer kick_direction){
        setTimestamp_of_kick(timestamp);
        setKick_direction_speed(kick_direction);
    }

}