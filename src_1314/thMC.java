import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class thMC implements Runnable {
	List<Chunk> stored;
	List<Message> to_save, current_repdegree;
	List<Message> mdb_in, mc_in, mc_out, mdr_out;

	Communication comm = null;


	public thMC(List<Message> _mdb_in,List<Message> _mc_in,List<Message> _mc_out,List<Message> _mdr_out,
			List<Chunk> _stored,List<Message> _to_save,List<Message> _current_repdegree){
		stored = _stored;
		to_save = _to_save;
		current_repdegree = _current_repdegree;
		mdb_in = _mdb_in;
		mdr_out = _mdr_out;
		mc_in = _mc_in;
		mc_out = _mc_out;
	}

	//set connection settings like group and port
	public void setCom(String _group, int _port){
		comm = new Communication();
		try {
			comm.Connect(_group,_port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1); //error
		}
	}

	public void run() {
		ArrayList<Message> toStore = new ArrayList<>();

		if(comm == null){
			System.err.println("should had set comm...");
			System.exit(-1);
		}

		while(true){
			boolean tosleep = false;

			/*
			 *  Receiving stuff
			 * 
			 */
			Message toStoreChunk=null;
			//check for received putchunks from MDB and puts in local array
			//to check if there is need to store.
			synchronized (mdb_in) {

				for(Message m: mdb_in){
					toStore.add(m);
					mdb_in.remove(m);
					break;
				}
			}



			//check if its time to send Store to respond to a PUTCHUNK
			for(Message m: toStore){				
				if(m.ready() && m.getElapsed() <= 400){
					m.messageType = "STORED";


					//					synchronized (current_repdegree) {
					//						for(Message x: current_repdegree){
					//							if(m.equals(x)){
					//								//Message.checkDegree returns false if currentRepDegree isnt enough
					//								if(!x.checkDegree()){
					//
					//									//make a message Store to send
					//									m.messageType = "STORED";
					//
					//
					//								} else {
					//									current_repdegree.remove(x);
					//									toStore.remove(m);
					//								}
					//							}
					//						}

				}

				if(m.messageType.equals("STORED")){
					//verifica se no stored já existe a chunk 
					synchronized (stored) {
						for(Chunk c:stored){
							if(!m.equalsChunk(c)){
								toStoreChunk=null;
							} else {
								toStoreChunk = m;
							}
						}
					}
					
					try {
						comm.send(m);
//						System.out.println("sent STORE for no:" + m.chunkNumber +
//								" " + m.fileId);

						//puts the chunk in a queue to save to file
						//only if it isnt already in stored
						if(toStoreChunk == null){
							synchronized (to_save) {
								to_save.add(m);
							}
						}else {
							System.out.println("not saving no:" + m.chunkNumber +
								" " + m.fileId);
						}
						toStore.remove(m);
					} catch (IOException e) {
						e.printStackTrace();

					}
					break;
				}
			} 



			//recebe algo do MC
			Message msg = null;
			try {
				msg = comm.receive();
				tosleep = true;
			} catch (IOException e) {
				//timeout
				msg = null;
			}

			//check what type of message is recieved
			if(msg != null){

				switch(msg.messageType){
				case "GETCHUNK":
				case "DELETE":
				case "REMOVED":
					
					synchronized (mc_in) {
						System.out.println("thMC received: " + msg.messageType);
						msg.startTime();
						mc_in.add(msg);
					}
					break;

				case "STORED":
					System.out.println("Recebi stored");
					synchronized (current_repdegree) {
						
						//check if we have the chunk waiting for stored
						//   note: may not be our putchunk, may be someone elses putchunk
						for(Message c: current_repdegree){

							if(c.equals(msg)){

								c.incrementRepDegree();

								//TODO debug
								System.out.println("currentRepDegree = " + c.currentRepDegree);

								break;
							}
						}

					}
					break;
				default:
				};
			}

			/*
			 * 
			 * Check list to see if there are things to be done
			 * 
			 */


			/*
			 *check if main wants to send GETCHUNK, DELETE or REMOVED or STORE
			 */

			Message ob = null;

			synchronized (mc_out) {

				for(Message c: mc_out){
					ob = c;
					mc_out.remove(c);
					break;

				}
			}

			if(ob != null){

				//If on mc_out, there is no need to check for validity.
				try {
					System.out.println("to send " + ob.messageType);
					comm.send(ob);
					System.out.println("SENDING: " + ob.messageType);


					ob.startTime();

					if(ob.messageType.equals("GETCHUNK")){
						//thMDR will deal with the reception of chunk 
						synchronized (mdr_out) {
							mdr_out.add(ob);
						}
					} else if(ob.messageType.equals("DELETE")){
						//TODO pensar no enhancement de enviar varias vezes...
					} 
				} catch (IOException e) {
					e.printStackTrace();
					System.err.print("Could not send requested message: " + ob.messageType 
							+ " " + ob.fileId +" " + ob.chunkNumber);
					synchronized (mc_out) {
						mc_out.add(ob);
					}
				}
			}

			if(tosleep){
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}//same as timout
			}

		}//while

	}
}
