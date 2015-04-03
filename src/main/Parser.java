package main;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
