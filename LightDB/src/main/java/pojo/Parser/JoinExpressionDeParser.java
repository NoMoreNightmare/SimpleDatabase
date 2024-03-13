package pojo.Parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import java.util.*;

/**
 * it is the deparser for join expression, it will visit the expression and split the expression for the left table,
 * right table, and for the join. It assists to implement the selection pushdown
 */
public class JoinExpressionDeParser extends ExpressionDeParser {
//    Tuple leftTuple;
    String rightTuple;
    Expression thisExpressionSingle;
    Expression thisExpressionJoin;
    Expression otherExpression;
    Map<String, Column> required = new HashMap<>();

    /**
     * construct the deparser and set the right table's name
     * @param rightTuple the right table's name
     */
    public JoinExpressionDeParser(String rightTuple){
        this.rightTuple = rightTuple;
    }

    /**
     * default constructor
     */
    public JoinExpressionDeParser(){

    }

    /**
     * set the right table's name
     * @param rightTuple right table's name
     */
    public void setTuple(String rightTuple){
        this.rightTuple = rightTuple;
    }

    /**
     * get the expressions belong to one table
     * @return the expressions belong to one table
     */
    public Expression getThisExpressionSingle() {
        return thisExpressionSingle;
    }

    /**
     * get the expressions for the join
     * @return the expressions for the join
     */
    public Expression getThisExpressionJoin() {
        return thisExpressionJoin;
    }

    /**
     * get the expressions belong to another table
     * @return the expressions belong to another table
     */
    public Expression getOtherExpression() {
        return otherExpression;
    }

    /**
     * reset the expressions
     */
    public void reset(){
        thisExpressionSingle = null;
        thisExpressionJoin = null;
        otherExpression = null;
    }

    /**
     * visit all the andExpression and decompose it
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
     * if the expression is still andExpression or expression for numerical calculation, keep visit and decompose
     * else concat them with expression for one table
     * @param expression the expression
     */
    private void expressionForAnd(Expression expression) {
        if(expression instanceof LongValue){
            LongValue longValue = (LongValue) expression;
            if(thisExpressionSingle == null){
                thisExpressionSingle = longValue;
            }else{
                AndExpression newExpression = new AndExpression();
                newExpression.withLeftExpression(thisExpressionJoin);
                newExpression.withRightExpression(longValue);
                this.thisExpressionSingle = newExpression;
            }
        }else if(expression instanceof Column){
            Column column = (Column) expression;
            String tableName = column.getTable().getName().toUpperCase();
            String rightTupleTableName = rightTuple;
            if(tableName.equals(rightTupleTableName)){
                if(this.thisExpressionSingle == null){
                    thisExpressionSingle = column;
                }else{
                    AndExpression andExpression = new AndExpression();
                    andExpression.withLeftExpression(thisExpressionSingle);
                    andExpression.withRightExpression(column);
                    thisExpressionSingle = andExpression;
                }
            }
        }else{
            expression.accept(this);
        }
    }

    /**
     * if the expression is the numerical calculation, concat them with correlated tables or joins
     * @param expression the numerical expression
     */
    @Override
    public void visit(EqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();


        concatExpression(expression, left, right);
    }

    /**
     * concat the expression to the correlated tables or joins
     * if the expression only uses the column from one table, then it belongs to that table; otherwise
     * it should be kept for the join operator
     * @param expression the single expression
     * @param left the left part of that expression
     * @param right the right part of that expression
     */
    private void concatExpression(Expression expression, Expression left, Expression right) {
        if(left instanceof Column leftColumn){
            if(right instanceof Column rightColumn){
                String newLeft = leftColumn.getTable().toString().toUpperCase();
                String newRight = rightColumn.getTable().toString().toUpperCase();

                if(newLeft.equals(rightTuple) || newRight.equals(rightTuple)){
                    if(newLeft.equals(rightTuple)){
                        if(newRight.equals(rightTuple)){
                            if(thisExpressionSingle == null){
                                thisExpressionSingle = expression;
                            }else{
                                thisExpressionSingle = new AndExpression(thisExpressionSingle, expression);
                            }
                        }else{
                            calculateJoinConditions(expression, leftColumn, rightColumn);
                        }
                    }else{
                        calculateJoinConditions(expression, leftColumn, rightColumn);
                    }
                }else{
                    if(otherExpression == null){
                        otherExpression = expression;
                    }else{
                        otherExpression = new AndExpression(otherExpression, expression);
                    }
                }
            }else{
                separateConditions(expression, (Column) left);
            }
        }else{
            if(right instanceof Column){
                separateConditions(expression, (Column) right);
            }else{
                if(thisExpressionSingle == null){
                    thisExpressionSingle = expression;
                }else{
                    thisExpressionSingle = new AndExpression(thisExpressionSingle, expression);
                }
            }
        }
    }

    /**
     * concat the expression with the join expression
     * @param expression the expression
     * @param leftColumn the left column used in the expression
     * @param rightColumn the right column used in the expression
     */
    private void calculateJoinConditions(Expression expression, Column leftColumn, Column rightColumn) {
        if(thisExpressionJoin == null){
            thisExpressionJoin = expression;
        }else{
            thisExpressionJoin = new AndExpression(thisExpressionJoin, expression);
        }
        required.put(leftColumn.getFullyQualifiedName().toUpperCase(), leftColumn);
        required.put(rightColumn.getFullyQualifiedName().toUpperCase(), rightColumn);
    }

    /**
     * concat the expression with the expressions for one table
     * @param expression the expression only used columns in one table
     * @param left the left column
     */
    private void separateConditions(Expression expression, Column left) {
        String newLeft = left.getTable().toString().toUpperCase();
        if(newLeft.equals(rightTuple)){
            if(thisExpressionSingle == null){
                thisExpressionSingle = expression;
            }else{
                thisExpressionSingle = new AndExpression(thisExpressionSingle, expression);
            }
        }else{
            if(otherExpression == null){
                otherExpression = expression;
            }else{
                otherExpression = new AndExpression(otherExpression, expression);
            }
        }
    }

    /**
     * if the expression is the numerical calculation, concat them with correlated tables or joins
     * @param expression the numerical expression
     */
    @Override
    public void visit(NotEqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    /**
     * if the expression is the numerical calculation, concat them with correlated tables or joins
     * @param expression the numerical expression
     */
    @Override
    public void visit(GreaterThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    /**
     * if the expression is the numerical calculation, concat them with correlated tables or joins
     * @param expression the numerical expression
     */
    @Override
    public void visit(GreaterThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    /**
     * if the expression is the numerical calculation, concat them with correlated tables or joins
     * @param expression the numerical expression
     */
    @Override
    public void visit(MinorThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();


        concatExpression(expression, left, right);
    }

    /**
     * if the expression is the numerical calculation, concat them with correlated tables or joins
     * @param expression the numerical expression
     */
    @Override
    public void visit(MinorThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    /**
     * get the columns used in the condition and prepare for the projection pushdown
     * @return the used columns
     */
    public Map<String, Column> getRequired() {
        return required;
    }



}
