import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class Files implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	String id;
	int repDegree;
	long size;
	int numberOfChunks;
	ArrayList<Chunk> chunkers;
	File file;
	//Getters and setters //

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRepDegree() {
		return repDegree;
	}

	public void setRepDegree(int repDegree) {
		this.repDegree = repDegree;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public int getNumberOfChunks() {
		return numberOfChunks;
	}

	public void setNumberOfChunks(int numberOfChunks) {
		this.numberOfChunks = numberOfChunks;
	}

	public ArrayList<Chunk> getChunkers() {
		return chunkers;
	}

	public void setChunkers(ArrayList<Chunk> chunkers) {
		this.chunkers = chunkers;
	}

	//  //

	public Files(File file, String name, int repDegree){
		this.name=name;
		this.repDegree=repDegree;
		size=0;
		numberOfChunks=0;
		chunkers= new ArrayList<Chunk>();
		this.file=file;
		
	}

	public Files(String name, int repDegree){
		this.name=name;
		this.repDegree=repDegree;
		size=0;
		numberOfChunks=0;
		chunkers= new ArrayList<Chunk>();
	}

	public String calculateFileId() throws NoSuchAlgorithmException, IOException{
		 File file = new File(name);
		FileInputStream inStream = new FileInputStream(file);
		byte[] data = new byte[Chunk.size];
		String identifier = file.lastModified() + name;
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(identifier.getBytes());
		inStream.read(data);
		md.update(data);
		inStream.close();    
		return bytArrayToHex(md.digest());
	}
	
	
	//Function to convert byte[] to String
	public String bytArrayToHex(byte[] a) {
		   StringBuilder sb = new StringBuilder();
		   for(byte b: a)
		      sb.append(String.format("%02x", b&0xff));
		   return sb.toString();
		}
	
	public ArrayList<Chunk> divideFile() throws IOException, NoSuchAlgorithmException{
		 File file = new File(name);
		Chunk tempChunk;
		int bytesRead = 0; //bytes read for each chunk
		FileInputStream inStream = new FileInputStream(file);
		size = file.length();
		numberOfChunks= (int) Math.ceil((double)size/Chunk.size);
		long totalBytesRead=0;
		//long totalBytesRemaining=size;			
		byte[] data = new byte[Chunk.size];
		this.id=calculateFileId();//name atribution
		
		System.out.println("Tamanho ficheiro: " + size);
		System.out.println("Tamanho chunk: " + Chunk.size);
		System.out.println("Numero de chunks: " + numberOfChunks);
	
		int i=0;
		for(; i<numberOfChunks; i++){
			//totalBytesRemaining=size-totalBytesRead;
			
			bytesRead = inStream.read(data);
			//System.out.println("\n\n\nDATA= " );
			//String sta = new String(data,0,data.length);
			//System.out.println(sta+"\n\n");
			tempChunk = new Chunk(id, i, repDegree, data.clone(),bytesRead);
			tempChunk.write(data, bytesRead);
			chunkers.add(tempChunk);

			if(bytesRead > 0){//If bytes read not empty
				totalBytesRead += bytesRead;
			}
			
		}
		
		//If the last chunk has 64kB, add another one empty
		if (bytesRead==Chunk.size) 
		{
			tempChunk = new Chunk(id, i, repDegree, null ,0);
			chunkers.add(tempChunk);
			tempChunk.write(data, 0);
		}
		
		inStream.close();

		//serialization
		File files = new File(id + ".ser"); // file id
		FileOutputStream fileOut = new FileOutputStream(files);  
		ObjectOutputStream outStream = new ObjectOutputStream(fileOut);  
		outStream.writeObject(this);  
		outStream.close();  
		fileOut.close();  
		
		return chunkers;
	}
	
	
	public void joinFile() throws IOException{
		
		File tempFile;
		byte[] data = new byte[Chunk.size];
		int bytesRead;

		if(file.createNewFile()){
			FileInputStream inStream = null;
			FileOutputStream outStream = new FileOutputStream(file);

			for(int i=0; i<numberOfChunks; i++){
				tempFile = new File("chunks/" + id + "/" + i );
				inStream = new FileInputStream(tempFile);
				bytesRead=inStream.read(data);
				if (bytesRead == -1) 
					break;

				outStream.write(data, 0, bytesRead);
			}

			inStream.close();
			outStream.close();		
		}else{
			System.out.println("The file with this ID already exists, soz!");
		}
	}





}
