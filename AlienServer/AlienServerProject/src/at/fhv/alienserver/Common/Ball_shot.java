package at.fhv.alienserver.Common;

/**
 * Created by Jim on 09.12.2016.
 */
public class Ball_shot {

    //Members
    private CoordinateContainer _next_kick_value;
    private long timestamp = 0;

    //Function
    public CoordinateContainer get_next_kick_value() {
        return _next_kick_value;
    }

    public void set_next_kick_value(CoordinateContainer _next_kick_value) {
        this._next_kick_value = _next_kick_value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
