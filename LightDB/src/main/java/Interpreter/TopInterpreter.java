package Interpreter;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import net.sf.jsqlparser.statement.Statement;
import Operator.*;
import pojo.Catalog;

/**
 * take the sql query file and database and process the query
 */
public class TopInterpreter {

    Statement statement;
    /**
     * constructor a query plan of that sql query
     * @param inputFile the sql query file
     * @param database the database contains data files
     * @throws FileNotFoundException exception if file doesn't exist
     * @throws JSQLParserException exception if parse failed
     */
    public TopInterpreter(String inputFile, String database) throws FileNotFoundException, JSQLParserException {
        statement = CCJSqlParserUtil.parse(new FileReader(inputFile));
        Catalog.getInstance().setDatabase(database);
    }

    /**
     * default constructor
     */
    public TopInterpreter(){

    }

    /**
     * parse the statement stored in the input file
     * @param inputFile the sql query file
     * @throws FileNotFoundException exception if file doesn't exist
     * @throws JSQLParserException exception if parse failed
     */
    public void parseStatement(String inputFile) throws FileNotFoundException, JSQLParserException {
        statement = CCJSqlParserUtil.parse(new FileReader(inputFile));
    }

    /**
     * set the statement need to parse
     * @param statement
     */
    public void setStatement(Statement statement){
        this.statement = statement;
    }

    /**
     * print or store all the tuples of that query
     */
    public void dump(){
        if(statement != null){
            QueryConstructor queryConstructor = new QueryConstructor();
            Operator operator = queryConstructor.constructor(statement);
            operator.dump();
        }else{
            throw new NullPointerException("The statement is null");
        }

    }
}
