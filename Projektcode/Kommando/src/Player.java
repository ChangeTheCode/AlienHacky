public class Player {

    public static int NO_PLAYER = 0;

    private StringBuffer mac_address = new StringBuffer("");
    int player_number = 0;

    public Player () {
        this.mac_address.setLength(0);
        this.player_number = 0;
    }

    public Player (StringBuffer mac_address, int player_number) {
        this.mac_address = mac_address;
        this.player_number = player_number;
    }

    public StringBuffer getMac_address() {
        return mac_address;
    }

    public void setMac_address(StringBuffer mac_address) {
        this.mac_address = mac_address;
    }

    public int getPlayer_number() {
        return player_number;
    }

    public void setPlayer_number(int player_number) {
        this.player_number = player_number;
    }

}
