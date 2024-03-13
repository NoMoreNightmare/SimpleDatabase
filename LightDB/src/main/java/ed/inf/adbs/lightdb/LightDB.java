package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.IOException;

import Interpreter.TopInterpreter;
import net.sf.jsqlparser.JSQLParserException;
import tools.Catalog;

/**
 * Lightweight in-memory database system
 *
 */
public class LightDB {

	public static void main(String[] args) throws IOException {

		if (args.length != 3) {
			System.err.println("Usage: LightDB database_dir input_file output_file");
			return;
		}

		String databaseDir = args[0];
		String sqlFile = args[1];
		String outputFile = args[2];


		Catalog catalog = Catalog.getInstance();
		catalog.setSqlPath(sqlFile);
		catalog.setDbPath(databaseDir);
		catalog.setOutputPath(outputFile);
		catalog.setSchemaFile("schema.txt");

		// Just for demonstration, replace this function call with your logic
		try {
			parsingExample();
		} catch (JSQLParserException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Example method for getting started with JSQLParser. Reads SQL statement from
	 * a file and prints it to screen; then extracts SelectBody from the query and
	 * prints it to screen.
	 */

	public static void parsingExample() throws FileNotFoundException, JSQLParserException {
		TopInterpreter topInterpreter = new TopInterpreter(Catalog.getInstance().getSqlPath(), "data");
		topInterpreter.dump();
	}




}
