package main;

public class Chunk {
    private int chunkNr;
    private String fileId = new String();
    private String path = new String();
    private int repDegree;
    private byte[] data;

	public Chunk(int _chunkNr, String _fileId, String _path, int _repDegree){
        chunkNr = _chunkNr;
        fileId = _fileId;
        path = _path;
        repDegree = _repDegree;
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

    public void setFileId(int repDegree) {
        this.repDegree = repDegree;
    }
    
    public void setData(byte[] data) {
		this.data = data;
	}
    
    public byte[] getData(){
    	return this.data;
    }
    
    public void saveToFile(){
    	//TODO saveToFile(){
    }
    
    public byte[] readFromFile(){
    	//TODO readFromFile(){
    	return null;
    }
}
