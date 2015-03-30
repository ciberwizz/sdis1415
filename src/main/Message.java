package main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;

public class Message {
	public static String CRLF = "\r\n";

	private String type = new String();
	private String fileId = new String();
	private int chunkNr;
	private int repDegree;
	private Chunk chunk;

	public Message(String _type, String _fileId, int _chunkNr, int _repDegree/*TODO , Chunk _chunk*/){
		type = _type;
		fileId = _fileId;
		chunkNr = _chunkNr;
		repDegree = _repDegree;
		//TODO chunk = _chunk;
	}

	public Message(String _type, Chunk ch){
		chunk = ch;

		type = _type;
		fileId = ch.getFileId();
		chunkNr = ch.getChunkNr();
		repDegree = ch.getRepDegree();

	}


	//parse what is received by communication class
	public Message(byte[] data){

		ByteArrayOutputStream sheader = new ByteArrayOutputStream();
		ByteArrayOutputStream sbody = new ByteArrayOutputStream();

		String header;
		byte[] body;

		byte[] bcrlf = CRLF.getBytes();

		int found = 0;

		for(int i = 0 ; i < data.length ; i++){


			//The header is before crlf + crlf wich are 4 chars
			if(found < 4) {
				if( data[i] == bcrlf[0] || data[i] == bcrlf[1] )
					found++;
			} else{
				//index of chunk body 
				found = i;
				break;
			}

		}

		//if found is 4 then its because message doesnt have a body
		if(found == 4){
			sheader.write(data, 0, data.length -4);
			body = new byte[0];
		} else {
			//there is a body

			sheader.write(data, 0, found -4);
			sbody.write(data, found, data.length - found);

			body = sbody.toByteArray();
		}

		header = new String(sheader.toByteArray());


		String[] temp = header.split(" ");

		this.type = temp[0];
		
		
		switch (type) {
		case "DELETE":

			this.fileId = temp[2];
			this.chunkNr = 0;	
			this.repDegree = 0;
			break;
			
		case "PUTCHUNK":
			
			this.fileId = temp[2];
			this.chunkNr = Integer.parseInt(temp[3]);
			this.repDegree = Integer.parseInt(temp[4]);
			
			break;
			
		default:

			this.fileId = temp[2];
			this.chunkNr = Integer.parseInt(temp[3]);
			this.repDegree = 0;

			break;
		}
		


		chunk = new Chunk( this.chunkNr, this.fileId, "", this.repDegree);

		if( body.length >0){
			chunk.setData(body);
		}

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

	public int getChunkNr() {
		return chunkNr;
	}

	public void setChunkNr(int chunkNr) {
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

	public byte[] getData(){

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			output.write(getHeader().getBytes());
			output.write(chunk.readFromFile());

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return output.toByteArray();

	}

}
