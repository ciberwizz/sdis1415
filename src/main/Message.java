package main;

public class Message {
	public static String CRLF = "\r\n";
	
	private String type = new String();
	private String fileId = new String();
	private String chunkNr = new String();
	private int repDegree;
	//TODO private Chunk chunk;

	public Message(String _type, String _fileId, String _chunkNr, int _repDegree/*TODO , Chunk _chunk*/){
		type = _type;
		fileId = _fileId;
		chunkNr = _chunkNr;
		repDegree = _repDegree;
		//TODO chunk = _chunk;
	}
	
	
	//parse what is received by communication class
	public Message(byte[] data){
		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getChunkNr() {
		return chunkNr;
	}

	public void setChunkNr(String chunkNr) {
		this.chunkNr = chunkNr;
	}

	public int getRepDegree() {
		return repDegree;
	}

	public void setRepDegree(int repDegree) {
		this.repDegree = repDegree;
	}

	public String getHeader() throws Exception{

		String str = new String( fileId + " " + chunkNr );
		
		switch(type){

		case "PUTCHUNK":
			
			str = "PUTCHUNK " + "1.0 " + str + " " + repDegree + " " + CRLF + CRLF; //TODO + chunk.getDados() 

			break;

		case "GETCHUNK":
		case "STORED":
		case "REMOVED":

			str = type + " " + "1.0 " + str + " " + CRLF + CRLF;
			
			break;

		case "DELETE":

			str = "DELETE " + "1.0 " + fileId + " " + CRLF + CRLF;
			
			break;

		case "CHUNK":
			
			str = "CHUNK " + "1.0 " + str + " " + CRLF + CRLF; //TODO + chunk.getDados();
			break;
		default:

			System.err.println("ERROR: wrong message type in Comunication class");
			throw new Exception("ERROR: wrong message type in Comunication class");

		}
		
		return str;
	}

}
