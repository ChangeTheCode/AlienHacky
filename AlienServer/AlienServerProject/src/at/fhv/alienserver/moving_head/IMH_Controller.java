package at.fhv.alienserver.moving_head;

import at.fhv.alienserver.common.CoordinateContainer;
import at.fhv.alienserver.common.moving_head_color;

/**
 * Created by Jim on 09.12.2016.
 */
public interface IMH_Controller {

    public void move_to(CoordinateContainer position, boolean exaggerate);
    public void move_to(CoordinateContainer position, boolean exaggerate, moving_head_color color);
    public void set_light(Boolean on); //To turn on and off
}
