package Operator;

import net.sf.jsqlparser.expression.Expression;
import tools.Parser.SelectExpressionDeParser;
import tools.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * the join operator
 */
public class JoinOperator extends Operator{

    Operator left;
    Operator right;

    Expression expressionJoin;
    Tuple leftTuple;

    /**
     * construct a join operator
     * @param expression the join condition expression
     * @param left the left child
     * @param right the right child
     */
    public JoinOperator(Expression expression, Operator left, Operator right){
        this.left = left;
        this.right = right;
        this.expressionJoin = expression;
    }

    /**
     * get the next tuple
     * @return next tuple
     */
    @Override
    public Tuple getNextTuple() {
        //如果当前leftTuple是null，返回null
        SelectExpressionDeParser deParser = new SelectExpressionDeParser();
        while(true){
            Tuple rightTuple = right.getNextTuple();
            if(rightTuple == null){
                leftTuple = left.getNextTuple();
                right.reset();
                rightTuple = right.getNextTuple();
            }

            if(leftTuple == null){
                leftTuple = left.getNextTuple();
                if(leftTuple == null){
                    return null;
                }
            }

            Tuple tuple = new Tuple();

            List<String> columns = new ArrayList<>(leftTuple.getColumns());
            List<Integer> values = new ArrayList<>(leftTuple.getValues());

            columns.addAll(rightTuple.getColumns());
            values.addAll(rightTuple.getValues());

            tuple.setColumns(columns);
            tuple.setValues(values);


            if(expressionJoin == null){
                return tuple;
            }

            deParser.setTuple(tuple);
            this.expressionJoin.accept(deParser);

            if(deParser.getWhereResult()){
                return tuple;
            }

            deParser.reset();

        }

    }

    /**
     * reset the operator
     */
    @Override
    public void reset() {
        left.reset();
        right.reset();
    }
}
