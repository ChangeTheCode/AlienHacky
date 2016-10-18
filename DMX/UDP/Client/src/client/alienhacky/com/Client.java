package client.alienhacky.com;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
	
	public static final String ESP_IP = "10.0.0.200";
	public static final int ESP_PORT = 3330;


	public static void main(String[] args) {

		try {
			
			System.out.println ("AlienServer UDP Send commands Version 17.10.2016");
			System.out.println ("Sending to IP: " + ESP_IP);
			System.out.println ("Sending on port: " + ESP_PORT);

			byte[] message = "ESDD001".getBytes();
			// ByteBuffer buffer;
			// buffer = ByteBuffer.wrap(message);
			// buffer.order(ByteOrder.BIG_ENDIAN);

			// Get the internet address of the specified host
			InetAddress address = InetAddress.getByName (ESP_IP);

			// Initialize a datagram packet with data and address
			DatagramPacket packet = new DatagramPacket (message, message.length, address, ESP_PORT);

			// Create a datagram socket, send the packet through it, close it.
			DatagramSocket dsocket = new DatagramSocket();
			// dsocket.setBroadcast(true);
			dsocket.send(packet);
			
			dsocket.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
