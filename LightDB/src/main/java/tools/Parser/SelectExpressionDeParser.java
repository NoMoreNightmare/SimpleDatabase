package tools.Parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import tools.Tuple;

/**
 * visit the expression and judge whether the tuple fit the condition
 */
public class SelectExpressionDeParser extends ExpressionDeParser {
    Tuple tuple;
    boolean result = true;

    /**
     * construct a deparser with a tuple
     * @param tuple the tuple need to judge whether it fits the condition
     */
    public SelectExpressionDeParser(Tuple tuple){
        this.tuple = tuple;
    }

    /**
     * default constructor
     */
    public SelectExpressionDeParser(){

    }

    /**
     * set the tuple to judge
     * @param tuple the tuple need to judge whether it fits the condition
     */
    public void setTuple(Tuple tuple){
        this.tuple = tuple;
    }

    /**
     * get the result of whether the tuple fit the condition
     * @return
     */
    public boolean getWhereResult(){
        return this.result;
    }

    /**
     * reset the result
     */
    public void reset(){
        result = true;
    }

    /**
     * visit and decompose the and expression
     * @param expression AndExpression
     */
    @Override
    public void visit(AndExpression expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        expressionForAnd(left);
        expressionForAnd(right);

    }

    /**
     * if the expression is long value or column, judge whether it is true or false
     * @param expression the expression
     */
    private void expressionForAnd(Expression expression) {
        if(expression instanceof LongValue){
            LongValue longValue = (LongValue) expression;
            long num = longValue.getValue();
            if(num == 0){
                result = false;
            }
        }else if(expression instanceof Column){
            Column column = (Column) expression;
            String columnName = column.getFullyQualifiedName().toUpperCase();
            Integer value = tuple.getValue(columnName);
            if("True".equalsIgnoreCase(columnName)){
                return;
            }else if("False".equalsIgnoreCase(columnName)){
                result = false;
            }
            if(value == null){
                throw new RuntimeException("The column doesn't exist");
            }

            if(value == 0){
                result = false;
            }

        }else{
            expression.accept(this);
        }
    }

    /**
     * if the expression is numerical, return the value
     * @param expression the expression contains value
     * @return the value of the column or long value
     */
    private long expressionForNumerical(Expression expression) {
        if(expression instanceof LongValue){
            LongValue longValue = (LongValue) expression;
            long num = longValue.getValue();
            return num;
        }else if(expression instanceof Column){
            Column column = (Column) expression;
            String columnName = column.getFullyQualifiedName().toUpperCase();
            Integer value = tuple.getValue(columnName);
            if(value == null){

                throw new RuntimeException("The column doesn't exist");
            }

            return value;

        }else{
            return 0;
        }
    }

    /**
     * if the expression is equalTo, calculate the result
     * @param expression equalTo expression
     */
    @Override
    public void visit(EqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue == rightValue);

    }

    /**
     * if the expression is NotEqualTo, calculate the result
     * @param expression NotEqualTo expression
     */
    @Override
    public void visit(NotEqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue != rightValue);
    }

    /**
     * if the expression is GreaterThan, calculate the result
     * @param expression greaterThan expression
     */
    @Override
    public void visit(GreaterThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue > rightValue);
    }

    /**
     * if the expression is GreaterThanEquals, calculate the result
     * @param expression greaterThanEquals expression
     */
    @Override
    public void visit(GreaterThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue >= rightValue);
    }

    /**
     * if the expression is MinorThan, calculate the result
     * @param expression MinorThan expression
     */
    @Override
    public void visit(MinorThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue < rightValue);
    }

    /**
     * if the expression is MinorThanEquals, calculate the result
     * @param expression minorThanEquals expression
     */
    @Override
    public void visit(MinorThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue <= rightValue);
    }
}
