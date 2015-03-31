package main;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ThChannelRecv implements Runnable {

	ConcurrentLinkedQueue<Message> queue;
	Communication comm;
	
	public ThChannelRecv(String _channel, int port, ConcurrentLinkedQueue<Message> _queue){
		
		this.queue = _queue;
		
		comm = new Communication(_channel, port);
	}

	@Override
	public void run() {
		

		while(true){
			
			byte[] buff = comm.receive();
			
			Message m = new Message(buff);
			
			queue.add(m);
			System.out.println("DEBUG thMC: added to the queue");
			
		}
		
		
	}
	
	
}
