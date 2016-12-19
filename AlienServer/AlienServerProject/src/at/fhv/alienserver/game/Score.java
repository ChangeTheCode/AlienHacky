package at.fhv.alienserver.game;

/**
 * Created by Jim on 15.12.2016.
 */
public class Score {

    private long _time;
    private int _score;

    public Score(long time, int _score) {
        this._score = _score;
        this._time = time;
    }

    public Score(){
        this._score = 0;
        this._time =0;
    }
}
