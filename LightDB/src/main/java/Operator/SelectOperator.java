package Operator;

import net.sf.jsqlparser.statement.select.FromItem;
import pojo.Parser.SelectExpressionDeParser;
import pojo.PropertyInTest;
import pojo.Tuple;
import net.sf.jsqlparser.expression.Expression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * the selection operator
 */
public class SelectOperator extends Operator{

    Operator operator;
    Expression expression;

    /**
     * construct the selection operator
     * @param expression the expression for filtering the tuples
     * @param operator the child operator
     */
    public SelectOperator(Expression expression, Operator operator){
        this.operator = operator;
        this.expression = expression;
    }

    /**
     * get the next tuple that fit the selection expression
     * @return the next tuple
     */
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

    /**
     * reset the operator
     */
    @Override
    public void reset() {
        operator.reset();
    }

}
