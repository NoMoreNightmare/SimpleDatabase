package Operator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.SelectItem;
import pojo.Tuple;

import java.util.ArrayList;
import java.util.List;

public class ProjectOperator extends Operator{
    Operator operator;
    List<SelectItem<?>> selectItem;

    public ProjectOperator(FromItem fromItem, Expression expression, List<SelectItem<?>> selectItem){
        if(expression == null){
            this.operator = new ScanOperator(fromItem);
        }else{
            this.operator = new SelectOperator(fromItem, expression);
        }

        this.selectItem = selectItem;
    }

    public ProjectOperator(FromItem fromItem, Expression expression, List<SelectItem<?>> selectItem, List<Join> joins){
        this.operator = new JoinOperator(fromItem, expression, joins);
        this.selectItem = selectItem;
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple = operator.getNextTuple();
        if(tuple == null){
            return null;
        }

        if(selectItem.size() == 1){
            if(selectItem.get(0).getExpression() instanceof AllColumns){
                return tuple;
            }
        }

        Tuple newTuple = new Tuple();

//        List<String> columns = new ArrayList<>();
//        List<Integer> values = new ArrayList<>();
//        for(SelectItem<?> item : selectItem){
//            String[] variousColumns = item.toString().split("\\.");
//            String column;
//            if(variousColumns.length > 1){
//                column = variousColumns[1];
//            }else{
//                column = variousColumns[0];
//            }
//
//            int value = tuple.getValue(column);
//
//            columns.add(column);
//            values.add(value);
//        }

        List<String> columns = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for(SelectItem<?> item : selectItem){
            String column = item.toString().toUpperCase();
            int value = tuple.getValue(column);
            columns.add(column);
            values.add(value);
        }

        newTuple.setColumns(columns);
        newTuple.setValues(values);

        return newTuple;


    }

    @Override
    public void reset() {
        operator.reset();
    }
}
