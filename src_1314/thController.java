import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.rowset.spi.SyncResolver;


public class thController implements Runnable{
	// current_repdegree used to count stored messages from MC to see if
	// there is need to send stuff
	List<Message> current_repdegree;

	// used to save new chunks to file..
	List<Message> to_save;

	// stored is used to keep track of files and/or chunks
	List<Chunk> stored;

	List<Message> mdb_in;
	List<Message> mdb_out;
	List<Message> mdb_removed_check;

	List<Message> mc_in ;
	List<Message> mc_out;

	// mdr_in not needed..
	List<Message> mdr_out;

	//queue to delete chunks/files
	ArrayList<Chunk> to_delete = new ArrayList<>();

	public thController( List<Message> _current_repdegree, List<Message> _to_save,
			List<Chunk> _stored, List<Message> _mdb_in, List<Message> _mdb_out,
			List<Message> _mdb_removed_check, List<Message> _mc_in, List<Message> _mc_out, 
			List<Message> _mdr_out){

		current_repdegree = _current_repdegree;

		to_save = _to_save;

		stored = _stored;

		mdb_in = _mdb_in;
		mdb_out = _mdb_out;
		mdb_removed_check = _mdb_removed_check;

		mc_in = _mc_in;
		mc_out = _mc_out;

		mdr_out = _mdr_out;

	}

	public void run() {
		while(true) {
			resendPutchunk();

			saveChunk();


			updateChunk();


			deleteFileId();

			removed();

			try {
				// 20 the same as timeout of comunication
				Thread.sleep(20); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}

	}

	private void deleteFileId() {
		/*
		 * DELETE
		 */
		//check if there is something to delete
		String to_delete_id = null;

		synchronized (mc_in) {
			for(Message de: mc_in){
				if(de.messageType.equals("DELETE")){
					to_delete_id = de.fileId;
					mc_in.remove(de);
					break;
				}
			}

		}

		//add all chunks with the specified id to the delete queue
		if(to_delete_id != null){
			synchronized (stored) {
				for(Chunk ckde: stored) {
					if(ckde.fileID.equals(to_delete_id)){
						to_delete.add(ckde);					
					}
				}
			}
		}

		//delete chunks in the queue
		//delete one at a time, so it wont block the thread for too long
		for(Chunk ck: to_delete){
			ck.delete();

			synchronized (stored) {
				stored.remove(ck);
			}
			to_delete.remove(ck);
			break;
		}
	}

	private void updateChunk() {
		/*
		 * GETCHUNK/CHUNK
		 */
		// check to see if we received a chunk
		Message msg = null;
		Chunk get_ch = null;
		synchronized (mdr_out) {
			for (Message x : mdr_out) {
				if (x.chunkSet()) {
					get_ch = x.chunk;
					msg = x;
					mdr_out.remove(x);
					break;
				}
			}
		}

		// save chunk.body to the chunk in stored array
		if (get_ch != null) {
			synchronized (stored) {
				for (Chunk ch : stored) {
					if (ch.equals(get_ch)) {
						try {

							//copy body and write to file
							ch.body = get_ch.body.clone();
							ch.write();

						} catch (IOException e) {
							e.printStackTrace();
							System.err.println("unable to write: no:" + msg.chunkNumber 
									+ "fileid:" + msg.fileId );

							//retry next cycle
							synchronized (mdr_out) {
								mdr_out.add(msg);
							}

						}
						break;
					}
				}
			}
		}
	}

	private void saveChunk() {
		//check if there is anything to save to file from to_save
		Message msg_to_save = null;
		synchronized (to_save) {
			for(Message ts: to_save){					
				msg_to_save = ts;
				to_save.remove(ts);
				break;
			}
		}

		//save to store and file
		if(msg_to_save!=null){
			try {
				msg_to_save.chunk.write();

				synchronized (stored) {
					stored.add(msg_to_save.chunk);
				}
				System.out.println("Saving chunk! (Não tem que ser isto)");

			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("unable o write chunk: no:" + msg_to_save.chunkNumber 
						+ "fileid:" + msg_to_save.fileId );

				//retry
				synchronized (to_save) {
					to_save.add(msg_to_save);
				}
			}
		}
	}

	// when sending a putchunk
	// if after 400ms it did not receive enough stored to match repdegree
	// resend
	private void resendPutchunk() {
		Message msg = null;
		synchronized (current_repdegree) {
			for (Message ms : current_repdegree) {
				if (ms.ourMessage) {
					// If it has already been 400ms and not received enough
					// STORED
					// Resend
					if (ms.getElapsed() > 400 + (400*ms.retry)) {
						if (ms.repDegree <= ms.currentRepDegree) {
							System.out.println("PUTCHUNK !! SUCCESS: "
									+ ms.fileId + " " + ms.chunkNumber);
							current_repdegree.remove(ms);
							break;
						} else {
							if(ms.retry > 4){
								System.out.println("PUTCHUNK: too much retries no:" + 
										ms.chunkNumber + " " + ms.fileId);
								msg=null;
							} else{
								msg = ms;
								msg.retry++;
								msg.currentRepDegree = 0;
								msg.startTime();
							}
							current_repdegree.remove(ms);
							System.out.println("currRepDegree < repDegree");
							break;
						}
					}
				}
			}
		}


		// resend putchunk
		// TODO think about limit of tries
		if (msg != null) {
			synchronized (mdb_out) {
				mdb_out.add(msg);
			}
		}
	}

	public void removed(){
		Message put_out = null;
		Message check = null;

		//check if there is something to be removed
		synchronized (mc_in) {
			for( Message m: mc_in){
				if(m.messageType.equals("REMOVED")){
					//start time to see if we send putchunk
					if(!m.hasStarted){
						m.startTime();
						//TODO CHECK IF EXISTS
						check = m;
						break;
					} else {
						//if ready to send putchunk
						if( m.ready()){
							put_out = m;
							mc_in.remove(m);
							break;
						}
					}
				}
			}
		}

		//send putchunk cause its ready
		if(put_out != null){
			synchronized (mdb_out) {
				mdb_out.add(put_out);
			}
		}

		//check if we have chunk
		//if not remove. cant send putchunk if we dont have it...
		if(check != null){
			Message toremove = null;
			synchronized (stored) {
				if(!stored.contains(check)){
					toremove = check;
				}
			}

			if(toremove != null){
				synchronized (mc_in) {
					mc_in.remove(toremove);
				}
			}
		}


		//in List<Message> mdb_removed_check there are
		//copies of putchunk so we can see if a putchunk
		//could be a response to a removed.
		synchronized (mdb_removed_check) {
			synchronized (mc_in) {

				for(Message x: mdb_removed_check){
					int t = mc_in.indexOf(x);
					if(t!=-1){
						if(mc_in.get(t).messageType.equals("REMOVED")){
							mc_in.remove(t);
						}
					}

				}
				mdb_removed_check.clear();
			}
		}

	}

}
