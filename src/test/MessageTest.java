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
		
		//TODO check if any crlf char is presant,
		//TODO check if all chars are there
	}
	
	@Test
	public void toByte(){
		Chunk ch = new Chunk(1, "Sdaas", "chunks/", 1);
		Message msg = new Message("PUTCHUNK",ch );
		
		
		assertNotNull(ch.readFromFile());
		assert(msg.getData().length > 0);
	}
}
