package main;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import java.io.*;
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

        CSVReader cvsReader = new CSVReader(new FileReader(filesList), ',', '\"', '@');
        CSVWriter csvWriter = new CSVWriter(new FileWriter(auxFile));

        String[] row;

        while ((row = cvsReader.readNext()) != null) {
            if(row[0].equals(filename)) continue;
            else{
                csvWriter.writeNext(row);}
        }
        csvWriter.close();
        cvsReader.close();
        filesList.delete();
        auxFile.renameTo(filesList);

        File chunks = new File("csv\\chunks.csv");
        File auxFile2 = new File("csv\\auxFile2.csv");

        CSVReader cvsReader2 = new CSVReader(new FileReader(chunks), ',', '\"', '@');
        CSVWriter csvWriter2 = new CSVWriter(new FileWriter(auxFile2));
        
        String[] row2;

        while ((row2 = cvsReader2.readNext()) != null) {
            if(row2[2].equals("data\\files\\"+filename)) continue;
            else{
            csvWriter2.writeNext(row2);}
        }
        csvWriter2.close();
        cvsReader2.close();
        chunks.delete();
        auxFile2.renameTo(chunks);

        File nchunks = new File("csv\\nchunks.csv");
        File auxFile3 = new File("csv\\auxFile3.csv");

        CSVReader cvsReader3 = new CSVReader(new FileReader(nchunks), ',', '\"', '@');
        CSVWriter csvWriter3 = new CSVWriter(new FileWriter(auxFile3));

        String[] row3;

        while ((row3 = cvsReader3.readNext()) != null) {
            if(row3[0].equals(filename)) continue;
            else{
                csvWriter3.writeNext(row3);}
        }
        csvWriter3.close();
        cvsReader3.close();
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
