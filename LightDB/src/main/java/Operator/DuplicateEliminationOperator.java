package Operator;

import tools.Tuple;

import java.util.*;

/**
 * the operator to eliminate the duplicate records using list or hashset
 */
public class DuplicateEliminationOperator extends Operator{
    Operator operator;

    /**
     * construct the operator according to whether the tuples has been sorted
     * @param operator the child operator
     * @param ordered whether the tuples has been sorted
     */
    public DuplicateEliminationOperator(Operator operator, boolean ordered){
        if(ordered){
            this.operator = new SortedDuplicationEliminationOperator(operator);
        }else{
            this.operator = new ExternalHashingDuplicationEliminationOperator(operator);
        }

    }

    @Override
    public Tuple getNextTuple() {
        return operator.getNextTuple();
    }

    /**
     * reset the operator
     */
    @Override
    public void reset() {
        operator.reset();
    }

}
