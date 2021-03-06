package main;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import javax.swing.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Config {

    public static ConcurrentHashMap<String, Chunk> chunksOfOurFiles = new ConcurrentHashMap<String, Chunk>();
    public static ConcurrentHashMap<String, Chunk> theirChunks = new ConcurrentHashMap<String, Chunk>();
    public static ConcurrentHashMap<String, Integer> numberOfChunks = new ConcurrentHashMap<String, Integer>();
    public static ConcurrentLinkedQueue<String> missingChunks = new ConcurrentLinkedQueue<String>();
    public static int rDegree;
    private long reservedSpace;
    private long usedSpace;
    //private long freeSpace

//    public static void main(String[] args) throws NoSuchAlgorithmException {
//
//        try {
//            newFileInPath();
//restoreFile("a.png");
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void newFileInPath()  {

        String [] nextLine;
        FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("csv/fileslist.csv", true);

        CSVWriter csvWriter = new CSVWriter(fileWriter);
        CSVReader reader = new CSVReader(new FileReader("csv/fileslist.csv"));
        ArrayList<String> csvContent = new ArrayList<String>();
        File folder = new File("data/files");
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> folderContent = new ArrayList<String>();

        while ((nextLine = reader.readNext()) != null) {
            csvContent.add(nextLine[0]);
        }
        reader.close();

        for (int i = 0; i < listOfFiles.length; i++)
            if(listOfFiles[i].isFile())
                folderContent.add(listOfFiles[i].getName());

        Set<String> a = new HashSet<String>(csvContent);
        Set<String> b = new HashSet<String>(folderContent);
        b.removeAll(a);
            if(b.size()>0){
                rDegree = Integer.parseInt(JOptionPane.showInputDialog("New files found. Set the replication degree:"));
            }
        ArrayList<String> mainList = new ArrayList<String>();
        mainList.addAll(b);

        for (int h = 0; h < mainList.size(); h++) {
            csvWriter.writeNext(new String[]{mainList.get(h)});
            splitFile(mainList.get(h));
        }
        csvWriter.close();
		} catch (IOException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }


    private static void splitFile(String fileName) throws IOException, NoSuchAlgorithmException {

        File receivedFile = new File("data/files/"+fileName);
        FileInputStream inputStream = new FileInputStream(receivedFile);
        FileOutputStream outputStream = null;
        String fileId = toSHA256(fileName);
        String fileIdChunkNr;
        int fSize = (int) receivedFile.length();
        int nChunks = 0;
        int read = 0;
        byte[] chunkPartBytes = null;
        Chunk chunk = null;

        //if (freeSpace >= fSize) {
        while (fSize > 0) {
            if (fSize <= 64*1024) {
                chunkPartBytes = new byte[fSize];
                read = inputStream.read(chunkPartBytes, 0, fSize);
            } else {
                chunkPartBytes = new byte[64*1024];
                read = inputStream.read(chunkPartBytes, 0, 64*1024);
            }

            fSize = fSize - read;
            nChunks++;
            fileIdChunkNr = fileId + "_" + Integer.toString(nChunks - 1);
            outputStream = new FileOutputStream(new File("data/chunks",fileIdChunkNr));
            outputStream.write(chunkPartBytes);
            chunk = new Chunk(nChunks-1, fileId, receivedFile.getPath(), rDegree);
            chunksOfOurFiles.put(fileIdChunkNr, chunk);
            Parser.writeChunkToCsv(chunk);
            outputStream.flush();
            outputStream.close();
        }
        inputStream.close();
        numberOfChunks.put(fileName, nChunks);
        Parser.writeNumberOfChunksToCsv(fileName,nChunks);
        // freeSpace = freeSpace - fSize;
        //}

    }


    public static void restoreFile(String fileName) throws IOException, NoSuchAlgorithmException {
        int nChunks = numberOfChunks.get(fileName);
        ArrayList<File> cfile = new ArrayList<File>();
        ArrayList<String> missingchunk = new ArrayList<String>();
        String fileId = toSHA256(fileName);
        byte chunkData[];
        InputStream inputStream = null;
        OutputStream outputStream = new FileOutputStream(new File("data/files/"+"restored_" +fileName));

        for (int i = 0; i < nChunks; i++) {
            File file = new File("data/chunks/"+fileId + "_" + i);
            if(file.exists())
                cfile.add(file);
            else
                missingchunk.add(fileId + "_" + i);
        }


        if(missingchunk.size() > 0){
            missingChunks.addAll(missingchunk);
            return;
        }

        for (File file : cfile) {
            inputStream = new FileInputStream(file);
            chunkData = new byte[(int) file.length()];
            inputStream.read(chunkData, 0, (int) file.length());
            outputStream.write(chunkData);
            outputStream.flush();
            inputStream.close();
        }
        outputStream.close();

    }


    public static void deleteFile(String fileName) throws NoSuchAlgorithmException, IOException {

        File file = new File("data/files/"+fileName);
        File chunksFolder = new File("data/chunks/");
        File[] listOfFiles = chunksFolder.listFiles();

        Set set = numberOfChunks.keySet();
        Set set2 = chunksOfOurFiles.keySet();

        Iterator iterator = set.iterator();
        Iterator iterator2 = set2.iterator();

        while (iterator.hasNext())
        {
            Object o = iterator.next();
            if (o.toString().equals(fileName))
                numberOfChunks.remove(o.toString());
        }

        while (iterator2.hasNext())
        {
            Object o = iterator2.next();
            if (o.toString().contains(toSHA256(fileName)))
                chunksOfOurFiles.remove(o.toString());
        }

        Parser.removeFileFromCsv(fileName);

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(toSHA256(fileName))) {
                File auxFile = new File("data/chunks/" + listOfFiles[i].getName());
                auxFile.delete();
            }
        }

        file.delete();

    }


    public static void deleteChunk(String chunkName) throws NoSuchAlgorithmException, IOException {

        File file = new File("data/files/"+chunkName);
        Set set = chunksOfOurFiles.keySet();
        Iterator iterator = set.iterator();

        while (iterator.hasNext())
        {
            Object o = iterator.next();
            if (o.toString().equals(chunkName))
                chunksOfOurFiles.remove(o.toString());
        }

        Parser.removeChunkFromCsv(chunkName);

        file.delete();

    }


    public static String toSHA256(String filename) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(filename.getBytes());
        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }


    // return
    //     - chunk - objectiveRepdegree > repdegree
    //     - null - objectiveRepdegree <= repdegree
    public Chunk decRepDegree(Message temp) {
    	
    	String id = temp.getId();
    	
    	Chunk chk = null;
    	
    	if(theirChunks.containsKey(id)){
    		chk = theirChunks.get(id);
    		
    		chk.decRepDegree();
    		
    		theirChunks.replace(id, chk);
    		
    		
    	} else
    		if(chunksOfOurFiles.containsKey(id)){
        		chk = chunksOfOurFiles.get(id);
        		
        		chk.decRepDegree();
        		
        		chunksOfOurFiles.replace(id, chk);
    			
    		}
    	
    
    	
    	return chk;

    }


	public static void loadCSV() {
		
		try {
			Parser.mapCsvToHash(numberOfChunks);
			Parser.mapCsvToChunk(chunksOfOurFiles, theirChunks, numberOfChunks);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
}