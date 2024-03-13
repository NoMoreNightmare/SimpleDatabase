package pojo.Parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.ArrayList;
import java.util.List;

/**
 * the visitor for the multiplication in the sum column
 * store all the columns or long values used in the multiplication in a list
 */
public class MultiplicationDeParser extends ExpressionDeParser {
    List<Expression> expressions = new ArrayList<>();


    /**
     * visit the multiplication
     * @param expression the multiplication expression
     */
    @Override
    public void visit(Multiplication expression){
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();

        if(left instanceof Multiplication){
            left.accept(this);
        }else{
            expressionForColumn(left);
        }

        if(right instanceof Multiplication){
            right.accept(this);
        }else{
            expressionForColumn(right);
        }



    }

    /**
     * if it is a column or long value object, stop visiting and store it in the list
     * @param expression the column or long value
     */
    private void expressionForColumn(Expression expression) {
        Column column = (Column) expression;
        expressions.add(column);
    }

    /**
     * return the expression list
     * @return the expression list
     */
    public List<Expression> getExpressions(){
        return this.expressions;
    }
}
