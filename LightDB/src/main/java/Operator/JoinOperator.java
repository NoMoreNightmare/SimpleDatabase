package Operator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import pojo.Parser.JoinExpressionDeParser;
import pojo.Parser.SelectExpressionDeParser;
import pojo.Tuple;

import java.util.ArrayList;
import java.util.List;

public class JoinOperator extends Operator{

    Operator left;
    Operator right;

    Expression expressionJoin;
    Tuple leftTuple;

    public JoinOperator(FromItem fromItem, Expression expression, List<Join> joins){
        //TODO 存储expression
        String tableName = fromItem.toString();
        JoinExpressionDeParser joinExpressionDeParser = new JoinExpressionDeParser();
        if(joins.get(0).getFromItem().getAlias() == null){
            joinExpressionDeParser.setTuple(joins.get(0).toString().toUpperCase());
        }else{
            joinExpressionDeParser.setTuple(joins.get(0).getFromItem().getAlias().toString().trim().toUpperCase());
        }

        Expression expressionSingle = null;
        Expression otherExpression = null;
        if(expression != null){
            expression.accept(joinExpressionDeParser);
            expressionSingle = joinExpressionDeParser.getThisExpressionSingle();
            otherExpression = joinExpressionDeParser.getOtherExpression();
        }

        this.expressionJoin = joinExpressionDeParser.getThisExpressionJoin();


        //如果joins的长度为1，则不再创建左深连接树，left和right都变成Select Operator
        if(joins.size() == 1){
            if(otherExpression == null){
                left = new ScanOperator(fromItem);
            }else{
                left = new SelectOperator(fromItem, otherExpression);
            }

            if(expressionSingle == null){
                right = new ScanOperator(joins.get(0).getFromItem());
            }else{
                right = new SelectOperator(joins.get(0).getFromItem(), expressionSingle);
            }

        }

        //如果joins的长度不为1，则获取并移除joins的最后一个table，为其创建select operator；为left则是新的join operator
        else{
            Join join = joins.remove(joins.size() - 1);
            left = new JoinOperator(fromItem, otherExpression, joins);

            if(expressionSingle == null){
                right = new ScanOperator(join.getFromItem());
            }else{
                right = new SelectOperator(join.getFromItem(), expressionSingle);
            }
        }
        //JoinExpressionDeParser每次只把右边的table的相关的expression筛选出来并留下，其他的expression传到left operator里
    }

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

    @Override
    public void reset() {
        left.reset();
        right.reset();
    }
}
