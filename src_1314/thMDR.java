import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.SliderUI;

public class thMDR implements Runnable{

	List<Chunk> stored;
	List<Message> mdr_out, mc_in, mc_out;

	Communication comm = null;

	public  thMDR( List<Chunk> _stored,	List<Message> _mdr_out, 
			List<Message> _mc_in, List<Message> _mc_out) {

		stored = _stored;
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


	public void run(){
		ArrayList<Message> to_respond = new ArrayList<>();

		if(comm == null){
			System.err.println("should had set comm...");
			System.exit(-1);
		}

		while(true){

			boolean tosleep = false;


			/*
			 * check if we have any GETCHUNK to respondo to
			 */

			synchronized (mc_in) {
				for(Message m: mc_in){
					if(m.messageType.equals("GETCHUNK")){
						Message x = new Message(m);
						x.startTime();
						to_respond.add(x);
						mc_in.remove(m);
						break;
					}
				}					
			}


			//Only receives CHUNK
			Message msg;
			try {
				msg = comm.receive();
				tosleep = true;
				//				System.out.println("MDR RECEIVED: CHUNK");
			} catch (IOException e) {
				//timeout
				msg = null;
				//System.err.println("MDR receive error: "+e.getMessage());
			}

			boolean our_getchunk = true;

			/*
			 * check if someoneelse responded 
			 * if not
			 *    check if we are ready to send a CHUNK
			 */
			for(Message m: to_respond){
				boolean tobreak = false;
				//if msg!= null
				//	it means that someone else responded. so we dont need
				if(msg!=null){
					if(m.equals(msg)){
						our_getchunk = false;
						to_respond.remove(m);
						break;
					}
				}

				//				//no one responded, so we need to see if we are ready to send the chunk
				//				System.out.println("chunk no:"+m.chunkNumber+" is ready to send? " + 
				//						m.ready() +" elaplsed: " + m.getElapsed());
				//The chunk we want to send
				if(m.ready() && m.getElapsed() <= 401){

					// check if we have the chunk and send it
					synchronized (stored) {
						for(Chunk  ck: stored){
							if(m.equalsChunk(ck)){
								Message x = new Message(m);
								x.messageType = "CHUNK";
								x.chunk = ck;
								try {
									comm.send(x);			
									System.out.println("sending chunk");
									to_respond.remove(m);
									tobreak = true;
								} catch (IOException e) {
									System.err.println("unable to send stored..");
									//									m.messageType = "GETCHUNK";
								}
								break;
							}
						}
					}

				} else if(m.getElapsed()>600){
					to_respond.remove(m);
					break;
				}

				if(tobreak)
					break;
			}


			/*
			 * check if received CHUNK for our GETCHUNK
			 */

			if(msg!=null && our_getchunk){			

				synchronized (mdr_out) {
					for(Message m: mdr_out){
						//if there is one GETCHUNK that is ours
						//save the chunk and let main/thController deal with it
						if( m.equals(msg) && m.getElapsed() <= 401 ){
							m.chunk = msg.chunk;
							System.out.println("RECEBEMOS O CHUNK Q QUERIAMOS");
							break;
						} 
					}
				}

			} else {
				Message mx = null;
				synchronized (mdr_out) {
					for(Message m: mdr_out){
						if(!m.hasStarted){
							m.startTime();
						} else {
							if(m.getElapsed() > 401 ){
								//in case our getchunk hasnt received the chunk
								mx = m;
								mdr_out.remove(m);
								break;
							}
						}
					}
				}
				
				if(mx != null){
					synchronized (mc_out) {
						mc_out.add(mx);
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

		}

	}

}
