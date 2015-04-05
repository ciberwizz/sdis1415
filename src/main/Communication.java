package main;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;



public class Communication {

	public static int MAX_SIZE = 64*1024; 

	private	String channel = new String();
	private int port;

	

	public static int getMAX_SIZE() {
		return MAX_SIZE;
	}

	public static void setMAX_SIZE(int mAX_SIZE) {
		MAX_SIZE = mAX_SIZE;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
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

		byte[] ret = null;

		try(MulticastSocket recveiveSocket = new MulticastSocket(port)){

			InetAddress channel_inet = InetAddress.getByName(channel);
			//Joint the Multicast group.
			recveiveSocket.joinGroup(channel_inet);

			// Receive the information and print it.
			DatagramPacket msgPacket = new DatagramPacket(data, data.length);
			recveiveSocket.receive(msgPacket);
			recveiveSocket.close();
			
			ret = new byte[msgPacket.getLength()];
			System.arraycopy(data, 0, ret, 0, msgPacket.getLength());

		}catch (Exception e) {
			e.printStackTrace();
		}


		return ret;
	}


}
