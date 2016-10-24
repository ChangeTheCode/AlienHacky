package esp.alienhacky.com;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class ESP {

	public static final String ESP_VERSION = "19.10.2016";
	public static final String ESP_DEFAULT_IP = "10.0.0.200";
	public static final int ESP_DEFAULT_PORT = 3333;

	public static final int POLL_HEARTBEAT = 0;
	public static final int POLL_FULL_INFO = 1;
	public static final int POLL_NODE_INFO = 2;

	private static byte universe = 0;

	public byte getUniverse() {
		return ESP.universe;
	}

	public void setUniverse(byte universe) {
		ESP.universe = universe;
	}

	public static void main(String[] args) throws InterruptedException {

		// Just a message so you know what is happening 
		System.out.println ("ESP Protokoll Version " + ESP_VERSION);
		System.out.println ("IP: " + ESP_DEFAULT_IP);
		System.out.println ("Port: " + ESP_DEFAULT_PORT);

		// just test
		try {
			
			DMX head = new DMX ();
			head.pan = 100;
			head.tilt = 100;  // winkel
			head.fine_pan = 0;
			head.fine_tilt = 0;
			head.speed_pan_tilt = 0;
			head.color = 39; // red
			head.shutter = (byte) 218; // on
			head.dimmer = (byte) 255; 
			head.gobo_wheel = 7; // open
			head.gobo_rotation = 0; // fixed postition
			head.special_functions = 16; // no blackout
			head.build_in_functions = 0; // no not in use
			
			sendPollPacket (POLL_FULL_INFO);
			sendDataPacket (head, 2);
					
			Random rand = new Random();  ;
			
			for (int i = 0; i < 200; i++) {
				head.tilt = (byte) rand.nextInt(256);
				head.pan = (byte) rand.nextInt(256);
				head.color = (byte) rand.nextInt(256);
				head.shutter = (byte) 216;
				sendDataPacket (head, 2);
			    Thread.sleep (500);
			}
		//		head.color = (byte) i;
			//	sendDataPacket (head, 2);
				// Thread.sleep (1000);
		//		System.out.println("i = " + i);
			//	if ((i % 20) == 0) {
				//	System.out.println("Sleep 5 sec");
//					Thread.sleep (5000);
	//			}
			//}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static boolean sendPollPacket (int replyType) throws IOException {

		// ToDo: check parameters - maybe enums?

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

	private static boolean sendPacket (byte [] data) throws IOException {
		
		// Get the internet address of the specified host (IP also by name)
		InetAddress address = InetAddress.getByName (ESP_DEFAULT_IP);

		// Initialize a datagram packet with data and address
		DatagramPacket packet = new DatagramPacket (data, data.length, address, ESP_DEFAULT_PORT);
		DatagramSocket dsocket = new DatagramSocket ();
		dsocket.send(packet);

		// now close your socket as you are finished
		dsocket.close();
		
		// finished
		return true;
	}
	
	private static boolean sendDataPacket (DMX head, int headNumber) throws IOException {

		// setup the packet
		byte[] dataPacket = new byte [9 + (head.size * headNumber)];
		dataPacket [0] = (byte) 'E';
		dataPacket [1] = (byte) 'S';
		dataPacket [2] = (byte) 'D';
		dataPacket [3] = (byte) 'D';
		dataPacket [4] = (byte) universe; 
		dataPacket [5] = (byte) 0;  // DMX Start code
		dataPacket [6] = (byte) 1; // Send up-to 512 bytes of DMX Data
		
		// get the length of the data  
		byte[] dataSize = getBytesInBigEndian (head.size * headNumber);
		dataPacket [7] = dataSize [2];
		dataPacket [8] = dataSize [3];
		
		// clear the first head
		for (int dataPos = 0; dataPos < 12; dataPos++) {
			dataPacket [9 + dataPos] = 0;
		};
		
		// now copy my stuff  => 9 header + 12 first head = 21
		dataPacket [21] = head.pan;
		dataPacket [22] = head.tilt;
		dataPacket [23] = head.fine_pan;
		dataPacket [24] = head.fine_tilt;
		dataPacket [25] = head.speed_pan_tilt;
		dataPacket [26] = head.color;
		dataPacket [27] = head.shutter;
		dataPacket [28] = head.dimmer; 
		dataPacket [29] = head.gobo_wheel;
		dataPacket [30] = head.gobo_rotation;
		dataPacket [31] = head.special_functions;
		dataPacket [32] = head.build_in_functions;

		
		// send the data
		sendPacket (dataPacket);

		// fin
		return true;
	}

	// return an int in a byte array in big endian format
	public static byte[] getBytesInBigEndian (int value) {
	    ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
	    buffer.putInt(value);
	    return buffer.array();
	}
	
//	public  fillArray (byte [] outArray, DMX head) {
//
//		head.pan = 100;
//		head.tilt = 100;
//		head.fine_pan = 0;
//		head.fine_tilt = 0;
//		head.speed_pan_tilt = 0;
//		head.color = 9; // yellow
//		head.shutter = 100; // strobe
//		head.dimmer = 100; 
//		head.gobo_wheel = 0; // open
//		head.gobo_rotation = 0; // fixed postition
//		head.special_functions = 16; // no blackout
//		head.build_in_functions = 0; // no not in use
//
//	}
//
}

class DMX {
	
	public byte pan = 0;
	public byte tilt = 0;
	public byte fine_pan = 0;
	public byte fine_tilt = 0;
	public byte speed_pan_tilt = 0;
	public byte color = 0;
	public byte shutter = 0;
	public byte dimmer = 0;
	public byte gobo_wheel = 0;
	public byte gobo_rotation = 0;
	public byte special_functions = 0;
	public byte build_in_functions = 0;
	
	public int size = 12;
	
}
