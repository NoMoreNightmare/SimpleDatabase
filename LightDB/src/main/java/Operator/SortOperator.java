package Operator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.SelectItem;
import pojo.Tuple;

import java.util.*;

public class SortOperator extends Operator{

    Operator operator;

    List<Tuple> tuples = new LinkedList<>();

    List<OrderByElement> orders;

    int index = 0;

//    public SortOperator(FromItem fromItem, Expression expression, List<SelectItem<?>> selectItems, List<Join> joins, List<OrderByElement> orders){
//        if(joins == null){
//            operator = new ProjectOperator(fromItem, expression, selectItems);
//        }else{
//            operator = new ProjectOperator(fromItem, expression, selectItems, joins);
//        }
//        this.orders = orders;
//        getAllTuples();
//    }
//
//    public SortOperator(FromItem fromItem, Expression expression, List<Join> joins, List<OrderByElement> orders){
//        operator = new JoinOperator(fromItem, expression, joins);
//        this.orders = orders;
//        getAllTuples();
//    }
//
//    public SortOperator(FromItem fromItem, Expression expression, List<OrderByElement> orders){
//        operator = new SelectOperator(fromItem, expression);
//        this.orders = orders;
//        getAllTuples();
//    }
//
//    public SortOperator(FromItem fromItem, List<OrderByElement> orders){
//        operator = new ScanOperator(fromItem);
//        this.orders = orders;
//        getAllTuples();
//    }

    public SortOperator(List<OrderByElement> orders, Operator operator){
        this.orders = orders;
        this.operator = operator;
        getAllTuples();
    }

    private void getAllTuples(){
        Tuple tuple = operator.getNextTuple();
        while(tuple != null){
            tuples.add(tuple);
            tuple = operator.getNextTuple();
        }

        tuples.sort(new MyComparator());
    }

    @Override
    public Tuple getNextTuple() {
        if(index == tuples.size()){
            return null;
        }

        Tuple tuple = tuples.get(index);
        index++;

        return tuple;
    }

    @Override
    public void reset() {
        index = 0;
    }


    class MyComparator implements Comparator<Tuple>{

        @Override
        public int compare(Tuple tuple1, Tuple tuple2) {
            for(OrderByElement order : orders){

                String column = order.toString().toUpperCase();
                int v1 = tuple1.getValue(column);
                int v2 = tuple2.getValue(column);
                if(v1 != v2){
                    return v1 - v2;
                }
            }

            return 0;
        }
    }
}
