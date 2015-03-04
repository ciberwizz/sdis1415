import java.io.IOException;
import java.util.List;


public class thMDB implements Runnable{

	List<Chunk> stored;
	List<Message> current_repdegree;
	List<Message> mdb_in, mdb_out, mdb_removed_check;

	Communication comm = null;
	
	public  thMDB( List<Chunk> _stored, List<Message> _current_repdegree,
			List<Message> _mdb_in, List<Message> _mdb_out, List<Message> _mdb_removed_check) {
		
		stored = _stored;
		current_repdegree = _current_repdegree;
		mdb_in = _mdb_in;
		mdb_out = _mdb_out;
		mdb_removed_check = _mdb_removed_check;
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
		
		if(comm == null){
			System.err.println("should had set comm...");
			System.exit(-1);
		}
		
		while(true){
			boolean tosleep = false;

			/*
			 * check if there is to receive PUTCHUNk
			 */
			
			Message msg;
			try {
				msg = comm.receive();
				tosleep = true;
			} catch (IOException e) {
				//timeout
				//System.out.println("timeout");
				msg = null;
			}
			
			if(msg != null) {
				
				if(msg.messageType.equals("PUTCHUNK")){
					System.out.println("RECEIVED PUTCHUNK no:" + msg.chunkNumber +" id: " + msg.fileId);
					msg.startTime();
					synchronized (mdb_in) {
						mdb_in.add(msg);
					}
					synchronized (mdb_removed_check) {
						mdb_removed_check.add(new Message(msg));
					}

				}
			}
			
			/*
			 * check if there are putchunks to send
			 */
			msg = null;
			
			synchronized (mdb_out) {
				for(Message m: mdb_out){
					msg = m;
					mdb_out.remove(m);
					break;
				}
			}
			if(msg!=null){

				try {
					comm.send(msg);
					System.out.println("MDB is sending: putchunk");
					msg.startTime();
					msg.ourMessage = true;
					synchronized (current_repdegree) {
						current_repdegree.add(msg);
					}				
				} catch (IOException e) {
					System.err.println("thMDB faild to send ");
					synchronized (mdb_out) {
						mdb_out.add(msg);
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
