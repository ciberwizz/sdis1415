


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class Server {

	//                   plate nr, owner
	private static HashMap<String, String> Plates = new HashMap<String, String>();

	public static void main(String[] args) {
		String addr_inet;
		int port_srv,port_inet;


		if( args.length < 1){
			System.out.println("Wrong njumber of args");
			return;
		}

		port_srv = Integer.parseInt(args[0]);


		ServerSocket sSocket = null;
		try {
			sSocket = new ServerSocket( port_srv);


			while(true){
				Socket s = null;
				try {
					s = sSocket.accept();
					threquest(s);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				} 


			}
			sSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}






	}

	private static void threquest(Socket server){
		Thread threquest = new Thread(){
			public void run() {
				try {

					DataInputStream input = new DataInputStream(server.getInputStream());

					String msg = new String(input.readUTF()) ;


					System.out.println("Received: " + msg);

					String result = processRequest(msg);

					DataOutputStream output = new DataOutputStream(server.getOutputStream());

					output.writeUTF(result);
					input.close();

					output.close();
					server.close();

					//						System.out.println("*********");
					//						System.out.println("Request packet: " + msgPacket.getAddress().toString());
					//						System.out.println(msg);


				} catch (IOException ex) {

					ex.printStackTrace();

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



		//Simulate alot of work
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}



}
