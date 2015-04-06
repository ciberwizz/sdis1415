package test;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import main.Chunk;
import main.Communication;
import main.Config;
import main.Distributed;
import main.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DistributedTest {

	@Rule
	public ExpectedException thrown= ExpectedException.none();


	@Before
	public void cleanStatic(){
		Distributed.expectChunk.clear();
		Distributed.expectStore.clear();
		Distributed.inMC.clear();
		Distributed.inMDB.clear();
		Distributed.inMDR.clear();
		Distributed.toSendChunk.clear();
		Distributed.toSendPutChunk.clear();
		Distributed.toSendStore.clear();

		Config.chunksOfOurFiles.clear();
		Config.numberOfChunks.clear();
		Config.theirChunks.clear();

		//TODO config read csv
	}

	/*
	 * 
	 * receiving commmands
	 * 		- putchunk
	 * 		- getchunk
	 * 		- removed
	 * 		- stored 
	 */


	@Test
	public void receive_putchunk() {

		Thread main = thMain();

		Communication comm = new Communication("224.0.0.7", 9999);

		Message msg = new Message("PUTCHUNK","sha5",5,3 );
		String data = "data";
		msg.getChunk().setData(data.getBytes());

		Distributed.inMDB.add(msg);

		byte[] b = comm.receive();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		main.interrupt();

		String stored  = new String(b);

		String crlf = Message.CRLF;


		assertEquals("STORED 1.0 sha5 5 "+ crlf+crlf ,stored);
		assertTrue(Config.theirChunks.containsKey(msg.getId()));


	}



	@Test
	public void receive_stored_to_ignore(){

		Thread main = thMain();

		Message msg = new Message("STORED","sha5",5,3 );

		Distributed.inMC.add(msg);
		Distributed.inMC.add(msg);
		Distributed.inMC.add(msg);
		Distributed.inMC.add(msg);

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		main.interrupt();

		assertFalse(Distributed.toSendStore.containsKey(msg.getId()));

	}


	@Test
	public void receive_stored(){

		Thread main = thMain();

		Message msgPut = new Message("PUTCHUNK","sha5",5,3 );

		Distributed.inMDB.add(msgPut);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Message msg = new Message("STORED","sha5",5,3 );


		randomAdd(400, Distributed.inMC, msg);
		randomAdd(400, Distributed.inMC, msg);
		randomAdd(400, Distributed.inMC, msg);
		randomAdd(400, Distributed.inMC, msg);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		main.interrupt();

		if(Distributed.toSendStore.containsKey(msg.getId())) {

			Message t = Distributed.toSendStore.get(msg.getId());

			assertEquals( 4 , t.getRepDegree());
		} else fail("asd");

	}

	@Test
	public void receive_getchunk(){

		Chunk chk = new Chunk(5, "sha5", "", 3);
		String data = "data";
		chk.setData(data.getBytes());

		String id = chk.getFileId() + "_" + chk.getChunkNr();

		Config.theirChunks.put(id , chk);



		Thread main = thMain();

		//MDR
		Communication comm = new Communication("224.0.0.9", 9999);

		Message msg = new Message("GETCHUNK","sha5",5,3 );

		Distributed.inMC.add(msg);

		byte[] b = comm.receive();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		main.interrupt();

		String stored  = new String(b);

		String crlf = Message.CRLF;


		assertEquals("CHUNK 1.0 sha5 5 "+ crlf+crlf+"data" ,stored);

	}

	@Test (timeout = 400)
	public void receive_getchunk_answered(){
		thrown.expect(Exception.class);
		thrown.expectMessage("test timed out after 400 milliseconds");

		Chunk chk = new Chunk(5, "sha5", "", 3);
		String data = "data";
		chk.setData(data.getBytes());

		String id = chk.getFileId() + "_" + chk.getChunkNr();

		Config.theirChunks.put(id , chk);



		Thread main = thMain();

		//MDR
		Communication comm = new Communication("224.0.0.9", 9999);

		Message msg = new Message("GETCHUNK","sha5",5,3 );
		Message msg2 = new Message("CHUNK","sha5",5,3 );

		Distributed.inMC.add(msg);

		Distributed.inMDR.add(msg2);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		main.interrupt();
		assertEquals(1, Distributed.toSendChunk.get(msg.getId()).getRepDegree());


		comm.receive();

	}

	@Test
	public void receive_removed(){
		Thread main = thMain();

		Chunk chk = new Chunk(5, "sha5", "", 3);
		String data = "data";
		chk.setData(data.getBytes());

		String id = chk.getFileId() + "_" + chk.getChunkNr();

		Config.theirChunks.put(id , chk);


		Message msgRemoved = new Message("REMOVED","sha5",5,3 );

		Distributed.inMC.add(msgRemoved);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		main.interrupt();

		assertEquals( 2 , Config.theirChunks.get(id).getRepDegree());

	}

	@Test
	public void receive_removed_trigger_putchunk(){

		Thread main = thMain();

		Communication comm = new Communication("224.0.0.7", 9999);

		Message msg = new Message("PUTCHUNK","sha5",5,3 );
		String data = "data";
		msg.getChunk().setData(data.getBytes());

		Distributed.inMDB.add(msg);

		byte[] b = comm.receive();





		String stored  = new String(b);

		String crlf = Message.CRLF;


		assertEquals("STORED 1.0 sha5 5 "+ crlf+crlf ,stored);

		Message msgRemoved = new Message("REMOVED","sha5",5,3 );

		Distributed.inMC.add(msgRemoved);

		comm.setChannel("224.0.0.8");

		b = comm.receive();
		stored  = new String(b);

		System.out.println("response to REMOVED:");
		System.out.println(stored);

		msg.setType("PUTCHUNK");

		String str = new String(msg.getData());
		assertEquals( str ,stored);

		main.interrupt();

	}

	@Test
	public void receive_putchunk_removed_putchunk(){

		Thread main = thMain();

		Communication comm = new Communication("224.0.0.7", 9999);

		Message msg = new Message("PUTCHUNK","sha5",5,3 );
		String data = "data";
		msg.getChunk().setData(data.getBytes());

		Distributed.inMDB.add(msg);

		byte[] b = comm.receive();





		String stored  = new String(b);

		String crlf = Message.CRLF;


		assertEquals("STORED 1.0 sha5 5 "+ crlf+crlf ,stored);

		Message msgRemoved = new Message("REMOVED","sha5",5,3 );

		Distributed.inMC.add(msgRemoved);
		Distributed.inMDB.add(msg);

		comm.setChannel("224.0.0.7");

		b = comm.receive();
		stored  = new String(b);


		main.interrupt();

		System.out.println("response to REMOVED:");
		System.out.println(stored);

		assertEquals("STORED 1.0 sha5 5 "+ crlf+crlf ,stored);



	}


	/*
	 * 
	 * sending commands
	 * 		- putchunk
	 * 		- getchunk
	 * 		- removed
	 * 
	 */

	@Test
	public void sending_putchunk() {

		Thread main = thMain();

		//MDB
		Communication comm = new Communication("224.0.0.8", 9999);

		Message msg = new Message("PUTCHUNK","sha5",5,3 );
		String data = "data";
		msg.getChunk().setData(data.getBytes());


		Distributed.sendRequestMessage(msg.getType(),msg,
				Distributed.expectStore,comm);


		byte[] putchunk = comm.receive();

		assertEquals(new String(msg.getData()), 
				new String(putchunk));

		msg.setType("STORED");

		Distributed.inMC.add(new Message(msg));
		Distributed.inMC.add(new Message(msg));
		Distributed.inMC.add(new Message(msg));

		try {
			Thread.sleep(450);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		main.interrupt();
		assertEquals(3, Distributed.expectStore.get(msg.getId()).getRepDegree());


	}

	@Test
	public void send_putchunk_repeat(){
		//TODO repeat 5 times each time wait for 400*n^2
		fail("fail");
	}

	@Test
	public void send_getchunk() {

		Thread main = thMain();

		//MC
		Communication comm = new Communication("224.0.0.7", 9999);

		Message msg = new Message("GETCHUNK","sha5",5,3 );
		String data = "data";
		msg.getChunk().setData(data.getBytes());


		Distributed.sendRequestMessage(msg.getType(),msg,
				Distributed.expectChunk,comm);


		byte[] putchunk = comm.receive();

		assertEquals(new String(msg.getData()), 
				new String(putchunk));

		msg.setType("CHUNK");

		Distributed.inMDR.add(new Message(msg));

		try {
			Thread.sleep(450);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		main.interrupt();
		
		assertEquals(1, Distributed.expectChunk.get(msg.getId()).getRepDegree());
	} 


	public Thread thMain(){

		Thread thReceive = new Thread(){
			@Override
			public void run() {
				super.run();

				Distributed.main(null);					
			}
		};

		thReceive.setDaemon(true);
		thReceive.start();
		return thReceive;
	}

	public void randomAdd(int t, final ConcurrentLinkedQueue<Message> in, final Message m){

		Thread th = new Thread(){
			@Override
			public void run() {
				super.run();

				Random rnd = new Random();

				try {
					Thread.sleep(rnd.nextInt(t));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				in.add(m);

			}
		};

		th.start();

	}
}
