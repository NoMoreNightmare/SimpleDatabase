package Parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import net.sf.jsqlparser.statement.Statement;
import Operator.*;

public class TopInterpreter {

    Statement statement;

    public TopInterpreter(String inputFile) throws FileNotFoundException, JSQLParserException {
        statement = CCJSqlParserUtil.parse(new FileReader(inputFile));
    }

    public TopInterpreter(){

    }

    public void parseStatement(String inputFile) throws FileNotFoundException, JSQLParserException {
        statement = CCJSqlParserUtil.parse(new FileReader(inputFile));
    }

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
