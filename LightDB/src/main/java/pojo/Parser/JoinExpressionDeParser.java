package pojo.Parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import pojo.Tuple;

import javax.swing.*;
import java.util.*;

public class JoinExpressionDeParser extends ExpressionDeParser {
//    Tuple leftTuple;
    String rightTuple;
    Expression thisExpressionSingle;
    Expression thisExpressionJoin;
    Expression otherExpression;
    Map<String, Column> required = new HashMap<>();
    public JoinExpressionDeParser(String rightTuple){
        this.rightTuple = rightTuple;
    }

    public JoinExpressionDeParser(){

    }

    public void setTuple(String rightTuple){
        this.rightTuple = rightTuple;
    }

    public Expression getThisExpressionSingle() {
        return thisExpressionSingle;
    }

    public Expression getThisExpressionJoin() {
        return thisExpressionJoin;
    }

    public Expression getOtherExpression() {
        return otherExpression;
    }

    public void reset(){
        thisExpressionSingle = null;
        thisExpressionJoin = null;
        otherExpression = null;
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

    @Override
    public void visit(EqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();


        concatExpression(expression, left, right);
    }

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
                                AndExpression andExpression = new AndExpression(thisExpressionSingle, expression);
                                thisExpressionSingle = andExpression;
                            }
                        }else{
                            if(thisExpressionJoin == null){
                                thisExpressionJoin = expression;
                            }else{
                                AndExpression andExpression = new AndExpression(thisExpressionJoin, expression);
                                thisExpressionJoin = andExpression;
                            }
                            required.put(leftColumn.getFullyQualifiedName().toUpperCase(), leftColumn);
                            required.put(rightColumn.getFullyQualifiedName().toUpperCase(), rightColumn);
                        }
                    }else{
                        if(thisExpressionJoin == null){
                            thisExpressionJoin = expression;
                        }else{
                            AndExpression andExpression = new AndExpression(thisExpressionJoin, expression);
                            thisExpressionJoin = andExpression;
                        }
                        required.put(leftColumn.getFullyQualifiedName().toUpperCase(), leftColumn);
                        required.put(rightColumn.getFullyQualifiedName().toUpperCase(), rightColumn);
                    }
                }else{
                    if(otherExpression == null){
                        otherExpression = expression;
                    }else{
                        AndExpression andExpression = new AndExpression(otherExpression, expression);
                        otherExpression = andExpression;
                    }
                }
            }else{
                String newLeft =((Column) left).getTable().toString().toUpperCase();
                if(newLeft.equals(rightTuple)){
                    if(thisExpressionSingle == null){
                        thisExpressionSingle = expression;
                    }else{
                        AndExpression andExpression = new AndExpression(thisExpressionSingle, expression);
                        thisExpressionSingle = andExpression;
                    }
                }else{
                    if(otherExpression == null){
                        otherExpression = expression;
                    }else{
                        AndExpression andExpression = new AndExpression(otherExpression, expression);
                        otherExpression = andExpression;
                    }
                }
            }
        }else{
            if(right instanceof Column){
                String newRight =((Column) right).getTable().toString().toUpperCase();
                if(newRight.equals(rightTuple)){
                    if(thisExpressionSingle == null){
                        thisExpressionSingle = expression;
                    }else{
                        AndExpression andExpression = new AndExpression(thisExpressionSingle, expression);
                        thisExpressionSingle = andExpression;
                    }
                }else{
                    if(otherExpression == null){
                        otherExpression = expression;
                    }else{
                        AndExpression andExpression = new AndExpression(otherExpression, expression);
                        otherExpression = andExpression;
                    }
                }
            }else{
                if(thisExpressionSingle == null){
                    thisExpressionSingle = expression;
                }else{
                    AndExpression andExpression = new AndExpression(thisExpressionSingle, expression);
                    thisExpressionSingle = andExpression;
                }
            }
        }
    }

    @Override
    public void visit(NotEqualsTo expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    @Override
    public void visit(GreaterThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    @Override
    public void visit(GreaterThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    @Override
    public void visit(MinorThan expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    @Override
    public void visit(MinorThanEquals expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        concatExpression(expression, left, right);
    }

    public Map<String, Column> getRequired() {
        return required;
    }



}
