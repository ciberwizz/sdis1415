package main;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Config{
	
	public ConcurrentHashMap<String, Chunk> ourFiles = new ConcurrentHashMap<String, Chunk>();
	public ConcurrentHashMap<String, Chunk> theirFiles = new ConcurrentHashMap<String, Chunk>();
	

	private void filesToCsv() throws IOException {
		File folder = new File("csv");
		File[] listOfFiles = folder.listFiles();
		List<String[]> listing = new ArrayList<String[]>();
		String csv = "csv\\output3.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(csv));

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
		String csv = "csv\\output3.csv";


		try {
			br = new BufferedReader(new FileReader(csv));
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



}

