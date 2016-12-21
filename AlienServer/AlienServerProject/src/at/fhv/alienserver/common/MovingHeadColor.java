package at.fhv.alienserver.Common;

/**
 * Created by Jim on 09.12.2016.
 */
public enum MovingHeadColor {
    RED(69), PINK(13), GREEN(2), BLUE(3), PURPLE(4), WHITE(5);

    private int value;

    private MovingHeadColor(int value) {
        this.value = value;
    }

    public int getValue() {
            return value;
    }
}
