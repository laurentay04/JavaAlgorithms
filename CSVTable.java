package tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import model.FileTable;
import model.Row;
import model.Table;

public class CSVTable implements FileTable {
	//creates permanent file path
	private final static Path base = Paths.get("db", "tables");
	private Path file;
	
	//2-ary constructor to create base directories
	//create file with the given name
	//store columns as header
	//initialize file structure
	public CSVTable(String name, List<String> columns) {
		
		//try to create a directory for the base 
		try {
			//create base directory
			Files.createDirectories(base);
			
			//create a file with the given name
			this.file = base.resolve(name + ".csv");
			if (Files.notExists(file)) {
				Files.createFile(file);
			}	
			
			//use the column names as a header by making it one line with "," separator
			String header = String.join(",", columns);
			//write the columns as the header into the file
			var lines = Files.readAllLines(file);
			lines.add(header);
			Files.write(file, lines);
		}
		//throw an exception if there is an error
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}

	//reopen the existing file
	public CSVTable(String name) {
		//initialize file 
		this.file = base.resolve(name + ".csv");
		
		//if the file does not exist, throw an error
		if (Files.notExists(file))
		{
			throw new RuntimeException();
		}
		
		//fall-through, it exists
		//try catch block to open the file
		try {
			var lines = Files.readAllLines(file);
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}

	//reinitialize the file structure
	@Override
	public void clear() {
		//delete everything and write header back in
		try {
			String header = String.join(",", columns());
			var lines = List.of(header);
			Files.write(file, lines);
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}
	
	//helper method to encode a row into a CSV string
	private static String encodeRow(String key, List<Object> fields)
	{
		//create a string joiner with a comma as the delimiter
		StringJoiner str = new StringJoiner(",");
		//add the encoded key to the joiner
		str.add(encode(key));
		//iterator through each field and add each encoded field to joiner
		for (Object f : fields) {
			str.add(encode(f));
		}
		
		//return the encoded row as a string
		return str.toString();
	}
	
	//helper method to encode inputed object
	private static String encode(Object obj)
	{
		//if object is null, return "null"
		if (obj == null) {
			return "null";
		} 
		//if object is a string, return it encased in quotations
		else if (obj instanceof String) {
			return "\"" + obj + "\"";
		} 
		//if object is an int, double, or boolean, return it as a string
		else if (obj instanceof Integer || obj instanceof Double || obj instanceof Boolean) {
			return obj.toString();
		//if all tests fail, throw error
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	//helper method to decode a row from a CSV String
	private static Row decodeRow(String str)
	{
		//split the string into an array at every comma
		String[] fields = str.split(",");
		String key = fields[0].trim().replace("\"", "");
	
		//create a list of decoded objects
		List<Object> decodeFields = new ArrayList<Object>();
		for (int i = 1; i < fields.length; i++)
		{
			decodeFields.add(decode(fields[i]));
		}
		//return the decoded row
		return new Row(key, decodeFields);
	}
	
	//helper method to decode a string back into an object
	private static Object decode(String s)
	{
		//if the string is "null", its a null
		if(s.equals("null"))
		{
			return null;
		}
		//if the string starts and ends with quotes, remove quotes
		else if (s.startsWith("\"") && s.endsWith("\""))
		{
			return s.substring(1, s.length()-1);
		}
		//if the string is either true or false, case-insensitive, it's a boolean
		else if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
		{
			return Boolean.parseBoolean(s);
		}
		//if the string is an integer, return it as an int
		else if (s.matches("-?\\d+")) 
		{
			return Integer.parseInt(s);
		}
		//if string is a float/double, return as double
		else if (s.matches("-?\\d+(\\.\\d+)?"))
		{
			return Double.parseDouble(s);
		}
		else {
			return s;
		}
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		try {
			//read all lines from the Csv file into a list of lines
			var lines = Files.readAllLines(file);
			
			//degree is the size of the fields plus key, so -1 to degree for the key
			if (fields.size() != degree()-1)
			{
				throw new IllegalArgumentException();
			}
			
			//make new row object with key and fields (as in prev mods)
			Row row = new Row(key, fields);
			List<Object> oldField = null;
			
			//for each line number (in list) excluding the header {
			for (int i = 1; i < lines.size(); i++) {
				//get the csv line from the list at that line num
				//decode the csv line into a row using helper method
				Row r = decodeRow(lines.get(i));
			
				// if the key of the decoded row equals our param key (hit){
				if (r.key().equals(key))
				{
					oldField = r.fields();
					//remove the old matching row in list
					lines.remove(encodeRow(r.key(), r.fields()));
					//insert newly updated row in list
					//if hit in first line, add it back at first line
					if (i == 1){
						lines.add(i, encodeRow(row.key(), row.fields()));
					}
					//otherwise add it in at line before
					else {
						lines.add(i-1, encodeRow(row.key(), row.fields()));
					}
					//write full list of lines back to file
					Files.write(file, lines);
					//return old fields
					return oldField;
				}
			
			}
			
			//fall through (miss)
			//insert newly created row (no heuristic)
			var newRow = encodeRow(row.key(), row.fields());
			lines.add(newRow);
			//write list of lines back to file
			Files.write(file, lines);
			//return null
			return null;
		}
		catch (IOException e) {
			throw new RuntimeException();
		}
	}

	//on hit, return old list of fields
	//	apply move-to-front heuristic
	//on miss, return null
	@Override
	public List<Object> get(String key) {
		try {
			//read all lines from the Csv file into a list of lines
			var lines = Files.readAllLines(file);
					
			//for each line number (in list) excluding the header {
			for (int i = 1; i < lines.size(); i++) {
				//get the csv line from the list at that line num
				//decode the csv line into a row using helper method
				Row r = decodeRow(lines.get(i));
					
				// if the key of the decoded row equals our param key (hit){
				if (r.key().equals(key)) {
					//do transpose heuristic
					//get the line BEFORE our line
					var transposeLine = lines.get(i-1);
					//set the top line as the line we got
					lines.set(i-1, lines.get(i));
					//set the line we got as the old transposed line
					lines.set(i, transposeLine);
					//write full list of lines back to file
					Files.write(file, lines);
					//return old fields
					return r.fields();
					
				}
			}
					
			//fall through (miss)
			//write list of lines back to file
			Files.write(file, lines);
			//return null
			return null;
		}
		catch (IOException e) {
			throw new RuntimeException();
		}
	}

	//on a hit, delete the old row and return the old list of fields
	//on miss, return null
	@Override
	public List<Object> remove(String key) {
		try {
			//read all lines from csv file into list of lines
			var lines = Files.readAllLines(file);
			List<Object> oldField = null;
			
			//for each line in list excluding header
			for (int i = 1; i < lines.size(); i++)
			{
				//decode the csv line into a row
				Row r = decodeRow(lines.get(i));
				
				//if the key of the decoded row equals param key
				if (r.key().equals(key))
				{
					oldField = r.fields();
					//remove the row
					lines.remove(encodeRow(r.key(), r.fields()));
					//write full list of lines back to file
					Files.write(file, lines);
					//return old fields
					return oldField;
				}
			}
			
			//fall-through (miss)
			//write list of lines to file
			Files.write(file, lines);
			//return null
			return null;
		}
		catch (IOException e) {
			throw new RuntimeException();
		}
	}

	//total number of columns
	@Override
	public int degree() {
		return columns().size();
	}

	//number of rows not amortized
	@Override
	public int size() {
		try {
			//increase size by 1 for every row until end, starting after header
			var lines = Files.readAllLines(file);
			
			//if the size of the lines is greater than 0 then return the size minus 1 for the header
			//otherwise return 0
			return lines.size() > 0 ? lines.size() - 1 : 0;
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}

	//sum of hash codes of all rows not amortized
	@Override
	public int hashCode() {
		try {
			int fingerprint = 0;
			var lines = Files.readAllLines(file);
			
			//for each non-header line {
			for (int i = 1; i < lines.size(); i++) {
				//decode line into row
				Row r = decodeRow(lines.get(i));
				//fingerprint += that row's hashcode
				fingerprint += r.hashCode();
			}
			//return fingerprint
			return fingerprint;
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}

	//Verify the given object is an instance of any table type in the API.
	//Verify the given object has the same fingerprint as this table.
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Table && obj.hashCode() == this.hashCode()) {
			return true;
		}	
		return false;
	}

	@Override
	public Iterator<Row> iterator() {
		//copied from hashcode, change slightly
		try {
			//let result = new empty list of rows
			List<Row> result = new ArrayList<Row>();
			//read all lines into list
			var lines = Files.readAllLines(file);
			//for each non-header line in list of lines{
			for (int i = 1; i < lines.size(); i++) {
				//decode line into row
				var decodeLine = decodeRow(lines.get(i));
				//add that row to list of rows
				result.add(decodeLine);
			}
			//return list_of_rows.iterator()
			return result.iterator();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String name(){
		return this.file.getFileName().toFile().getName().replace(".csv", "");
	}

	//list of column names aka the header of the file
	@Override
	public List<String> columns(){
		List<String> firstLine = null;
		try {
			//read all lines
			var lines = Files.readAllLines(file);
			//if the file is not empty
			if (!lines.isEmpty())
			{
				//take the first line and split it with a comma
				firstLine = List.of(lines.getFirst().split(","));
			}
			
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
		//return the first line
		return firstLine;
	}

	@Override
	public String toString() {
		return toPrettyString();
	}
	
	//static factory method
	//stores a given CSV string in a file 
	//returns a new CSV table from that file using 1-ary constructor
	public static CSVTable createFromString (String fileName, String str)
	{
		//create new file to store string
		try {
			Files.createDirectories(base);
			Path file = base.resolve(fileName + ".csv");
			Files.createFile(file);
			List<String> list = List.of(str);
			Files.write(file, list);
			//1-ary constructor
			CSVTable newTable = new CSVTable(fileName);
			return newTable;
			
		} catch (IOException e) {
			throw new RuntimeException();
		}
		
	}
}