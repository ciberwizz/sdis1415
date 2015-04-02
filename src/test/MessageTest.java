package test;

import static org.junit.Assert.*;
import main.Chunk;
import main.Message;

import org.junit.Test;

public class MessageTest {

	@Test
	public void getHeader() {
		String crlf = Message.CRLF;
		Message msg = new Message("PUTCHUNK","sha5",5,3 );
		try{
		assertEquals("PUTCHUNK 1.0 sha5 5 3 "+ crlf+crlf ,msg.getHeader());
		
		msg.setType("GETCHUNK");
		assertEquals("GETCHUNK 1.0 sha5 5 "+ crlf+crlf ,msg.getHeader());
		
		msg.setType("STORED");
		assertEquals("STORED 1.0 sha5 5 "+ crlf+crlf ,msg.getHeader());
		
		msg.setType("REMOVED");
		assertEquals("REMOVED 1.0 sha5 5 "+ crlf+crlf ,msg.getHeader());
		
		msg.setType("DELETE");
		assertEquals("DELETE 1.0 sha5 "+ crlf+crlf ,msg.getHeader());
		
		msg.setType("CHUNK");
		assertEquals("CHUNK 1.0 sha5 5 "+ crlf+crlf ,msg.getHeader());
		} catch( Exception e){
			fail("failed: " + e.getMessage());
		}
		
		Message msg2 = new Message(msg.getHeader().getBytes());
		
		assertEquals(msg.getHeader(), msg2.getHeader());


	}

	@Test
	public void dataContructor(){
		String message1 = "PUTCHUNK 1.0 sha5 5 3 "+ Message.CRLF+ Message.CRLF+"ola"; 
		Message msg1 = new Message(message1.getBytes()); 
		
		
		try {
			
			String v = new String(msg1.getData());
			assert(message1.equals(v));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void toByte(){
		Chunk ch = new Chunk(1, "Sdaas", "chunks/", 1);
		Message msg = new Message("PUTCHUNK",ch );
		
		
		assert((new String(msg.getData()).equals("PUTCHUNK 1.0 Sdaas 1 1 "+ Message.CRLF+ Message.CRLF)));
	}
}
