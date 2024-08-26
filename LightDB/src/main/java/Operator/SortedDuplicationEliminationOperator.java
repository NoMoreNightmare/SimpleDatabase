package Operator;

import tools.Tuple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * the operator to eliminate the duplicate records using list or hashset
 */
public class SortedDuplicationEliminationOperator extends Operator{
    Operator operator;
//    List<Tuple> lists = new ArrayList<>();
    int index = 0;

    Tuple previous = null;
    /**
     * construct the operator according to whether the tuples has been sorted
     * @param operator the child operator
     */
    public SortedDuplicationEliminationOperator(Operator operator){
        this.operator = operator;
    }

    @Override
    public Tuple getNextTuple() {
        Tuple tuple = operator.getNextTuple();
        if(tuple == null){
            return null;
        }
        while(tuple.equals(previous)){
            tuple = operator.getNextTuple();
            if(tuple == null){
                return null;
            }
        }
        previous = tuple;
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

}
