import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

class Teste {


	public static void main(String argv[]) throws IOException,
	NoSuchAlgorithmException, InterruptedException {

		Scanner sc = new Scanner(System.in);
		int in;

		// ficheiro.joinFile();

		// current_repdegree used to count stored messages from MC to see if
		// there is need to send stuff 
		List<Message> current_repdegree = Collections
				.synchronizedList(new ArrayList<Message>());

		// used to save new chunks to file..
		List<Message> to_save = Collections
				.synchronizedList(new ArrayList<Message>());

		// stored is used to keep track of files and/or chunks
		List<Chunk> stored = Collections
				.synchronizedList(new ArrayList<Chunk>());

		List<Message> mdb_in = Collections
				.synchronizedList(new ArrayList<Message>());
		List<Message> mdb_out = Collections
				.synchronizedList(new ArrayList<Message>());
		List<Message> mdb_removed_check = Collections
				.synchronizedList(new ArrayList<Message>());



		// TODO check if there ir a need to create controller_in/out
		List<Message> mc_in = Collections
				.synchronizedList(new ArrayList<Message>());
		List<Message> mc_out = Collections
				.synchronizedList(new ArrayList<Message>());

		// mdr_in not needed..
		List<Message> mdr_out = Collections
				.synchronizedList(new ArrayList<Message>());

		//queue to delete chunks/files
		ArrayList<Chunk> to_delete = new ArrayList<>();


		ArrayList<Chunk> chunks;

		ArrayList<Files> fi = new ArrayList<Files>();

		thMC tc = new thMC(mdb_in, mc_in, mc_out, mdr_out, stored, to_save,
				current_repdegree);
		thMDR tmdr = new thMDR(stored, mdr_out, mc_in, mc_out);
		thMDB tmdb = new thMDB(stored, current_repdegree, mdb_in, mdb_out,mdb_removed_check);
		thController tcontroller = new thController( current_repdegree, to_save, 
				stored, mdb_in, mdb_out,mdb_removed_check, mc_in, mc_out, mdr_out);

		tc.setCom("239.1.1.194", 6781);
		tmdr.setCom("239.1.1.196", 6781);
		tmdb.setCom("239.1.1.195", 6781);

		Thread thmdb = new Thread(tmdb, "MDB");
		Thread thmc = new Thread(tc, "MC");
		Thread thmdr = new Thread(tmdr, "MDR");
		Thread thcontroller = new Thread(tcontroller,"CONTROLLER"); 

		thmc.start();
		thmdr.start();
		thmdb.start();
		thcontroller.start();

		while(true) {
			System.out.println("1----------- Backup");
			System.out.println("2----------- Restore");
			System.out.println("3----------- Delete");
			System.out.println("4----------- Space Reclaim");
			in = sc.nextInt();

			switch (in) {
			case 1:

				File fn = null;
				do{
					System.out.println("Qual a path do ficheiro a fazer backup?");

					String no = sc.nextLine();

					fn = new File(no);

				}while(!fn.isFile());


				System.out.println("Qual o replication degree desejado?");
				int rep =sc.nextInt();


				Files ficheiro = new Files(fn,fn.getName(), rep);

				fi.add(ficheiro);
				chunks = ficheiro.divideFile();
				stored.addAll(chunks);

				for (int i = 0; i < chunks.size(); i++) {

					Message ch_put = new Message("PUTCHUNK", "1.0",
							chunks.get(i).fileID, chunks.get(i).chunkNumber,
							chunks.get(i).repDegree, chunks.get(i));

					synchronized (mdb_out) {
						mdb_out.add(ch_put);
					}
					Thread.sleep(500);

				}

				break;
			case 2:
				// restore()
				System.out.println("Escolha ficheiro");
				for (int i = 0; i < fi.size(); i++) {
					System.out.println(i + " ------ " + fi.get(i).id);
				}
				int in2 = sc.nextInt();
				int nchunks = fi.get(in2).chunkers.size();

				for (int i = 0; i < nchunks; i++) {
					Message st = new Message("GETCHUNK", "1.0",
							fi.get(in2).id, i, 1);

					synchronized (mc_out) {
						mc_out.add(st);
					}
				}

				break;
			case 3:
				// delete
				System.out.println("Escolha ficheiro");
				for (int i = 0; i < stored.size(); i++) {
					System.out.println(i + " ------ " + stored.get(i).fileID);
				}
				int in3 = sc.nextInt();
				String id = stored.get(in3).fileID;
				Message todel = new Message("DELETE", "1.0", id, 0, -1);

				//to send
				synchronized (mc_out) {
					mc_out.add(todel);
				}

				//to delete
				synchronized (mc_in) {
					mc_in.add(new Message(todel));
				}

				break;
			case 4:
				// space reclaim
				System.out.println("Space reclaim");
				System.out.println("Remove all files? - 1");
				System.out.println("Cancel - 2");
				int rm = sc.nextInt();
				
				if(rm == 1){
					
					synchronized (stored) {
						for(Chunk s: stored){
							Message torm = new Message("REMOVED","1.0",s.fileID,s.chunkNumber,1);
							synchronized (mc_out) {
								mc_out.add(torm);
							}
							s.delete();
						}
						stored.clear();
					}
					
				}
				break;
			case 5:
				return;

			default:
				System.out.println("please insert the right number");
				break;
			}


		}


	}



}

