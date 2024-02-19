package Operator;

import pojo.Parser.SelectExpressionDeParser;
import pojo.Tuple;
import net.sf.jsqlparser.expression.Expression;

public class SelectOperator extends Operator{

    ScanOperator scanOperator;
    Expression expression;
    public SelectOperator(String tableName, Expression expression){
        this.scanOperator = new ScanOperator(tableName);
        this.expression = expression;
    }
    @Override
    public Tuple getNextTuple() {
        Tuple tuple = scanOperator.getNextTuple();
        if(tuple == null){
            return null;
        }

        SelectExpressionDeParser deParser = new SelectExpressionDeParser();

        while(true){
            deParser.setTuple(tuple);
            this.expression.accept(deParser);
            if(deParser.getWhereResult()){
                return tuple;
            }
            deParser.reset();
            tuple = scanOperator.getNextTuple();
            if(tuple == null){
                return null;
            }
        }

    }

    @Override
    public void reset() {
        scanOperator.reset();
    }
}
