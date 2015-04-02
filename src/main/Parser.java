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
/*	private static void readLineByLine() throws IOException {
		System.out.println("\n**** Read Line By Line ****");
		String csvFilename = "csv\\chunks.csv";
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
		String[] row = null;
		while ((row = csvReader.readNext()) != null) {
			System.out.println(row[0] + " # " + row[1] + " #  " + row[2]);
		}

		csvReader.close();
	}*/

	private static void readAll() throws IOException {
		System.out.println("\n**** Read All ****");
		String[] row = null;
		String csvFilename = "csv\\chunks.csv";

		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));
		List content = csvReader.readAll();

		for (Object object : content) {
			row = (String[]) object;

			System.out.println(row[0] + " # " + row[1] + " #  " + row[2]);
		}

		csvReader.close();

	}

    static void writeChunkToCsv(Chunk chunk) throws IOException {

        FileWriter fileWriter = new FileWriter("csv\\chunks.csv", true);
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        int chunkNr = chunk.getChunkNr();
        String fileId = chunk.getFileId();
        String path = chunk.getPath();
        int repDegree = chunk.getRepDegree();
        int objectiveRepDegree = chunk.getObjectiveRepDegree();
        byte[] data = chunk.getData();

        String[] dataToWrite = new String[] {String.valueOf(chunkNr), fileId, path, String.valueOf(repDegree), String.valueOf(objectiveRepDegree), String.valueOf(data)};

        csvWriter.writeNext(dataToWrite);
        csvWriter.close();
    }

	public static void mapCsvToChunk() throws FileNotFoundException {
		System.out.println("\n**** Map CSV to Chunk Object ****");

		ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
		strat.setType(Chunk.class);
		String[] columns = new String[] {"chunkNr", "fileId", "path", "repDegree", "objectiveRepDegree", "data"};
		strat.setColumnMapping(columns);

		CsvToBean csv = new CsvToBean();
		CSVReader csvReader = new CSVReader(new FileReader("csv\\chunks.csv"));

		List list = csv.parse(strat, csvReader);
		for (Object object : list) {
			Chunk chunk = (Chunk) object;
            String fileIdChunkNr = chunk.getFileId() + ".part" + Integer.toString(chunk.getChunkNr());
            Config.chunksOfOurFiles.put(fileIdChunkNr, chunk);
			System.out.println(chunk.getChunkNr());
		}
	}
}
