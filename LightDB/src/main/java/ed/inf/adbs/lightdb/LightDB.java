package ed.inf.adbs.lightdb;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import Interpreter.QueryConstructor;
import Operator.Operator;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import pojo.Catalog;
import pojo.PropertyInTest;

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
		QueryConstructor queryConstructor = new QueryConstructor();
		Statement statement = CCJSqlParserUtil.parse(new FileReader(Catalog.getInstance().getSqlPath()));
		Operator operator = queryConstructor.constructor(statement);
		operator.dump();
	}

//	public static Properties loadProperties() throws IOException {
//		InputStream inputStream = LightDB.class.getClassLoader().getResourceAsStream("properties.properties");
//		Properties properties = new Properties();
//		properties.load(inputStream);
//
//		return properties;
//	}




}
