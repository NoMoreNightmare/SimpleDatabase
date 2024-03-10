package pojo.Parser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

import java.util.ArrayList;
import java.util.List;

public class MultiplicationDeParser extends ExpressionDeParser {
    List<Expression> expressions = new ArrayList<>();

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

    private void expressionForColumn(Expression expression) {
        Column column = (Column) expression;
        expressions.add(column);
    }

    public List<Expression> getExpressions(){
        return this.expressions;
    }
}
