package tables;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import model.FileTable;
import model.Row;
import model.Table;

public class XMLTable implements FileTable {
	private static final Path base = Paths.get("db", "tables");
	private final Path xmlFile;

	private static final DocumentFactory helper = DocumentFactory.getInstance();
	private final Document doc;

	public XMLTable(String name, List<String> columns) {
		try {
			Files.createDirectories(base);

			xmlFile = base.resolve(name + ".xml");
			if (Files.notExists(xmlFile))
				Files.createFile(xmlFile);

			doc = helper.createDocument();

			flush();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}

		throw new UnsupportedOperationException("Implement rest of 2-ary constructor for Module 4b");
	}

	public XMLTable(String name) {
		try {
			xmlFile = base.resolve(name + ".xml");

			if (Files.notExists(xmlFile))
				throw new IllegalArgumentException("Missing table: " + name);

			doc = new SAXReader().read(xmlFile.toFile());
		}
		catch (DocumentException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Implement clear for Module 4b");
	}

	@Override
	public void flush() {
		try {
			var writer = new XMLWriter(new FileWriter(xmlFile.toFile()));
	        writer.write(doc);
	        writer.close();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		throw new UnsupportedOperationException("Implement put for Module 4b");
	}

	@Override
	public List<Object> get(String key) {
		throw new UnsupportedOperationException("Implement get for Module 4b");
	}

	@Override
	public List<Object> remove(String key) {
		throw new UnsupportedOperationException("Implement remove for Module 4b");
	}

	@Override
	public int degree() {
		throw new UnsupportedOperationException("Implement degree for Module 4b");
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("Implement size for Module 4b");
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException("Implement table's hashCode for Module 4b");
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Table &&
			this.hashCode() == obj.hashCode();
	}

	@Override
	public Iterator<Row> iterator() {
		throw new UnsupportedOperationException("Implement iterator for Module 4b");

	}

	@Override
	public String name() {
		throw new UnsupportedOperationException("Implement name for Module 4b");
	}

	@Override
	public List<String> columns() {
		throw new UnsupportedOperationException("Implement columns for Module 4b");
	}

	@Override
	public String toString() {
		return toPrettyString();
	}
}