package test;

import static org.junit.Assert.*;
import main.Message;

import org.junit.Test;

public class MessageTest {

	@Test
	public void ToString() {
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
		
		//TODO give wrong type
		try{
			msg.setType("PUTCHUNK2");
			assertNotEquals("PUTCHUNK 1.0 sha5 5 3 "+ crlf+crlf ,msg.getHeader());
			
		}catch( Exception e){
			return;
		}

	}

	@Test
	public void dataContructor(){
		fail("Not yet implemented");
	}
	
	@Test
	public void toByte(){
		fail("Not yet implemented");
	}
}
