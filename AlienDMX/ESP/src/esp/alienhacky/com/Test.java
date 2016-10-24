package esp.alienhacky.com;

import java.io.IOException;

public class Test {

	public static final String 	VERSION = "24.10.2016";

	public static void main(String[] args) throws InterruptedException {

		// ESP
		System.out.println ("Test ESP Protokoll Version " + VERSION);
		
		// get your parameters (if there are any) 
		ESP esp = null;
		if (args.length == 0) {
			esp = new ESP ();
		} else if (args.length == 1) {
			String ip = args [0];
			esp = new ESP ((byte) 0, ip, ESP.ESP_DEFAULT_PORT);
		} else if (args.length == 2) {
			String ip = args [0];
			String port = args [1];
			esp = new ESP ((byte) 0, ip, Integer.parseInt (port));
		} else {
	        System.err.println("");
	        System.err.println("SYNTAX:");
	        System.err.println("\t\ttest [ip]");
	        System.err.println("\t\ttest [ip port]");
	        System.err.println("");
	        System.err.println("eg. test 10.10.0.6 3330");
	        System.exit(1);
		}
		
		// Just a message so you know what is happening 
		System.out.println ("IP:   " + esp.getIp());
		System.out.println ("Port: " + esp.getPort());
		System.out.println ("Head: the head we are talking to is fixed at position 2");

		// we are at head no. 2, just create a dummy head 1
		DMX dummyHead = new DMX ();

		// setup head 2
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
		
		// now send your packets
		try {
			esp.sendPackets (dummyHead, head);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
