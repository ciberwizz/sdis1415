

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) {

		String[] keys = {"key1", "key2","key3", "key4", "key21","key31", "key41", "key22","key32", "key42", "key23","key33", "key43", "key24","key34", "key44", "key25","key35", "key45", "key26","key36", "key46", "key27","key37", "key47"};
		String[] values = {"value1", "value2","value3", "value4", "value21","value31", "value41", "value22","value32", "value42", "value23","value33", "value43", "value24","value34", "value44", "value25","value35", "value45", "value26","value36", "value46", "value27","value37", "value47"};
		String[] command = {"register", "lookup"};

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

		// Create a buffer of bytes, which will be used to store
		// the incoming bytes containing the information from the server.
		// Since the message is small here, 256 bytes should be enough.
		// Create a new Multicast socket (that will allow other sockets/programs
		// to join it as well.

		Random rad =  new Random(System.currentTimeMillis());

		for(int i = 0; i < 1000 ; i++){

			Thread th = new Thread(){
				@Override
				public void run() {
					super.run();

					String req = new String();

					try( Socket reqSocket = new Socket(addr_inet,port_inet)){

						//							System.out.println("enter request:");
						//							Scanner s = new Scanner(System.in);
						//							req = s.nextLine();


						req = command[rad.nextInt(command.length)] +" " + keys[rad.nextInt(keys.length)]
								+ " " + values[rad.nextInt(values.length)];

						DataOutputStream output = new DataOutputStream(reqSocket.getOutputStream());

						output.writeUTF(req);
						System.out.println("sent request: " + req);

						DataInputStream input = new DataInputStream(reqSocket.getInputStream());
						String resp = input.readUTF();
						System.out.println("Response: " + req + " :: " + resp);

						output.close();

						input.close();


						reqSocket.close();


					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			};
			
			th.start();


		}



	}


}


