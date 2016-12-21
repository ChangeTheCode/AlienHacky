package at.fhv.alienserver.communication;

import at.fhv.alienserver.common.*;

import java.util.ArrayList;



/**
 * Created by Jim on 15.12.2016.
 */
public interface ICommunication {

    public ArrayList<AlienNode> get_node_list();

    public boolean clear_node_list();

    public boolean delete_node(String mac_address);

}
