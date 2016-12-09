package at.fhv.alienserver.movingHead;

import at.fhv.alienserver.Common.CoordinateContainer;
import at.fhv.alienserver.Common.moving_head_color;

/**
 * Created by Jim on 09.12.2016.
 */
public interface IMH_Controller {

    public void move_to(CoordinateContainer position, boolean exaggerate);
    public void move_to(CoordinateContainer position, boolean exaggerate, moving_head_color color);
    public void set_light(Boolean on); //To turn on and off
}
