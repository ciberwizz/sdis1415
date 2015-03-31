package main;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Distributed {

	public static void main(String[] args) {

		ConcurrentLinkedQueue<Message> inMC = new ConcurrentLinkedQueue<Message>();
		ConcurrentLinkedQueue<Message> inMDB = new ConcurrentLinkedQueue<Message>();
		ConcurrentLinkedQueue<Message> inMDR = new ConcurrentLinkedQueue<Message>();

		//DEBUG
		//TODO REMOVE
		String[] a = {"224.0.0.7", "9999", "224.0.0.8", "9999", "224.0.0.9", "9999"};
		args = a;

		if(args.length < 6){
			System.out.println("Args: Channel_MC Port_MC Channel_MDB Port_MDB Channel_MDR Port_MDR");
			return;
		}

		String chMc = args[0];
		int chMcPort =  Integer.parseInt(args[1]);

		String chMdb = args[2];
		int chMdbPort =  Integer.parseInt(args[3]);

		String chMdr = args[4];
		int chMdrPort =  Integer.parseInt(args[5]);

		Thread thMC = new Thread(new ThChannelRecv(chMc, chMcPort, inMC));
		Thread thMDB = new Thread(new ThChannelRecv(chMdb, chMdbPort, inMDB));
		Thread thMDR = new Thread(new ThChannelRecv(chMdr, chMdrPort, inMDR));



		while(true){

			Message temp;

			//check inMC
			temp = inMC.poll();

			if(temp != null){
				//check
					//stored
					//getchunk
					//delete
					//removed
				
				switch(temp.getType() ){

				case "STORED":
					
					break;
					
				case "GETCHUNK":
					
					break;
					
				case "DELETE":
					
					break;
					
				case "REMOVED":
					
					break;
				
					
					default:
						
				}
			}



			//check inMDB
			temp = inMDB.poll();

			if(temp != null){
				//check
					//putchunk
				
				if(temp.getType().equals("PUTCHUNK")){
					
				}
				
			}




			//check inMDR
			temp = inMDR.poll();

			if(temp != null){
				//check
					//chunk
				
				if(temp.getType().equals("CHUNK")){
					
				}
			}



		}



	}

}
