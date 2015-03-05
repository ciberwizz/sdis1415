package sdis_mcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws UnknownHostException {


		String addr_inet;
		int port_inet;

		int request_port;
		InetAddress request_addr;

		if( args.length < 2){
			System.out.println("Wrong number of args");
			return;
		}


		addr_inet = args[0];
		port_inet = Integer.parseInt(args[1]);

		InetAddress address = InetAddress.getByName(addr_inet);
		// Create a buffer of bytes, which will be used to store
		// the incoming bytes containing the information from the server.
		// Since the message is small here, 256 bytes should be enough.
		byte[] buf = new byte[256];
		// Create a new Multicast socket (that will allow other sockets/programs
		// to join it as well.
		try (MulticastSocket clientSocket = new MulticastSocket(port_inet)){
			//Joint the Multicast group.
			clientSocket.joinGroup(address);

			// Receive the information and print it.
			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			clientSocket.receive(msgPacket);
			clientSocket.close();

			String msg = new String(buf, 0, buf.length).trim();
			System.out.println("server announced: " + msg);
			request_port = Integer.parseInt(msg.trim());
			request_addr = msgPacket.getAddress();

			while(true){
				
				String req = new String();
				
				try( DatagramSocket reqSocket = new DatagramSocket()){

					System.out.println("enter request:");
					Scanner s = new Scanner(System.in);
					req = s.nextLine();
					
					DatagramPacket reqPacket = new DatagramPacket(req.getBytes(), req.getBytes().length,request_addr,request_port);
					reqSocket.send(reqPacket);
					reqSocket.close();
					System.out.println("sent request: " + req);

				} catch (IOException e) {
					e.printStackTrace();
				}
//				Thread.sleep(50);
				try( DatagramSocket response = new DatagramSocket(request_port+1)){
					
					byte[] buffer =  new byte[256];
					
					DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
					response.receive(responsePacket);
					response.close();
					String res = new String(responsePacket.getData());
					
					System.out.println(req + " :: " + res);
					
				}catch (IOException e) {
					e.printStackTrace();
				}

			}



		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}


}


