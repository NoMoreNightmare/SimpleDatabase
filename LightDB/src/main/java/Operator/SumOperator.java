package Operator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import pojo.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SumOperator extends Operator{
    Operator operator;
    Map<Tuple, Integer> sum = new HashMap<>();
    GroupByElement group;

    public SumOperator(GroupByElement group, Operator operator){
        this.operator = operator;
        this.group = group;
        //先project，再group（把最后一项sum先去掉，并进行project；然后生成新的tuple，计算新的列）
        getAllGroups();
        System.out.println(group.getGroupByExpressionList().get(1));
    }

    private void getAllGroups() {
        Tuple tuple = operator.getNextTuple();
        while(tuple != null){



            tuple = operator.getNextTuple();
        }
    }

    @Override
    public Tuple getNextTuple() {
        return null;
    }

    @Override
    public void reset() {
        operator.reset();
    }
}
