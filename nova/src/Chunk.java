public class Chunk {
    private String chunkId = new String();
    private String fileId = new String();
    private String path = new String();
    private int repDegree;


   /* public Chunk(String _chunkId, String _fileId, String _path, int _repDegree){
        chunkId = _chunkId;
        fileId = _fileId;
        path = _path;
        repDegree = _repDegree;
    }*/

	public String getChunkId() {
		return chunkId;
	}

	public void setChunkId(String chunkId) {
		this.chunkId = chunkId;
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
}
