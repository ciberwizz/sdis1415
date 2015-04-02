package main;

import java.io.ObjectOutputStream.PutField;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Distributed {

	public static Config config = new Config();

	public static void main(String[] args) {

		ConcurrentLinkedQueue<Message> inMC = new ConcurrentLinkedQueue<Message>();
		ConcurrentLinkedQueue<Message> inMDB = new ConcurrentLinkedQueue<Message>();
		ConcurrentLinkedQueue<Message> inMDR = new ConcurrentLinkedQueue<Message>();

		//responses for us
		ConcurrentHashMap<String, Message> expectChunk = new ConcurrentHashMap<String, Message>();
		ConcurrentHashMap<String, Message> expectStore = new ConcurrentHashMap<String, Message>();

		//for us to send
		ConcurrentHashMap<String, Message> toSendChunk = new ConcurrentHashMap<String, Message>();
		ConcurrentHashMap<String, Message> toSendStore = new ConcurrentHashMap<String, Message>();

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
					
					if(config.chunksOfOurFiles.contains(id)){
						
						t = new Message("CHUNK", config.chunksOfOurFiles.get(id));
						
						toSendChunk.put(id, temp);
						
						sendMessage("CHUNK",t,toSendChunk);
					
					
					} else 
						if(config.theirChunks.containsKey(id)){
							
							t = new Message("CHUNK", config.theirChunks.get(id));
							
							toSendChunk.put(id, temp);
							
							sendMessage("CHUNK",t,toSendChunk);
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
					sendMessage("STORED",temp,toSendStore);

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

		}



	}

	private static void sendMessage(String type, Message temp,
			ConcurrentHashMap<String, Message> idChunk) {
		// TODO criarThread que envia o chunk em 0-400ms
		// 		só se não tiver sido enviado. verificar o toSendChunk
		//TODO fazer um copy clean, se nao ao mudar o type tambem vamos mudar no concurrenthash

		Message out_m = null;
		
		String id = temp.getFileId() + "_" + temp.getChunkNr();
		
		switch (type) {
		case "STORED":

			//easy way to make a clean copy xD

			//clean copy
			out_m = new Message(temp.getHeader().getBytes());

			out_m.setType(type);

			break;

		case "CHUNK":
			out_m = new Message(temp.getData());
			out_m.setType(type);
			break;


		default:
			break;
		}

		Thread th = new Thread(){
			
			@Override
			public void run() {
				
				super.run();

//				if(out != null){
////					out.getData();
//
//				}
			}

		};

		th.start();
	}

}
