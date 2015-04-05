package main;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Parser {
	
	/*
	public static void main(String[] args) {

		try {
			//readLineByLine();
			readAll();
			//writeCSV();
			writeAll();
			mapObjectChunk();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
*/

    public static void writeChunkToCsv(Chunk chunk) throws IOException {

        FileWriter fileWriter = new FileWriter("csv\\chunks.csv", true);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        int chunkNr = chunk.getChunkNr();
        String fileId = chunk.getFileId();
        String path = chunk.getPath();
        int repDegree = chunk.getRepDegree();
        int objectiveRepDegree = chunk.getObjectiveRepDegree();
        byte[] data = chunk.getData();

        String[] dataToWrite = new String[]{String.valueOf(chunkNr), fileId, path, String.valueOf(repDegree), String.valueOf(objectiveRepDegree), String.valueOf(data)};

        csvWriter.writeNext(dataToWrite);
        csvWriter.close();
    }

    public static void writeNumberOfChunksToCsv(String fileName, int nChunks) throws IOException {

        FileWriter fileWriter = new FileWriter("csv\\nchunks.csv", true);
        CSVWriter csvWriter = new CSVWriter(fileWriter);

        String[] dataToWrite = new String[]{fileName, String.valueOf(nChunks)};

        csvWriter.writeNext(dataToWrite);
        csvWriter.close();
    }

    public static void mapCsvToChunk(ConcurrentHashMap<String, Chunk> chunksOfOurFiles) throws FileNotFoundException {

        ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
        strat.setType(Chunk.class);
        String[] columns = new String[]{"chunkNr", "fileId", "path", "repDegree"};
        strat.setColumnMapping(columns);

        CsvToBean csv = new CsvToBean();
        CSVReader csvReader = new CSVReader(new FileReader("csv\\chunks.csv"));

        List list = csv.parse(strat, csvReader);
        for (Object object : list) {
            Chunk chunk = (Chunk) object;
            String fileIdChunkNr = chunk.getFileId() + "_" + Integer.toString(chunk.getChunkNr());
            chunksOfOurFiles.put(fileIdChunkNr, chunk);
        }
    }

    public static void removeFileFromCsv(String filename) throws IOException, NoSuchAlgorithmException {
        File filesList = new File("csv\\fileslist.csv");
        File auxFile = new File("csv\\auxFile.csv");

        BufferedReader bufferedReader = new BufferedReader(new FileReader(filesList));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(auxFile));

        String remove = "\""+filename+"\"";
        String line;

        while((line = bufferedReader.readLine()) != null) {
            String trimmedLine = line.trim();
            if(trimmedLine.equals(remove)) continue;
            bufferedWriter.write(line + System.getProperty("line.separator"));
        }
        bufferedWriter.close();
        bufferedReader.close();
        filesList.delete();
        auxFile.renameTo(filesList);

        File chunks = new File("csv\\chunks.csv");
        File auxFile2 = new File("csv\\auxFile2.csv");

        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(chunks));
        BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(auxFile2));

        String remove2 = "\""+Config.toSHA256(filename)+"\"";
        String line2;

        while((line2 = bufferedReader2.readLine()) != null) {
            String trimmedLine2 = line2.trim();
            if(trimmedLine2.contains(remove2)) continue;
            bufferedWriter2.write(line + System.getProperty("line.separator"));
        }
        bufferedWriter2.close();
        bufferedReader2.close();
        chunks.delete();
        auxFile2.renameTo(chunks);

        File nchunks = new File("csv\\nchunks.csv");
        File auxFile3 = new File("csv\\auxFile3.csv");

        BufferedReader bufferedReader3 = new BufferedReader(new FileReader(nchunks));
        BufferedWriter bufferedWriter3 = new BufferedWriter(new FileWriter(auxFile3));

        String remove3 = "\""+filename+"\"";
        String line3;

        while((line3 = bufferedReader3.readLine()) != null) {
            String trimmedLine3 = line3.trim();
            if(trimmedLine3.contains(remove3)) continue;
            bufferedWriter3.write(line + System.getProperty("line.separator"));
        }
        bufferedWriter3.close();
        bufferedReader3.close();
        nchunks.delete();
        auxFile3.renameTo(nchunks);

    }



    public static void mapCsvToHash(ConcurrentHashMap<String, Integer> numberOfChunks) throws IOException {

        CSVReader cvsReader = new CSVReader(new FileReader("csv\\nChunks.csv"));
        String[] row;
        int i = 0;

        while ((row = cvsReader.readNext()) != null) {
            numberOfChunks.put(row[i], Integer.valueOf(row[i + 1]));
        }
        cvsReader.close();
    }
}
