
package sdis_mcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

	//                   plate nr, owner
	private static HashMap<String, String> Plates = new HashMap<String, String>();

	public static void main(String[] args) throws UnknownHostException, InterruptedException {
		String addr_inet;
		int port_srv,port_inet;


		if( args.length < 3){
			System.out.println("Wrong njumber of args");
			return;
		}

		port_srv = Integer.parseInt(args[0]);
		addr_inet = args[1];
		port_inet = Integer.parseInt(args[2]);

		threquest(port_srv);



		InetAddress addr = InetAddress.getByName(addr_inet);

		String message = args[0];

		byte[] buf = message.getBytes();
		// Create a new Multicast socket (that will allow other sockets/programs
		// to join it as well.
		try (MulticastSocket announceSocket = new MulticastSocket(port_inet)){
			announceSocket.setTimeToLive(1); // as requested by the exercise
			while (true) {
				// Receive the information and print it.
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length,
						addr,port_inet);
				announceSocket.send(msgPacket);

				//System.out.println("server announced on " + addr_inet + " :" + message);
				Thread.sleep(500);//one second
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private static void threquest(Integer port){
		Thread threquest = new Thread(){
			public void run() {
				while(true) {
					try (DatagramSocket serverSocket = new DatagramSocket( port)) {


						byte[] buff = new byte[256];


						DatagramPacket msgPacket = new DatagramPacket(buff, buff.length);
						serverSocket.receive(msgPacket);
						serverSocket.close();

						String msg = new String(buff) ;

						String result = processRequest(msg);

						respondResquest(result, msgPacket.getAddress(), port);

						//						System.out.println("*********");
						//						System.out.println("Request packet: " + msgPacket.getAddress().toString());
						//						System.out.println(msg);


					} catch (IOException ex) {

						ex.printStackTrace();

					}
				}
			};
		};

		threquest.start();

	}

	private static String processRequest(String req){

		String[] reqSub = req.trim().split(" ");
		String result = new String();

		if( reqSub[0].contains("lookup") ){
			if(Plates.containsKey(reqSub[1])){
				result = reqSub[1] + ", " + Plates.get(reqSub[1]);
				System.out.println(reqSub[0] + " " + reqSub[1] + " :: " + result);


			} else {
				result = "ERROR";
				System.out.println(reqSub[0] + " " + reqSub[1] + " :: " + result);

			}
		} else { //can only be register

			if(!Plates.containsKey(reqSub[1])){
				Plates.put(reqSub[1], reqSub[2]);
				result = reqSub[1] + ", " + reqSub[2];
				System.out.println(reqSub[0] + " " + reqSub[1] + 
						" " + reqSub[2] +" :: " + result);
			} else{
				result = "ERROR";
				System.out.println(reqSub[0] + " " + reqSub[1] + 
						" " + reqSub[2] +" :: " + result);
			}
		}



		return result;

	}

	private static void respondResquest(String res, InetAddress request_addr, int port) {
		try( DatagramSocket respSocket = new DatagramSocket()){

			DatagramPacket reqPacket = new DatagramPacket(res.getBytes(), res.getBytes().length,request_addr,port+1);
			respSocket.send(reqPacket);
			respSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
