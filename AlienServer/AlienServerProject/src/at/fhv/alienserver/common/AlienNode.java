package at.fhv.alienserver.common;

/**
 * Created by Jim on 15.12.2016.
 */
public class AlienNode {

    private String _name;
    private String _mac_address;
    private Boolean _is_connected;


    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_mac_address() {
        return _mac_address;
    }

    public void set_mac_address(String _mac_address) {
        this._mac_address = _mac_address;
    }

    public Boolean get_is_connected() {
        return _is_connected;
    }

    public void set_is_connected(Boolean _is_connected) {
        this._is_connected = _is_connected;
    }

    public AlienNode(String name, String mac_addr , boolean connect ){
        this._name = name;
        this._mac_address = mac_addr;
        this._is_connected = connect;
    }

}
