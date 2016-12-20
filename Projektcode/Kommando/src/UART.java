/**
*
 *
 * this is just a test version of UART for Kommando!!!!
 *
 *
 *
 *
 * */

import gnu.io.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.io.IOException;

// UART Driver for the Server
public class UART implements SerialPortEventListener {

    // list of ports
    private Enumeration ports = null;
    private HashMap port_map = new HashMap();

    // opened port
    private CommPortIdentifier selected_port_identifier = null;
    private SerialPort serial_port = null;

    // connection parameters
    final static int TIMEOUT = 2000;
    final static String APPLICATION_NAME = "AlienServer";

    // global error messages
    private int rc;
    private String message;

    // error codes
    final static int NO_ERROR = 0;

    final static int PORT_IN_USE = 10;
    final static int IO_OPEN_ERROR = 11;
    final static int WRITE_FAIL = 12;
    final static int TOO_MANY_LISTENERS = 13;
    final static int FAILED_TO_READ_DATA = 14;

    final static int GENERAL_ERROR = 99;

    //some ascii values for for certain things
    final static char END_OF_RECORD = '#';
    final static char BRIDGE_KENNZ = '!';

    // queue for the received UART commands
    Queue queue = new LinkedList();

    // input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;
    private String curr_input = "";

    public UART (String port_to_open) {

        // add a couple of things to the queue
        queue.add ("1123456");   // Login ==> player 0
        queue.add ("1345678");   // Login ==> player 1
        queue.add ("41");   // heatbeat ==> for player 1
        queue.add ("44");   // heatbeat ==> for not here player
        queue.add ("61999111222");   //==> kick for player 1

        // fin
        rc = NO_ERROR;
        message = "UART opened";
        return;
    }

    // find all serial ports
    public void load_ports() {

        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements()) {
            CommPortIdentifier curr_ports = (CommPortIdentifier) ports.nextElement();

            //get only serial ports
            if (curr_ports.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                port_map.put(curr_ports.getName(), curr_ports);
            }
        }
    }

    public boolean init_listener() {
        try {
            serial_port.addEventListener(this);
            serial_port.notifyOnDataAvailable(true);
            return true;
        } catch (TooManyListenersException e) {
            rc = TOO_MANY_LISTENERS;
            message = "Too many listeners. (" + e.toString() + ")";
            return false;
        }
    }

    public boolean send(StringBuffer to_send) {

        // yip - we sent it :o)
        return true;
    }

    // if there is anything in the queue copy it to the string buffer and return true
    public boolean receive(StringBuffer to_receive) {

        if (queue.isEmpty()) return false;

        to_receive.setLength(0);
        to_receive.append(queue.remove());
        return true;
    }

    // connect to a sp
    public boolean connect(String port_to_open) {

        // find the port in the list of ports you have
        selected_port_identifier = (CommPortIdentifier) port_map.get(port_to_open);

        // now try and connect to that port
        CommPort comm_port = null;
        try {
            // try and open the specified port
            comm_port = selected_port_identifier.open(APPLICATION_NAME, TIMEOUT);
            serial_port = (SerialPort) comm_port; // the CommPort object can be cast to a SerialPort object

            // setup parameters
            int baud_rate = 115200;
            serial_port.setSerialPortParams(baud_rate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // all OK
            rc = NO_ERROR;
            message = port_to_open + " opened successfully.";

            return true;
        } catch (PortInUseException e) {
            rc = PORT_IN_USE;
            message = port_to_open + " is in use. (" + e.toString() + ")";
            return false;
        } catch (Exception e) {
            rc = GENERAL_ERROR;
            message = "Failed to open " + port_to_open + "(" + e.toString() + ")";
            return false;
        }
    }

    // create the input and output streams for reading and writing
    public boolean init_io_stream() {

        try {
            input = serial_port.getInputStream();
        } catch (IOException e) {
            rc = IO_OPEN_ERROR;
            message = "Input stream failed to open. (" + e.toString() + ")";
            return false;
        }
        try {
            output = serial_port.getOutputStream();
            return true;
        } catch (IOException e) {
            rc = IO_OPEN_ERROR;
            message = "Output stream failed to open. (" + e.toString() + ")";
            return false;
        }
    }

    // this gets called when a serial event happens
    public void serialEvent (SerialPortEvent event) {

        // this check is most probably not needed as we are event driven - better check anyway though!
        if (event.getEventType() != SerialPortEvent.DATA_AVAILABLE) return;

        try {
            byte byte_read = (byte) input.read();

            if (byte_read != END_OF_RECORD) {
                curr_input = curr_input + new String(new byte[]{byte_read});
            } else {
                // add to the queue for later
                queue.add (curr_input);
                curr_input = "";
            }
        } catch (Exception e) {
            rc = FAILED_TO_READ_DATA;
            message = "Failed to read data. (" + e.toString() + ")";
        }
    }


    // ===================================================================================================
    // getters and setters

    public int get_rc() {
        return rc;
    }

    public void set_rc(int rc) {
        this.rc = rc;
    }

    public String get_message() {
        return message;
    }

    public void set_message(String message) {
        this.message = message;
    }
}
