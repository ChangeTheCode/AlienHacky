package at.fhv.alienserver.common;

/**
 * Created by Jim on 08.12.2016.
 */
public class KickContainer {

    private long timestamp_of_kick = 0;
    private AccelerationContainer kick_direction_acc = null;

    public long getTimestamp() {
        return timestamp_of_kick;
    }

    public void setTimestamp_of_kick(long timestamp_of_kick) {
        this.timestamp_of_kick = timestamp_of_kick;
    }

    public AccelerationContainer getKick_direction_speed() {
        if(this.kick_direction_acc instanceof AccelerationContainer)
            return this.kick_direction_acc;
        else{
            return null;
        }
    }

    public void setKick_direction_speed(AccelerationContainer kick_direction_acc) {
        this.kick_direction_acc = kick_direction_acc;
    }

    public KickContainer(long timestamp, AccelerationContainer kick_direction){
        setTimestamp_of_kick(timestamp);
        setKick_direction_speed(kick_direction);
    }

}