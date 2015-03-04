import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ThreadLocalRandom;


public class Message {
	public String messageType;
	public String version;
	public String fileId;
	public int chunkNumber;
	public int repDegree;
	byte[] bytes = new byte[] { (byte)0xD, (byte)0xA}; 
	public String CRLF;
	byte[]chunkData;
	public Chunk chunk;
	public int currentRepDegree = 0;
	public boolean ourMessage = false;
	public boolean hasStarted = false;
	public int retry = 0;

	//Copy the contents of oldMessage to a new one
	public Message(Message oldMessage){
		this.messageType=oldMessage.messageType;
		this.version=oldMessage.version;
		this.fileId=oldMessage.fileId;
		this.chunkNumber=oldMessage.chunkNumber;
		this.repDegree=oldMessage.repDegree;
		this.chunkData=null;
		this.chunk=null;
		start=-1;
		objective=-1;
		try {
			CRLF= new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Message(String messageType, String version, String fileId, int chunkNumber, int repDegree, Chunk chunk){
		this.messageType=messageType;
		this.version=version;
		this.fileId=fileId;
		this.chunkNumber=chunkNumber;
		this.repDegree=repDegree;
		this.chunk=chunk;
		this.chunkData = this.chunk.body;
		start=-1;
		objective=-1;
		try {
			CRLF= new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public Message(String messageType, String version, String fileId, int chunkNumber, int repDegree){
		this.messageType=messageType;
		this.version=version;
		this.fileId=fileId;
		this.chunkNumber=chunkNumber;
		this.repDegree=repDegree;
		start=-1;
		objective=-1;
		chunk=null;
		try {
			CRLF= new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
		
	

	//Função para separar a mensagem em todos os campos necessários.
	public Message(byte[] info) throws IOException{
		CRLF= new String(bytes, "UTF-8");
		String m = new String(info, "UTF-8");
		String[] data=m.split(" ");

		messageType = data[0];

		switch(messageType){

		case "PUTCHUNK": 
			version=data[1];
			fileId=data[2];
			chunkNumber=Integer.parseInt(data[3]);
			String[] repdegree_and_body = data[4].split(CRLF+CRLF);
			repDegree=Integer.parseInt(repdegree_and_body[0]);
			this.chunkData=repdegree_and_body[1].getBytes();
			this.chunk = new Chunk(fileId, chunkNumber, repDegree ,chunkData, chunkData.length);

			break;

		case "STORED":
			version=data[1];
			fileId=data[2];
			chunkNumber=Integer.parseInt(data[3].split(CRLF+CRLF)[0]);
			break;

		case "GETCHUNK":
			version=data[1];
			fileId=data[2];
			chunkNumber=Integer.parseInt(data[3].split(CRLF+CRLF)[0]);
			break;

		case "CHUNK" :
			version=data[1];
			fileId=data[2];
			String[] all = data[3].split(CRLF+CRLF);
			chunkNumber=Integer.parseInt(all[0]);
			chunkData=all[1].getBytes();
			this.chunk = new Chunk(fileId, chunkNumber, -1 ,chunkData, chunkData.length);
			break;

		case "DELETE":
			fileId=data[1].split(CRLF+CRLF)[0];
			break;

		case "REMOVED":
			version=data[1];
			fileId=data[2];
			chunkNumber=Integer.parseInt(data[3].split(CRLF+CRLF)[0]);
			break;

		/*List<String> data = Arrays.asList(m.split("\\s"));
		messageType = data.get(0);

		switch(messageType){

		case "PUTCHUNK": 
			version=data.get(1);
			fileId=data.get(2);
			chunkNumber=Integer.parseInt(data.get(3));
			String[] repdegree_and_body = data.get(4).split(CRLF+CRLF);
			repDegree=Integer.parseInt(data.get(4));//repdegree_and_body[0]);
			this.chunkData=data.get(8).getBytes();//repdegree_and_body[1].getBytes();
			this.chunk = new Chunk(fileId, chunkNumber, repDegree ,chunkData, chunkData.length);

			break;

		case "STORED":
			version=data.get(1);
			fileId=data.get(2);
			chunkNumber=Integer.parseInt(data.get(3));
			break;

		case "GETCHUNK":
			version=data.get(1);
			fileId=data.get(2);
			chunkNumber=Integer.parseInt(data.get(3));
			break;

		case "CHUNK" :
			version=data.get(1);
			fileId=data.get(2);
			String[] all = data.get(3).split(CRLF+CRLF);
			chunkNumber=Integer.parseInt(all[0]);
			chunkData=all[1].getBytes();
			this.chunk = new Chunk(fileId, chunkNumber, -1 ,chunkData, chunkData.length);
			break;

		case "DELETE":
			fileId=data.get(1);
			break;

		case "REMOVED":
			version=data.get(1);
			fileId=data.get(2);
			chunkNumber=Integer.parseInt(data.get(3));
			break;
		*/}

	}

	//Attributes and methods to control time //
	private long start;
	private long objective;

	public void startTime(){
		hasStarted = true;
		start = System.currentTimeMillis();
		objective= ThreadLocalRandom.current().nextInt(0, 400);
	}

	public long getElapsed() {
		return System.currentTimeMillis() - start;
	}

	public boolean ready(){
		if(this.getElapsed() >= objective)
			return true;
		else 
			return false;
	}
	// // 

	//Checks if chunk is set or not
	public boolean chunkSet(){
		if(chunk!=null)
			return true;
		return false;
	}
	
	
	//Increments repDegree
	public int incrementRepDegree(){
		currentRepDegree+=1;
		return currentRepDegree;
	}

	//Check if the current replication degree is the supposed
	public boolean checkDegree(){
		if(currentRepDegree< repDegree)
			return false;
		else
			return true;
	}


	//override equals (chunkno e fileID)
	public boolean equals(Object other){
		if(!(other instanceof Message))
			return false;
		Message m = (Message) other;

		boolean b = this.chunkNumber==m.chunkNumber && this.fileId.equals(m.fileId);
		
		return b;
	}


	public boolean equalsChunk(Chunk c){
		if(c == null)
			return false;
		
		return this.chunkNumber==c.chunkNumber && this.fileId.equals(c.fileID);
	}


	public byte[] createMessage() throws IOException {
		String returnValue = null;

		
		switch(messageType){
		case "PUTCHUNK": 
			
			if(chunkData == null){
				chunk.read();
				chunkData = chunk.body;
			}
			returnValue= "PUTCHUNK " + version + " " + fileId + " " + chunkNumber + " " + repDegree +  CRLF + CRLF + chunkData.toString()+"\n";			
			break;

		case "STORED":
			returnValue= "STORED " + version + " " + fileId + " " + chunkNumber +  CRLF + CRLF+"\n";
			break;

		case "GETCHUNK":
			returnValue= "GETCHUNK " + version + " " + fileId + " " + chunkNumber +  CRLF + CRLF+"\n";
			break;

		case "CHUNK" :
			
			if(chunkData == null){
				chunk.read();
				chunkData = chunk.body;
			}
			returnValue= "CHUNK " + version + " " + fileId + " " + chunkNumber +  CRLF + CRLF +  chunkData.toString()+"\n";
			break;

		case "DELETE":
			returnValue= "DELETE " + fileId +  CRLF +  CRLF+"\n";
			break;

		case "REMOVED":
			returnValue= "REMOVED " + version + " " + fileId + " " + chunkNumber + CRLF + CRLF+"\n"; 
			break;
		}

		return returnValue.getBytes();
	}


}
