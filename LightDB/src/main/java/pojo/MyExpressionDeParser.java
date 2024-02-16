package pojo;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class MyExpressionDeParser extends ExpressionDeParser {
    Tuple tuple;
    boolean result = true;
    public MyExpressionDeParser(Tuple tuple){
        this.tuple = tuple;
    }

    public MyExpressionDeParser(){

    }

    public void setTuple(Tuple tuple){
        this.tuple = tuple;
    }

    public boolean getWhereResult(){
        return this.result;
    }

    public void reset(){
        result = true;
    }

    @Override
    public void visit(AndExpression expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        expressionForAnd(left);
        expressionForAnd(right);

    }
    private void expressionForAnd(Expression expression) {
        if(expression instanceof LongValue){
            LongValue longValue = (LongValue) expression;
            long num = longValue.getValue();
            if(num == 0){
                result = false;
            }
        }else if(expression instanceof Column){
            Column column = (Column) expression;
            String columnName = column.getColumnName();
            String[] tableAndColumn = columnName.split("\\.");
            String c;
            if(tableAndColumn.length > 1){
                c = tableAndColumn[1];
            }else{
                c = tableAndColumn[0];
            }

            Integer value = tuple.getValue(c);
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

    private long expressionForNumerical(Expression expression) {
        if(expression instanceof LongValue){
            LongValue longValue = (LongValue) expression;
            long num = longValue.getValue();
            return num;
        }else if(expression instanceof Column){
            Column column = (Column) expression;
            String columnName = column.getColumnName();
            String[] tableAndColumn = columnName.split("\\.");
            String c;
            if(tableAndColumn.length > 1){
                c = tableAndColumn[1];
            }else{
                c = tableAndColumn[0];
            }

            Integer value = tuple.getValue(c);
            if(value == null){

                throw new RuntimeException("The column doesn't exist");
            }

            return value;

        }else{
            return 0;
        }
    }

    @Override
    public void visit(EqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue == rightValue);

    }

    @Override
    public void visit(NotEqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue != rightValue);
    }

    @Override
    public void visit(GreaterThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue > rightValue);
    }

    @Override
    public void visit(GreaterThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue >= rightValue);
    }

    @Override
    public void visit(MinorThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue < rightValue);
    }

    @Override
    public void visit(MinorThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        long leftValue = expressionForNumerical(left);
        long rightValue = expressionForNumerical(right);

        result = result && (leftValue <= rightValue);
    }
}
