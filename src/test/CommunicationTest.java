package test;

import static org.junit.Assert.fail;
import main.Communication;

import org.junit.Test;

public class CommunicationTest {

	@Test
	public void test() {
		Thread sender = new Thread(){
			@Override
			public void run() {
				super.run();

				Communication sender = new Communication("224.0.0.7", 9999);
				try {
					this.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String blah = "uniTest";
				System.out.println("Sending: " + blah);
				sender.send(blah.getBytes());
			}
		};

		sender.start();

		Communication receiver = new Communication("224.0.0.7", 9999);

		String str = new String(receiver.receive());

		System.out.println("RECEIVED: " + str);


		assert(str.equals("uniTest"));
	}
	
	@Test
	public void ignore_from_same_port(){
		fail("not ignoring");
	}

}
