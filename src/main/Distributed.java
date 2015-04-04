package main;

import java.io.ObjectOutputStream.PutField;
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
	public static ConcurrentHashMap<String, Message> toSendStore = new ConcurrentHashMap<String, Message>();


	public static void main(String[] args) {

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

		//TODO start threads


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

						sendMessage("CHUNK",t,toSendChunk, new Communication(chMdr, chMdrPort));


					} else 
						if(Config.theirChunks.containsKey(id)){

							t = new Message("CHUNK", Config.theirChunks.get(id));

							temp.setRepDegree(0);
							toSendChunk.put(id, temp);

							sendMessage("CHUNK",t,toSendChunk, new Communication(chMdr, chMdrPort));
						}
					break;

				case "DELETE":

					config.delete(temp);

					break;

				case "REMOVED":
					config.decRepDegree(temp);

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
					temp.setRepDegree(0);
					toSendStore.put(id,temp);
					sendMessage("STORED",temp,toSendStore, new Communication(chMc, chMcPort));

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
			//TODO limpar os pedidos que foram á mais de 400ms

			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {

			}

		}



	}

	private static void sendMessage(String type, Message temp,
			final ConcurrentHashMap<String, Message> hash, 
			final Communication comm) {
		// TODO criarThread que envia o chunk em 0-400ms
		// 		só se não tiver sido enviado. verificar o toSendChunk

		final Message out_m;

		final String id = temp.getFileId() + "_" + temp.getChunkNr();

		switch (type) {
		case "STORED":

			//easy way to make a clean copy xD

			//clean copy
			out_m = new Message(temp);

			out_m.setType(type);

			break;

		case "CHUNK":
			out_m = new Message(temp);
			out_m.setType(type);
			break;


		default:

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
					sleep = rnd.nextInt(sleep);

					System.out.println("out.getElapsed: " + out.getElapsed());
					System.out.println("max: " + sleep);

					out.setObjectiveTime(sleep);

					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					//check if someone allready reponded to the getchunk
					if(out.getType().equals("CHUNK")){
						if(hash.containsKey(out.getId())){
							if(hash.get(out.getId()).getRepDegree()>0){
								return;
							}
						}
					}

					com.send(out.getData());
					System.out.println("sent in: " + out.getElapsed());

					switch(out.getType()){

					case "STORED":
						Message m  = new Message( hash.get(out.getId()));
						
						Config.theirChunks.put(m.getId(), m.getChunk());
						
						break;
						
					case "CHUNK":
						
						
						
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
