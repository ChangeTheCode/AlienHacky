package at.fhv.alienserver.movingHead;

/*
 * ESP (DMX over ETHERNET)
 *
 * @author	Ursus Schneider
 * @version	24.10.2016
 *
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ESP {

    public static final int POLL_HEARTBEAT = 0;
    public static final int POLL_FULL_INFO = 1;
    public static final int POLL_NODE_INFO = 2;

    public static final int ESP_DEFAULT_PORT = 3333;

    private static final String ESP_DEFAULT_IP = "10.0.0.200";
    private static final int MAX_PACKET = 512;
    private static final int OVERHEAD = 9;

    // michael: is this better than having a constructor? do you do a constructor for each parameter pair?
    private byte universe;
    private String ip;
    private int port;
    /*
     * constructor with default parameters
     */
    public ESP () {
        this.universe = 0;
        this.ip = ESP_DEFAULT_IP;
        this.port = ESP_DEFAULT_PORT;
    }
    /*
     * constructor
     * @params	universe	we are using universe 0 -> gets set in the config untility?
     * @params	ip			IP we are sending to
     * @params	port		Port for the communication
     */
    public ESP (byte universe, String ip, int port) {

        this.universe = universe;
        this.ip = ip;
        this.port = port;
    }
    /*
     * sendPackets 			Send packet of data to the moving heads
     * 						Just add as may DMX instances as you have
     *
     * @param	dmxs		variable number of DMX objects
     * @return  boolean		true if data sent, else false
     *
     */

    public boolean sendPackets (DMX...dmxs ) throws IOException {

        // check if you have too many DMX objects
        int DMXCount = 0;
        int DMXSize = 0;
        for (DMX dmx : dmxs) {
            DMXCount++;
            DMXSize = dmx.size;
        }

        // check you have enought space in your structure
        if ((MAX_PACKET - OVERHEAD - (DMXCount * DMXSize)) < 0) return false;

        // check if you have any data to actually send
        if (0 == DMXCount) return false;

        // DMX512 has a max of 512 bytes of data
        byte[] dataPacket = new byte [OVERHEAD + (DMXCount * DMXSize)];

        // we are sending data
        dataPacket [0] = (byte) 'E';
        dataPacket [1] = (byte) 'S';
        dataPacket [2] = (byte) 'D';
        dataPacket [3] = (byte) 'D';
        dataPacket [4] = (byte) universe;
        dataPacket [5] = (byte) 0;  // DMX Start code
        dataPacket [6] = (byte) 1; // Send up-to 512 bytes of DMX Data

        // now add the parameters one at a time
        int i = OVERHEAD;
        int dataSize = 0;
        for (DMX dmx : dmxs) {
            dataSize = dataSize + dmx.size; // needed later for the size of the array
            dataPacket [i++] = dmx.pan;
            dataPacket [i++] = dmx.tilt;
            dataPacket [i++] = dmx.fine_pan;
            dataPacket [i++] = dmx.fine_tilt;
            dataPacket [i++] = dmx.speed_pan_tilt;
            dataPacket [i++] = dmx.color;
            dataPacket [i++] = dmx.shutter;
            dataPacket [i++] = dmx.dimmer;
            dataPacket [i++] = dmx.gobo_wheel;
            dataPacket [i++] = dmx.gobo_rotation;
            dataPacket [i++] = dmx.special_functions;
            dataPacket [i++] = dmx.build_in_functions;
        }

        // get the length of the data
        byte[] endianSize = getBytesInBigEndian (dataSize);
        dataPacket [7] = endianSize [2];
        dataPacket [8] = endianSize [3];

        // send the data
        return sendPacket (dataPacket);
    }
    /*
     * sendPollPacket 		Send a poll packet to the ESP - unfortunatly I have not been able to get a return from the head
     * 						We do not seem to get any RC on the line at all
     * @params	 int		one of the following three commands: POLL_HEARTBEAT, POLL_FULL_INFO or POLL_NODE_INFO
     * @returns	 boolean	true
     */
    // send a Poll packet
    public boolean sendPollPacket (int replyType) throws IOException {

        // setup the packet
        byte[] dataPacket = new byte [5];
        dataPacket [0] = (byte) 'E';
        dataPacket [1] = (byte) 'S';
        dataPacket [2] = (byte) 'P';
        dataPacket [3] = (byte) 'P';
        dataPacket [4] = (byte) replyType;

        // send the data
        sendPacket (dataPacket);

        return true;
    }
    /*
     * sendPacket				Send the byte array via UPD
     * @params		byte array	Data to send
     * @returns		boolean		true if no exception
     *
     */
    private boolean sendPacket (byte [] data) throws IOException {

        // Get the internet address of the specified host (IP also by name)
        InetAddress address = InetAddress.getByName (this.ip);

        // Initialize a datagram packet with data and address
        DatagramPacket packet = new DatagramPacket (data, data.length, address, this.port);
        DatagramSocket dsocket = new DatagramSocket ();
        dsocket.send(packet);

        // now close your socket as you are finished
        dsocket.close();

        // finished
        return true;
    }
    /*
     * getBytesInBigEndian	Convert a length into a 4 byte big endian length
     * @params	value		int we need to convert to a big endian 4 byte value
     * @return	byte array	4 byte array that contains int in big endian
     */
    private static byte[] getBytesInBigEndian (int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        return buffer.array();
    }
    /*
     *
     * Getters and Setters
     *
     */
    public byte getUniverse() {
        return universe;
    }

    public void setUniverse(byte universe) {
        this.universe = universe;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}