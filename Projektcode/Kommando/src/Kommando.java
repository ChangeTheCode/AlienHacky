import static java.lang.System.exit;

/**
 * Created by ursus on 18/12/2016.
 */
public class Kommando {

    public static final String VERSION = "18.12.2016";
    final static int MAX_PLAYERS = 4;
    private Player players[] = new Player[MAX_PLAYERS];

    private static int MAC_ADDRESS_LENGTH = 6;

    final static String COMMAND_LOGIN = "1";
    final static String COMMAND_LOGIN_OK = "2";
    final static String COMMAND_LOGIN_NOK = "3";
    final static String COMMAND_HEARTBEAT = "4";
    final static String COMMAND_HEARTBEAT_OK = "5";
    final static String COMMAND_HEARTBEAT_NOK = "8";
    final static String COMMAND_KICK = "6";
    final static String COMMAND_KICK_OK = "7";
    final static String COMMAND_KICK_NOK = "8";

    final static int X_LENGTH = 2;
    final static int Y_LENGTH = 2;
    final static int Z_LENGTH = 2;

    public Kommando() {
    }

    public void run() {

        // connect to the UART
        UART my_UART = new UART("/dev/tty.usbmodemL1000191");
        if (my_UART.get_rc() != UART.NO_ERROR) {
            System.out.println("Error returned from .connect");
            System.out.println("RC: " + my_UART.get_rc());
            System.out.println("Message: " + my_UART.get_message());
            exit(1);
        }

        // now process one command from the UART at a time
        StringBuffer buffer = new StringBuffer();
        while (true) {
            if (!my_UART.receive(buffer)) continue;

            // login
            if (buffer.toString().substring(0, 1).equals(COMMAND_LOGIN)) {
                int i = 0;
                for (i = 0; i < MAX_PLAYERS; i++) {
                    if (players[i].getPlayer_number() == Player.NO_PLAYER) {
                        players[i].setMac_address(new StringBuffer(buffer.toString().substring(1, 6)));
                        players[i].setPlayer_number(i);
                        break;
                    }
                }
                if (i == MAX_PLAYERS)
                    my_UART.send(new StringBuffer(COMMAND_LOGIN_NOK));
                else
                    my_UART.send(new StringBuffer(COMMAND_LOGIN_OK));

                // next command
                continue;
            }

            // heartbeat
            if (buffer.toString().substring(0, 1).equals(COMMAND_HEARTBEAT)) {
                StringBuffer mac_address = new StringBuffer(buffer.toString().substring(1, MAC_ADDRESS_LENGTH));
                for (int i = 0; i < MAX_PLAYERS; i++) {
                    if (players[i].getMac_address().equals(mac_address)) {
                        my_UART.send(new StringBuffer(COMMAND_HEARTBEAT_OK));
                        continue;
                    }
                }
                // if you get here then you did not find the mac address in your list ==> should we inform them
                //    or just ignore them?
                my_UART.send(new StringBuffer(COMMAND_HEARTBEAT_NOK));

                // next command
                continue;
            }

            // kick
            if (buffer.toString().substring(0, 1).equals(COMMAND_KICK)) {
                String x = buffer.toString().substring(1, X_LENGTH);
                String y = buffer.toString().substring(1, Y_LENGTH);
                String z = buffer.toString().substring(1, Z_LENGTH);
                my_UART.send(new StringBuffer(COMMAND_KICK_OK));
            }

        }

    }
}

