package server.alienhacky.com;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class Server {
	
	public static final String MY_IP = "10.0.0.1";
	public static final int MY_PORT = 3333;
	
	public static void main(String[] args) {
		try {
			
			System.out.println ("AlienServer UDP Listing Version 17.10.2016");
			System.out.println ("Listening to IP: " + MY_IP);
			System.out.println ("Listening on port: " + MY_PORT);

			// Create a socket to listen on the port.
			@SuppressWarnings("resource")
			DatagramSocket dsocket = new DatagramSocket(null);
			InetSocketAddress address = new InetSocketAddress(MY_IP, MY_PORT);
			dsocket.bind(address);
			
			// Create a buffer to read datagrams into. If a
			// packet is larger than this buffer, the
			// excess will simply be discarded!
			byte[] buffer = new byte[2048];

			// Create a packet to receive data into the buffer
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			// Now loop forever, waiting to receive packets and printing them.
			while (true) {
				// Wait to receive a datagram
				dsocket.receive(packet);

				// Convert the contents to a string, and display them
				String msg = new String(buffer, 0, packet.getLength());
				System.out.println(packet.getAddress().getHostName() + ": "
						+ msg);

				// Reset the length of the packet before reusing it.
				packet.setLength(buffer.length);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
