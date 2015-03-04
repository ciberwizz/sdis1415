
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class Communication {
	String ip;
	int port;
	int timeOut;
	MulticastSocket socket;
	DatagramPacket dPacket;
	InetAddress inet;

	public Communication(){
		this.timeOut=20;
	}

	public void Connect(String ip, int port) throws IOException{
		socket= new MulticastSocket(port);
		inet=InetAddress.getByName(ip);
		socket.joinGroup(inet);
		//disable loopback
		socket.setLoopbackMode(true);
		this.port=port;
		this.ip=ip;
	}


	public void closeMulticastSocket() throws UnknownHostException, IOException{
		socket.leaveGroup(inet);
		socket.close();
	}

	public void send(Message m) throws IOException{
		byte[] data = m.createMessage();
		dPacket = new DatagramPacket(data, data.length, inet, port);
		socket.send(dPacket);
	}
/*
	//para teste//
	public void send(String str) throws IOException{
		String test;
		Scanner sc =new Scanner(System.in);
		test=sc.nextLine();
		if(test.equals("send")){
			byte[] data = str.getBytes();
			inet=InetAddress.getByName("239.1.1.194");
			dPacket = new DatagramPacket(data, data.length,inet , 6781);
			socket.send(dPacket);
			String sta = new String(data,0,data.length);
			System.out.println("Enviei mensagem!: " + sta);
		}
	}


	public void receive() throws IOException{
		byte[] buffer = new byte[65];		
		inet=InetAddress.getByName("239.1.1.194");
		dPacket = new DatagramPacket(buffer, buffer.length,inet,6781);
		socket.receive(dPacket);
		byte[] data = dPacket.getData();

		String st = new String(data,0,data.length);
		System.out.println("Recebi mensagem!: " + st);

	}



	// fim do para testes //

	*/

	public Message receive() throws IOException{
		int ttl = 1;
		byte[] buffer = new byte[65*1024];		

		dPacket = new DatagramPacket(buffer, buffer.length,inet,port);
		socket.setTimeToLive(ttl);
		socket.setSoTimeout(timeOut);
		try{
			
		socket.receive(dPacket);

		byte[] data = dPacket.getData();
		byte[] data2 = new byte[dPacket.getLength()];
		System.arraycopy(data, 0, data2, 0, dPacket.getLength());


		Message m = new Message(data2.clone());
		return m;
		} catch(IOException e){
			throw e;
		}
	}
	 


}
