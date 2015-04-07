package main;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Distributed {

	public static Config config = new Config();

	public static ConcurrentLinkedQueue<Message> inMC = new ConcurrentLinkedQueue<Message>();
	public static ConcurrentLinkedQueue<Message> inMDB = new ConcurrentLinkedQueue<Message>();
	public static ConcurrentLinkedQueue<Message> inMDR = new ConcurrentLinkedQueue<Message>();

	//responses for us
	public static ConcurrentHashMap<String, Message> expectChunk = new ConcurrentHashMap<String, Message>();
	public static ConcurrentHashMap<String, Message> expectStore = new ConcurrentHashMap<String, Message>();

	//for us to send
	public static ConcurrentHashMap<String, Message> toSendChunk = new ConcurrentHashMap<String, Message>();	
	public static ConcurrentHashMap<String, Message> toSendPutChunk = new ConcurrentHashMap<String, Message>();
	public static ConcurrentHashMap<String, Message> toSendStore = new ConcurrentHashMap<String, Message>();


	public static void main(String[] args) {

		//DEBUG
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
		
		//load csvs
		Config.loadCSV();
		
		Interface interfc = Interface.runInterface();

		Thread thMC = new Thread(new ThChannelRecv(chMc, chMcPort, inMC));
		Thread thMDB = new Thread(new ThChannelRecv(chMdb, chMdbPort, inMDB));
		Thread thMDR = new Thread(new ThChannelRecv(chMdr, chMdrPort, inMDR));

		thMC.start();
		thMDR.start();
		thMDB.start();
		

		while(!Thread.currentThread().isInterrupted()){

			Message temp;

			//check inMC
			temp = inMC.poll();

			if(temp != null){
				//check
				//stored
				//getchunk
				//delete
				//removed
				String id = temp.getFileId() + "_" + temp.getChunkNr();

				switch(temp.getType() ){

				case "STORED":

					if(expectStore.containsKey(id)){
						temp = expectStore.get(id);
						temp.incRepDegree();
						expectStore.replace(id, temp);
					} else 
						if(toSendStore.containsKey(id)){
							temp = toSendStore.get(id);
							temp.incRepDegree();
							toSendStore.replace(id, temp);
						} 

					break;

				case "GETCHUNK":
					temp.setRepDegree(0);

					Message t;

					if(Config.chunksOfOurFiles.contains(id)){

						t = new Message("CHUNK", Config.chunksOfOurFiles.get(id));

						temp.setRepDegree(0);
						toSendChunk.put(id, temp);

						sendResponseMessage("CHUNK",t,toSendChunk, new Communication(chMdr, chMdrPort));


					} else 
						if(Config.theirChunks.containsKey(id)){

							t = new Message("CHUNK", Config.theirChunks.get(id));

							temp.setRepDegree(0);
							toSendChunk.put(id, temp);

							sendResponseMessage("CHUNK",t,toSendChunk, new Communication(chMdr, chMdrPort));
						}
					break;

				case "DELETE":

					try {
						Config.deleteFile(temp.getFileId());
					} catch (NoSuchAlgorithmException | IOException e) {
						e.printStackTrace();
					}

					break;

				case "REMOVED":
					//Chunk if repdegree < objectiveRepDegree
					Chunk chk = config.decRepDegree(temp);
					if( chk != null){

						t = new Message("PUTCHUNK", chk);

						temp.setRepDegree(0);
						toSendPutChunk.put(id, temp);

						sendResponseMessage("PUTCHUNK",t,toSendPutChunk, new Communication(chMdb, chMdbPort));
					}
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

					String id = temp.getFileId() + "_" + temp.getChunkNr();

					if(toSendPutChunk.containsKey(id)){

						Message m = toSendPutChunk.get(id);

						m.incRepDegree();
						toSendPutChunk.replace(id, m);

					}

					temp.setRepDegree(0);
					toSendStore.put(id,temp);
					sendResponseMessage("STORED",temp,toSendStore, new Communication(chMc, chMcPort));
				}

			}




			//check inMDR
			temp = inMDR.poll();

			if(temp != null){
				//check
				//chunk

				if(temp.getType().equals("CHUNK")){

					String id = temp.getFileId() + "_" + temp.getChunkNr();

					if(expectChunk.containsKey(id)){

						temp.setRepDegree(1);
						expectChunk.replace(id, temp);

						//TODO send to config

					} else
						if(toSendChunk.containsKey(id)){

							//the thread will check
							//	1 - a chunk was already sent
							//  0 - a chunk was not sent
							temp.setRepDegree(1);
							toSendChunk.replace(id, temp);							
						}



				}
			}



			//TODO actualizar os repdegrees conforme o que estiver nos expect e tosend
			//TODO actualizar o chunk data e enviar para o config fazer escrita
			//TODO limpar os pedidos que foram á mais de 400ms



		/*
		 * actualizar o repdegrees
		 * e remover os q estao a mais de 400ms
		 */
			
			for( Message n :expectStore.values()){
				if(n!=null){
					String id = n.getId();
					Chunk temp1;
					if(n.getElapsed() > 400 ){
						if(Config.chunksOfOurFiles.containsKey(id)){

							temp1 = Config.chunksOfOurFiles.get(id);
							temp1.setRepDegree(n.getRepDegree());
							Config.chunksOfOurFiles.replace(id, temp1);

						} else
							if(Config.theirChunks.containsKey(id)){

								temp1 = Config.theirChunks.get(id);
								temp1.setRepDegree(n.getRepDegree());
								Config.theirChunks.replace(id, temp1);
							}

						expectStore.remove(id);
					}
				}

			}

			for( Message n :toSendStore.values()){
				if(n!=null){
					String id = n.getId();
					Chunk temp1;
					if(n.getElapsed() > 400 ){
						if(Config.theirChunks.containsKey(id)){

							temp1 = Config.theirChunks.get(id);
							temp1.setRepDegree(n.getRepDegree());
							Config.theirChunks.replace(id, temp1);
						}

						toSendStore.remove(id);
					}
				}

			}

			/*
			 * Actualizar chunks.data
			 */

			for( Message n :expectChunk.values()){
				if(n!=null){
					String id = n.getId();
					Chunk temp1;
					if(n.getElapsed() > 400 ){
						if(Config.chunksOfOurFiles.containsKey(id)){

							temp1 = Config.chunksOfOurFiles.get(id);
							temp1.setData(n.getChunk().getData());
							Config.chunksOfOurFiles.replace(id, temp1);
							temp1.saveToFile();

						} else
							if(Config.theirChunks.containsKey(id)){

								temp1 = Config.theirChunks.get(id);
								temp1.setData(n.getChunk().getData());
								Config.theirChunks.replace(id, temp1);
								temp1.saveToFile();
							}

						expectChunk.remove(id);
					}
				}

			}
			
			/*
			 * remove old messages
			 */
			
			for( Message n :toSendChunk.values())
				if(n!=null){
					if (n.getElapsed() > 400) {
						
						toSendChunk.remove(n.getId());
					}
				}
			
			for( Message n :toSendPutChunk.values())
				if(n!=null){
					if (n.getElapsed() > 400) {
						
						toSendPutChunk.remove(n.getId());
					}
				}
			
			
			
			/*
			 * 
			 * check if there are missing chunks that need to do getchunk
			 * 
			 */
			
			if(!Config.missingChunks.isEmpty()){
				String id =  Config.missingChunks.poll();
				
				if(id != null){
					Message m = new Message("GETCHUNK", id);
					sendRequestMessage(m.getType(),m,expectChunk,
							new Communication(chMc, chMcPort));
					
					//if it wa the last one
					if(Config.missingChunks.isEmpty()){
						Thread th =  new Thread(){
							@Override
							public void run() {
								super.run();
								
								try {
									Thread.sleep(400);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								
								interfc.updateTree();
								
							}
						};
					}
				}
				
			}
			
			
			


			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {

			}

		}



	}

	private static void sendResponseMessage(String type, Message temp,
			final ConcurrentHashMap<String, Message> hash, 
			final Communication comm) {

		final Message out_m;

		final String id = temp.getFileId() + "_" + temp.getChunkNr();

		switch (type) {
		case "STORED":
		case "PUTCHUNK":
		case "CHUNK":
			out_m = new Message(temp);
			out_m.setType(type);
			break;



		default:
			System.err.println("Wrong type to send a message");
			out_m = null;
			break;
		}		

		Thread th = new Thread(){
			Message out = out_m;
			Communication com = comm;
			@Override
			public void run() {

				super.run();

				Random rnd = new Random();				

				if(out != null){

					//TODO retries can be 400*2^n max
					int sleep = (int) (400 - out.getElapsed());

					if(sleep>0)
						sleep = rnd.nextInt(sleep);
					else
						sleep = rnd.nextInt(400);

					System.out.println("out.getElapsed: " + out.getElapsed());
					System.out.println("max: " + sleep);

					out.setObjectiveTime(sleep);

					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					switch (out.getType()) {

					//check if someone allready reponded to the getchunk
					case "CHUNK":
						//check if someone allready asked to putchunk the same id
					case "PUTCHUNK":
						if(hash.containsKey(out.getId())){
							if(hash.get(out.getId()).getRepDegree()>0){
								System.out.println("repdegree>0");
								return;
							}
						}
						break;


					default:
						break;
					}


					com.send(out.getData());
					System.out.println("sent in: " + out.getElapsed());

					if(out.getType().equals("STORED")){
						Message m  = new Message( hash.get(out.getId()));
						
						Config.theirChunks.put(m.getId(), m.getChunk());

					}

				}
			}

		};

		th.start();
	}

	public static void sendRequestMessage(String type, Message temp,
			final ConcurrentHashMap<String, Message> hash, 
			final Communication comm) {

		final Message out_m;

		switch (type) {
		case "GETCHUNK":
		case "PUTCHUNK":
		case "REMOVED":
		case "DELETED":
			out_m = new Message(temp);
			out_m.setType(type);
			break;


		default:
			System.err.println("Wrong type to send a message");
			out_m = null;
			break;
		}		

		Thread th = new Thread(){
			Message out = out_m;
			Communication com = comm;
			@Override
			public void run() {

				super.run();

				Random rnd = new Random();				

				if(out != null){

					int sleep = rnd.nextInt(400);

					System.out.println("sending: ");
					System.out.println("max: " + sleep);

					out.setObjectiveTime(sleep);

					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}


					com.send(out.getData());
					System.out.println("sent in: " + out.getElapsed());

					out.setRepDegree(0);

					switch (out.getType()) {


					case "PUTCHUNK":	
						out.setType("STORED");

						hash.put(out.getId(), out);

						break;
					case "GETCHUNK":
						out.setType("CHUNK");

						hash.put(out.getId(), out);

						break;


					default:
						break;
					}
				}
			}

		};
		th.start();
	}

}
