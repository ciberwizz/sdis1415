package main;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
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

	/*private static void writeCSV() throws IOException {
		System.out.println("\n**** Write to CSV ****");

		String csv = "csv\\output.csv";

		CSVWriter writer = new CSVWriter(new FileWriter(csv));

		String[] chunkId = "Chunk1#Chunk2#Chunk3".split("#");
		writer.writeNext(chunkId);
		System.out.println("CSV written successfully.");
		writer.close();
	}*/

	private static void writeAll() throws IOException {
		System.out.println("\n**** Write All to CSV ****");

		String csv = "csv\\output2.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(csv));

		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "Chunk1", "File1" });
		data.add(new String[] { "Chunk2", "File2" });

		writer.writeAll(data);
		System.out.println("CSV written successfully.");
		writer.close();
	}

	public static void mapObjectChunk() throws FileNotFoundException {
		System.out.println("\n**** Map CSV to Chunk Object ****");

		ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
		strat.setType(Chunk.class);
		String[] columns = new String[] { "chunkId", "fileId" };
		strat.setColumnMapping(columns);

		CsvToBean csv = new CsvToBean();

		String csvFilename = "csv\\chunks.csv";
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename));

		List list = csv.parse(strat, csvReader);
		for (Object object : list) {
			Chunk chunk = (Chunk) object;
			System.out.println(chunk.getChunkId());
		}
	}
}
