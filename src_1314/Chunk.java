import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class Chunk implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final int size = 64000;
	public int repDegree;
	public int chunkNumber;
	public String fileID;
	int sizeChunk;
	byte[] body;
	File file;

	public Chunk(String fatherId, int chunkNumber,int repDegree, byte[] _body,  int sizeChunk){
		super();
		this.repDegree=repDegree;
		this.chunkNumber=chunkNumber;
		this.fileID=fatherId;
		this.sizeChunk=sizeChunk;
		this.body = _body;
	}

	// Getters and settters //
	public int getChunkNumber() {
		return chunkNumber;
	}

	public String getFatherID() {
		return fileID;
	}

	public int getRepDegree() {
		return repDegree;
	}

	public int getSize() {
		return size;
	}

	public void setChunkNumber(int chunkNumber) {
		this.chunkNumber = chunkNumber;
	}

	public void setFatherId(String fatherId) {
		this.fileID = fatherId;
	}

	public void setRepDegree(int repDegree) {
		this.repDegree = repDegree;
	}

	//override equals 
	@Override
	public boolean equals(Object c){
		if(!(c instanceof Chunk))
			return false;
		Chunk ch = (Chunk) c;
		return this.chunkNumber==ch.chunkNumber && this.fileID.equals(ch.fileID);
	}

	public void write(byte[] data, int lenght) throws IOException{
		file = new File ("chunks/" + fileID + "/" + chunkNumber);
		file.getParentFile().mkdirs();
		FileOutputStream outstream = new FileOutputStream(file);
		outstream.write(data, 0, lenght);
		outstream.close();

		serialize();
	}

	private void serialize() throws IOException, FileNotFoundException {
		File file2 = new File("chunks/" + this.fileID + '/'+ this.chunkNumber + ".ser"); 
		file2.getParentFile().mkdir();
		file2.createNewFile();
		FileOutputStream outStream2 = new FileOutputStream(file);
		ObjectOutputStream oOutputStream = new ObjectOutputStream(outStream2);
		oOutputStream.writeObject(this);
		oOutputStream.close();
		outStream2.close();
	}

	public void write() throws IOException  {
		file = new File("chunks/"+this.fileID + "/" +this.chunkNumber);
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);
		if(body!=null)
			out.write(this.body);
		out.close();
		serialize();
		
	}


	//TODO is only reading first chunk instead of chunknumber
	public int read() throws IOException{
		file = new File("chunks/"+this.fileID + "/" +this.chunkNumber);
		FileInputStream instream = new FileInputStream(file);
		body = new byte[sizeChunk];
		int bytesRead= instream.read(body);
		instream.close();
		return bytesRead;
	}
	
	//delete the file and if the parent directory is empty
	//    delete aswell
	public void delete(){
		file = new File("chunks/"+this.fileID + "/" +this.chunkNumber);
		file.delete();
		
		file = new File("chunks/"+this.fileID + "/" +this.chunkNumber+".ser");
		file.delete();
		
		File parentFolder = new File( file.getParent());
		if(parentFolder.isDirectory() ){
			
			if( parentFolder.list().length == 0 ){
				parentFolder.delete();
			}
		}
	}

}
