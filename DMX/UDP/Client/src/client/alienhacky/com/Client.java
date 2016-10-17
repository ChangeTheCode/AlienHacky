package client.alienhacky.com;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Client {

	public static void main(String[] args) {

		try {
			String host = "127.0.0.1";
			int port = 9999;

			byte[] message = "Hello there - are you home?".getBytes();
			// ByteBuffer buffer;
			// buffer = ByteBuffer.wrap(message);
			// buffer.order(ByteOrder.BIG_ENDIAN);

			// Get the internet address of the specified host
			InetAddress address = InetAddress.getByName(host);

			// Initialize a datagram packet with data and address
			DatagramPacket packet = new DatagramPacket (message, message.length,
					address, port);

			// Create a datagram socket, send the packet through it, close it.
			DatagramSocket dsocket = new DatagramSocket();
			dsocket.send(packet);
			dsocket.setBroadcast(true);
			
			dsocket.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
