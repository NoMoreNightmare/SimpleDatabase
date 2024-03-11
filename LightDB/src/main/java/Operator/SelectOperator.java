package Operator;

import net.sf.jsqlparser.statement.select.FromItem;
import pojo.Parser.SelectExpressionDeParser;
import pojo.PropertyInTest;
import pojo.Tuple;
import net.sf.jsqlparser.expression.Expression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SelectOperator extends Operator{

    Operator operator;
    Expression expression;



    public SelectOperator(Expression expression, Operator operator){
        this.operator = operator;
        this.expression = expression;
    }
    @Override
    public Tuple getNextTuple() {
        Tuple tuple = operator.getNextTuple();
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
            tuple = operator.getNextTuple();
            if(tuple == null){
                return null;
            }
        }

    }

    @Override
    public void reset() {
        operator.reset();
    }

}
