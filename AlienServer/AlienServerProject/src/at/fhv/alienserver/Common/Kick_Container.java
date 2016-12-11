package at.fhv.alienserver.Common;

/**
 * Created by Jim on 08.12.2016.
 */
public class Kick_Container {

    private long timestamp_of_kick = 0;
    //Fixme: Why is a coordinate container used with the notation of speed????
    private CoordinateContainer kick_direction_speed = null;

    public long getTimestamp() {
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

    public  Kick_Container(long timestamp, CoordinateContainer kick_direction){
        setTimestamp_of_kick(timestamp);
        setKick_direction_speed(kick_direction);
    }

}
