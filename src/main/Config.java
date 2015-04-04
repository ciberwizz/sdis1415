package main;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Config {

    public static ConcurrentHashMap<String, Chunk> chunksOfOurFiles = new ConcurrentHashMap<String, Chunk>();
    public static ConcurrentHashMap<String, Chunk> theirChunks = new ConcurrentHashMap<String, Chunk>();
    public static ConcurrentHashMap<String, Integer> numberOfChunks = new ConcurrentHashMap<String, Integer>();
    private long reservedSpace;
    private long usedSpace;
    //private long freeSpace;

/*
    private void filesToCsv() throws IOException {
        File folder = new File("files");
        File[] listOfFiles = folder.listFiles();
        List<String[]> listing = new ArrayList<String[]>();
        CSVWriter writer = new CSVWriter(new FileWriter("csv\\fileslist.csv"));

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile())
                listing.add(new String[]{listOfFiles[i].getName()});
        }

        writer.writeAll(listing);
        writer.close();
    }
*/

    private void newFileInPath() throws IOException, NoSuchAlgorithmException {

        String [] nextLine;
        CSVReader reader = new CSVReader(new FileReader("csv\\fileslist.csv"));
        ArrayList<String> csvContent = new ArrayList<String>();
        File folder = new File("files");
        File[] listOfFiles = folder.listFiles();
        FileWriter fileWriter = new FileWriter("csv\\fileslist.csv", true);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        int a = 0;

        while ((nextLine = reader.readNext()) != null) {
            csvContent.add(nextLine[0]);
        }

        for (int i = 0; i < listOfFiles.length; i++) {
            String search = listOfFiles[i].getName();

            if(csvContent.size()== 0 && listOfFiles[i].isFile()) {
                csvWriter.writeNext(new String[]{listOfFiles[i].getName()});
                splitFile("files\\"+search);
            }
            else{
                for (String str : csvContent) {
                    if (search.equals(str) && listOfFiles[i].isFile()) {a = 1;}
                }

                if(a == 0 && listOfFiles[i].isFile()){
                    csvWriter.writeNext(new String[]{listOfFiles[i].getName()});
                    splitFile("files\\"+search);
                }
                csvWriter.close();
            }
        }
        reader.close();

    }


    private void splitFile(String fileName) throws IOException, NoSuchAlgorithmException {

        File receivedFile = new File(fileName);
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
            if (fSize <= 64) {
                chunkPartBytes = new byte[fSize];
                read = inputStream.read(chunkPartBytes, 0, fSize);
            } else {
                chunkPartBytes = new byte[64];
                read = inputStream.read(chunkPartBytes, 0, 64);
            }

            fSize = fSize - read;
            nChunks++;
            fileIdChunkNr = fileId + "_" + Integer.toString(nChunks - 1);
            outputStream = new FileOutputStream(new File(fileIdChunkNr));
            outputStream.write(chunkPartBytes);
            chunk = new Chunk(nChunks-1, fileId, receivedFile.getPath(), 0);
            chunksOfOurFiles.put(fileIdChunkNr, chunk);
            Parser.writeChunkToCsv(chunk);
            outputStream.flush();
            outputStream.close();
        }
        inputStream.close();
        receivedFile.delete();
        numberOfChunks.put(fileName, nChunks);
        Parser.writeNumberOfChunksToCsv(fileName,nChunks);
        // freeSpace = freeSpace - fSize;
        //}

    }


    private void restoreFile(String fileName) throws IOException, NoSuchAlgorithmException {
        int nChunks = numberOfChunks.get(fileName);
        ArrayList<File> cfile = new ArrayList<File>();
        String fileId = toSHA256(fileName);
        byte chunkData[] = null;
        InputStream inputStream = null;
        OutputStream outputStream = new FileOutputStream(new File(fileName));

        for (int i = 0; i < nChunks; i++) {
            File file = new File(fileId + "_" + i);
            cfile.add(file);
        }


        for (File file : cfile) {
            inputStream = new FileInputStream(file);
            chunkData = new byte[(int) file.length()];
            inputStream.read(chunkData, 0, (int) file.length());
            outputStream.write(chunkData);
            outputStream.flush();
            chunkData = null;
            inputStream.close();
            inputStream = null;
        }
        outputStream.close();
        outputStream = null;

    }


    private String toSHA256(String filename) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(filename.getBytes());
        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }


	public void delete(Message temp) {
		// TODO DELETE de um fileID e seus chunks
		//criar uma thread para fazer o delete, assim não poe a main thread busy com IO
		
		
	}

	public void decRepDegree(Message temp) {
		// TODO decrementar o repdegree associado ao chunk temp4
		// caso o repdegree for menor que o desejado
		//		temos de enviar um putchunk se já não tiver sido enviado por alguem
		
	}

}