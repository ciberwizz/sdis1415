package main;

public class Message {

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

	public String toString(){

		String str = new String( fileId + " " + chunkNr );
		
		switch(type){

		case "PUTCHUNK":
			
			str = "PUTCHUNK " + str + " " + repDegree; //TODO + " " + chunk.getDados() 

			break;

		case "GETCHUNK":
			
			str = "GETCHUNK " + str;
			
			break;

		case "STORED":

			str = "STORED " + str;
			
			break;

		case "REMOVE":

			str = "REMOVE " + str;
			
			break;

		case "DELETE":

			str = "DELETE " + fileId;
			
			break;

		case "CHUNK":
			
			str = "CHUNK " + str; //TODO + " " + chunk.getDados();
			break;
		default:

			System.err.println("ERROR: wrong message type in Comunication class");

		}
		
		return str;
	}

}
