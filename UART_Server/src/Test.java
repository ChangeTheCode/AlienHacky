import static java.lang.System.exit;

/**
 * Created by Ursus Schneider on 09.12.2016.
 */
public class Test {

    public static final String 	VERSION = "10.12.2016";

    public static void main(String[] args) throws InterruptedException {

        System.out.println ("Test UART code Version " + VERSION + "\n");

        // connect to the bridge
        UART my_UART = new UART ("/dev/tty.usbmodemL1000591");
        if (my_UART.get_rc() != UART.NO_ERROR) {
            System.out.println ("Error returned from .connect");
            System.out.println ("RC: " + my_UART.get_rc());
            System.out.println ("Message: " + my_UART.get_message());
            exit (1);
        }
        // ok message
        System.out.println (my_UART.get_message());

        // now read 6 lines from the port
        System.out.println ("\nReceiving 6 lines of text from the UART\n");
        StringBuffer received = new StringBuffer("");
        int i = 0;
        do {
            if (my_UART.receive(received) == true) {
                System.out.println(received);
                i++;
            }
            received.setLength(0);
        } while (i< 6);

        // send to the UART
        System.out.println ("\nSend something to UART\n");
        StringBuffer to_send = new StringBuffer("Hello there");
        my_UART.send(to_send);

        System.out.println ("\nTest UART finished\n");

    }
}
