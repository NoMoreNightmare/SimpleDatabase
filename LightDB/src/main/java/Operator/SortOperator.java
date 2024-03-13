package Operator;

import net.sf.jsqlparser.statement.select.OrderByElement;
import tools.Tuple;

import java.util.*;

/**
 * the order operator that sort the tuples according to the specified columns
 */
public class SortOperator extends Operator{

    Operator operator;

    List<Tuple> tuples = new LinkedList<>();

    List<OrderByElement> orders;

    int index = 0;

    /**
     * construct the sort operator according to the specified column
     * @param orders the columns used for sorting
     * @param operator child operator
     */
    public SortOperator(List<OrderByElement> orders, Operator operator){
        this.orders = orders;
        this.operator = operator;
        getAllTuples();
    }

    /**
     * store all the result tuples from the child operator and prepare for the sorting
     */
    private void getAllTuples(){
        Tuple tuple = operator.getNextTuple();
        while(tuple != null){
            tuples.add(tuple);
            tuple = operator.getNextTuple();
        }

        tuples.sort(new MyComparator());
    }

    /**
     * get the next tuple according to the sorted tuples
     * @return
     */
    @Override
    public Tuple getNextTuple() {
        if(index == tuples.size()){
            return null;
        }

        Tuple tuple = tuples.get(index);
        index++;

        return tuple;
    }

    /**
     * reset the operator
     */
    @Override
    public void reset() {
        operator.reset();
        index = 0;
    }


    /**
     * the compare rules of the Tuple class for the sorting
     */
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
