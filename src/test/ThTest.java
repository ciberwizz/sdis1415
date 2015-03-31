package test;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import main.*;

import org.junit.Test;

public class ThTest {

	@Test
	public void thchrcv() {
		ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<Message>();
		
		Communication sender = new Communication("224.0.0.7", 9999);
		
		Message msg = new Message("PUTCHUNK","sha5",5,3 );

		
		ThChannelRecv thChRcv = new ThChannelRecv("224.0.0.7", 9999, queue);
		
		Thread th = new Thread(thChRcv);
		
		th.start();
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {			
			e.printStackTrace();
			fail("thread.sleep fail");
		}
		
		for(int i = 0 ; i < 3 ; i++){
			
			sender.send(msg.getData());
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {			
				e.printStackTrace();
				fail("thread.sleep fail");
			}	
		}
		
		assert(queue.size() == 3);
		
		for(Message m:queue){
			assert(m != null);
		}
	}
	
}
