package main;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;



public class Communication {

	public static int MAX_SIZE = 64*1024; //TODO verificar qual o tamanho maximo de um chunk

	private	String channel = new String();
	private int port;

	public Communication(String ch, int ports){
		this.channel = ch;
		this.port = ports;		
	}

	public void send(byte[] data){

		try (MulticastSocket sendSocket = new MulticastSocket(port)){
			sendSocket.setTimeToLive(1); // as requested by the exercise
			InetAddress channel_inet = InetAddress.getByName(channel);

			// send data
			DatagramPacket msgPacket = new DatagramPacket(data, data.length,
					channel_inet, port);
			sendSocket.send(msgPacket);

		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	public byte[] receive(){
		byte[] data =  new byte[MAX_SIZE];


		try(MulticastSocket recveiveSocket = new MulticastSocket(port)){

			InetAddress channel_inet = InetAddress.getByName(channel);
			//Joint the Multicast group.
			recveiveSocket.joinGroup(channel_inet);

			// Receive the information and print it.
			DatagramPacket msgPacket = new DatagramPacket(data, data.length);
			recveiveSocket.receive(msgPacket);
			recveiveSocket.close();

		}catch (Exception e) {
			e.printStackTrace();
		}


		return data;
	}


}
