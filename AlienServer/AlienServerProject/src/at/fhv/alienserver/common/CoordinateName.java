package at.fhv.alienserver.common;

/**
 * Created by Jim on 09.12.2016.
 */
public enum CoordinateName {
    LEFT_TOP_CORNER(1), LEFT_BOTTOM_CORNER(2), RIGHT_TOP_CORNER(3), RIGHT_BOTTOM_CORNER(4), ERROR(5), FAILED(6), START_POINT(10);

    private int value;

    private CoordinateName(int value) { this.value = value; }

}
