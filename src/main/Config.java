package main;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Config {

    public ConcurrentHashMap<String, ArrayList<String>> chunksOfOurFiles = new ConcurrentHashMap<String, ArrayList<String>>();
    public ConcurrentHashMap<String, ArrayList<String>> theirFiles = new ConcurrentHashMap<String, ArrayList<String>>();
    private long reservedSpace;
    private long usedSpace;
    //private long freeSpace;


    private void filesToCsv() throws IOException {
        File folder = new File("csv");
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

    private ArrayList<File> newFileInPath() throws IOException {

        String line = null;
        BufferedReader br = null;
        ArrayList<String> csvContent = new ArrayList<String>();
        File folder = new File("csv");
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> newFiles = new ArrayList<File>();


        try {
            br = new BufferedReader(new FileReader("csv\\fileslist.csv"));
            while ((line = br.readLine()) != null)
                csvContent.add(line);
        } finally {
            if (br != null)
                br.close();
        }


        for (int i = 0; i < listOfFiles.length; i++) {
            String search = listOfFiles[i].getName();
            for (String str : csvContent) {
                if (!str.contains(search))
                    newFiles.add(listOfFiles[i]);
            }


        }

        for (int i = 0; i < newFiles.size(); i++) {
            System.out.println(newFiles.get(i));
        }
        return newFiles;
    }


    private void splitFile(String fileName) throws IOException {

        File receivedFile = new File(fileName);
        FileInputStream inputStream = new FileInputStream(receivedFile);
        FileOutputStream outputStream = null;
        String newChunkName;
        int fSize = (int) receivedFile.length();
        int nChunks = 0;
        int read = 0;
        byte[] chunkPartBytes = null;
        ArrayList<String> fileChunks = new ArrayList<String>();

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
            newChunkName = fileName + ".part" + Integer.toString(nChunks - 1);
            outputStream = new FileOutputStream(new File(newChunkName));
            outputStream.write(chunkPartBytes);
            fileChunks.add(newChunkName);
            outputStream.flush();
            outputStream.close();
        }
        inputStream.close();
        receivedFile.delete();
        chunksOfOurFiles.put(fileName, fileChunks);
        // freeSpace = freeSpace - fSize;
        //}

    }


    private void restoreFile(String fileName) throws IOException {

        ArrayList<String> chunkFiles = chunksOfOurFiles.get(fileName);
        ArrayList<File> cfile = new ArrayList<File>();
        byte chunkData[] = null;
        InputStream inputStream = null;
        OutputStream outputStream = new FileOutputStream(new File(fileName));

        for (int j = 0; j < chunkFiles.size(); j++) {
            cfile.add(new File(chunkFiles.get(j)));
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

