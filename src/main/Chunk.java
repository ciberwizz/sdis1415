package main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Chunk {
	private int chunkNr;
	private String fileId = new String();
	private String path = new String();
	private int repDegree;
	private int objectiveRepDegree;
	private byte[] data;

	public Chunk(){}

	public Chunk(int _chunkNr, String _fileId, String _path, int _repDegree, int _objectiveRepDegree, byte[] _data){
		chunkNr = _chunkNr;
		fileId = _fileId;
		path = _path;
		repDegree = _repDegree;
		objectiveRepDegree = _objectiveRepDegree;
		data = _data;
	}

	public Chunk(int _chunkNr, String _fileId, String _path, int _repDegree){
		chunkNr = _chunkNr;
		fileId = _fileId;
		path = _path;
		repDegree = _repDegree;
		objectiveRepDegree = _repDegree;
		data = null;
	}

	public int getChunkNr() {
		return chunkNr;
	}

	public void setChunkNr(int chunkNr) {
		this.chunkNr = chunkNr;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getRepDegree() {
		return repDegree;
	}

	public void setRepDegree(int repDegree) {
		this.repDegree = repDegree;
	}

	public int getObjectiveRepDegree() {
		return objectiveRepDegree;
	}

	public void setObjectiveRepDegree(int objectiveRepDegree) {
		this.objectiveRepDegree = objectiveRepDegree;
	}

	public void setFileId(int repDegree) {
		this.repDegree = repDegree;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData(){
		return this.data;
	}

	public void decRepDegree(){
		repDegree--;
	}

	public void incRepDegree() {
		repDegree++;		
	}


	public String getId(){
		return getFileId() + "_" + getChunkNr();
	}

	public void saveToFile(){
		if(data != null){
			path = "data/chunks/"+getId();

			FileOutputStream out;
			try {
				out = new FileOutputStream(path);

				out.write(data);
				out.flush();
				out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public byte[] readFromFile(){
		//TODO readFromFile(){

		if(data != null){
			return data;
		} else {

			if(path.equals("")){

				path = "data/chunks/"+getId();
			}


			try {
				FileInputStream in = new FileInputStream(path);

				byte[] b = new byte[(int) in.getChannel().size()];

				in.read(b);

				in.close();

				return b;

			} catch (IOException e) {
				e.printStackTrace();
			}



			return null;

		}
	}

}
