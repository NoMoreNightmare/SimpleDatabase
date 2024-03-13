package Operator;

import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectItem;
import tools.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * the projection operator that filter out the wanted columns
 */
public class ProjectOperator extends Operator{
    Operator operator;
    List<SelectItem<?>> selectItem;

    boolean materialized = false;

    /**
     * construct a projection operator
     * @param selectItem the projected columns
     * @param operator the child operator
     */
    public ProjectOperator(List<SelectItem<?>> selectItem, Operator operator){
        this.operator = operator;
        this.selectItem = selectItem;
    }

//    public ProjectOperator(FromItem fromItem, Expression expression, List<SelectItem<?>> selectItem, List<Join> joins){
//        this.operator = new JoinOperator(fromItem, expression, joins);
//        this.selectItem = selectItem;
//    }

    /**
     * get the next tuple
     * if the selected item is '*', return all the columns, else return the projected columns
     * @return next tuple
     */
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
            if("*".equals(column)){
                columns.addAll(tuple.getColumns());
                values.addAll(tuple.getValues());
            }else{
                int value = tuple.getValue(column);
                columns.add(column);
                values.add(value);
            }

        }

        newTuple.setColumns(columns);
        newTuple.setValues(values);
        newTuple.setTableName(tuple.getTableName());

        return newTuple;


    }

    /**
     * reset the operator
     */
    @Override
    public void reset() {
        operator.reset();
    }


}
